// data/local/keystore/KeystoreManager.kt
// Android Keystore wrapper — DB passphrase + per-message storage keys
package com.cryptika.messenger.data.local.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages AES-256-GCM keys in Android Keystore.
 *
 * Used for:
 * 1. DB passphrase encryption (singleton key "cryptika_db_passphrase_key")
 * 2. Per-message storage keys (alias = "msg_<uuid>")
 *
 * The DB passphrase is a 32-byte random value generated on first launch,
 * encrypted by the Keystore key. Even with raw DB file access, messages
 * are unreadable without the Keystore.
 */
class KeystoreManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DB_PASSPHRASE_KEY_ALIAS = "cryptika_db_passphrase_key"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        const val MESSAGE_KEY_PREFIX = "msg_"
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    // ──────────────────────────────────────────────────────────────────────────
    // DB Passphrase
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generates a 32-byte random DB passphrase, encrypts it with a Keystore key,
     * and stores the encrypted blob in SharedPreferences.
     *
     * Returns the raw 32-byte passphrase (caller must zeroize after use).
     */
    fun generateAndStoreDbPassphrase(prefs: android.content.SharedPreferences): ByteArray {
        ensureKey(DB_PASSPHRASE_KEY_ALIAS)
        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val (encrypted, iv) = encrypt(DB_PASSPHRASE_KEY_ALIAS, passphrase)

        prefs.edit()
            .putString("db_passphrase_enc", encrypted.toBase64())
            .putString("db_passphrase_iv", iv.toBase64())
            .apply()

        return passphrase
    }

    /**
     * Retrieves and decrypts the stored DB passphrase.
     * Returns null if not yet stored.
     */
    fun retrieveDbPassphrase(prefs: android.content.SharedPreferences): ByteArray? {
        val encB64 = prefs.getString("db_passphrase_enc", null) ?: return null
        val ivB64 = prefs.getString("db_passphrase_iv", null) ?: return null
        return decrypt(DB_PASSPHRASE_KEY_ALIAS, encB64.fromBase64(), ivB64.fromBase64())
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Per-Message Storage Keys
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generates a fresh AES-256-GCM key for a specific message.
     * @param messageId UUID of the message
     * @return key alias to store in DB
     */
    fun generateMessageKey(messageId: String): String {
        val alias = "$MESSAGE_KEY_PREFIX$messageId"
        ensureKey(alias)
        return alias
    }

    /**
     * Encrypts message plaintext with its dedicated storage key.
     * @return encrypted blob (IV prepended)
     */
    fun encryptMessageForStorage(messageId: String, plaintext: ByteArray): ByteArray {
        val alias = "$MESSAGE_KEY_PREFIX$messageId"
        val (encrypted, iv) = encrypt(alias, plaintext)
        // Prepend IV to ciphertext blob for self-contained storage
        return iv + encrypted
    }

    /**
     * Decrypts a stored message blob.
     * @throws IllegalStateException if key has been deleted (message expired)
     */
    fun decryptMessageFromStorage(messageId: String, blob: ByteArray): ByteArray {
        val alias = "$MESSAGE_KEY_PREFIX$messageId"
        if (!keyStore.containsAlias(alias)) {
            throw IllegalStateException("Storage key deleted — message has expired")
        }
        val iv = blob.copyOf(GCM_IV_LENGTH)
        val encrypted = blob.copyOfRange(GCM_IV_LENGTH, blob.size)
        return decrypt(alias, encrypted, iv)
    }

    /**
     * Deletes a message's storage key from Keystore.
     * After this, the stored ciphertext is UNRECOVERABLE noise.
     * This is the core of the expiry mechanism.
     */
    fun deleteMessageKey(messageId: String) {
        val alias = "$MESSAGE_KEY_PREFIX$messageId"
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    /**
     * Deletes a key by its full alias (used for direct alias management).
     */
    fun deleteKeyByAlias(alias: String) {
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    fun hasMessageKey(messageId: String): Boolean {
        val alias = "$MESSAGE_KEY_PREFIX$messageId"
        return keyStore.containsAlias(alias)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun ensureKey(alias: String) {
        if (!keyStore.containsAlias(alias)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGen.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(false)
                    .build()
            )
            keyGen.generateKey()
        }
    }

    private fun encrypt(alias: String, data: ByteArray): Pair<ByteArray, ByteArray> {
        val key = keyStore.getKey(alias, null) as SecretKey
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(data) to iv
    }

    private fun decrypt(alias: String, encrypted: ByteArray, iv: ByteArray): ByteArray {
        val key = keyStore.getKey(alias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encrypted)
    }

    private fun ByteArray.toBase64(): String =
        android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray =
        android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
}
