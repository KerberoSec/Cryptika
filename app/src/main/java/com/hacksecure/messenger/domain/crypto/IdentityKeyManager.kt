// domain/crypto/IdentityKeyManager.kt
// Stage 1 — Ed25519 Identity Key Management
package com.cryptika.messenger.domain.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.cryptika.messenger.domain.model.CryptoError
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages the long-term Ed25519 identity keypair.
 *
 * Strategy:
 * 1. Generate Ed25519 keypair via BouncyCastle (API ≥ 26 doesn't universally support Ed25519 in Keystore)
 * 2. Encrypt raw private key bytes with a Keystore-backed AES-256-GCM key
 * 3. Store encrypted blob in EncryptedPrefs
 * 4. Public key is stored plaintext (it's public anyway)
 *
 * The private key is NEVER exposed outside this class. All callers use sign().
 */
open class IdentityKeyManager(private val context: Context?) {

    companion object {
        private const val KEYSTORE_ALIAS = "cryptika_identity_wrapping_key"
        private const val PREFS_NAME = "cryptika_identity_prefs"
        private const val KEY_PUBLIC = "identity_public_key_b64"
        private const val KEY_ENCRYPTED_PRIVATE = "identity_encrypted_private_b64"
        private const val KEY_IV = "identity_iv_b64"
        private const val KEY_CREATED_AT = "identity_created_at"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val prefs by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?: throw IllegalStateException("Context is required for non-test usage of IdentityKeyManager")
    }

    /** Returns true if an identity key has already been generated */
    open fun hasIdentity(): Boolean = prefs.contains(KEY_PUBLIC) && prefs.contains(KEY_ENCRYPTED_PRIVATE)

    /**
     * Generates a new Ed25519 keypair on first launch.
     * Idempotent if already exists.
     */
    open fun generateIdentityIfNeeded(): Pair<ByteArray, Long> {
        if (hasIdentity()) return getPublicKeyBytes() to (prefs.getLong(KEY_CREATED_AT, 0L))

        // Generate Ed25519 keypair using BouncyCastle
        val random = SecureRandom()
        val generator = Ed25519KeyPairGenerator()
        generator.init(Ed25519KeyGenerationParameters(random))
        val keyPair = generator.generateKeyPair()

        val privateParams = keyPair.private as Ed25519PrivateKeyParameters
        val publicParams = keyPair.public as Ed25519PublicKeyParameters

        val privateKeyBytes = privateParams.encoded   // 32 bytes seed
        val publicKeyBytes = publicParams.encoded     // 32 bytes

        // Ensure AES wrapping key exists in Keystore
        ensureWrappingKey()

        // Encrypt private key with Keystore AES-GCM key
        val (encryptedPrivate, iv) = encryptWithKeystore(privateKeyBytes)

        // Zeroize raw private key bytes immediately after encryption
        privateKeyBytes.fill(0)
        // privateParams.destroy()  // Method not available in BouncyCastle 1.70

        val now = System.currentTimeMillis()
        prefs.edit()
            .putString(KEY_PUBLIC, publicKeyBytes.toBase64())
            .putString(KEY_ENCRYPTED_PRIVATE, encryptedPrivate.toBase64())
            .putString(KEY_IV, iv.toBase64())
            .putLong(KEY_CREATED_AT, now)
            .apply()

        return publicKeyBytes to now
    }

    /** Returns the raw 32-byte Ed25519 public key */
    open fun getPublicKeyBytes(): ByteArray {
        val b64 = prefs.getString(KEY_PUBLIC, null)
            ?: throw IllegalStateException("Identity not initialized")
        return b64.fromBase64()
    }

    /**
     * Signs data using the Ed25519 private key.
     * The data is pre-hashed (SHA-256) before signing.
     * Returns 64-byte signature.
     */
    open fun sign(data: ByteArray): ByteArray {
        val privateKeyBytes = decryptWithKeystore(
            encrypted = prefs.getString(KEY_ENCRYPTED_PRIVATE, null)!!.fromBase64(),
            iv = prefs.getString(KEY_IV, null)!!.fromBase64()
        )
        return try {
            val privateParams = Ed25519PrivateKeyParameters(privateKeyBytes, 0)
            val signer = Ed25519Signer()
            signer.init(true, privateParams)
            signer.update(data, 0, data.size)
            signer.generateSignature()
        } finally {
            privateKeyBytes.fill(0)
        }
    }

    /** Deletes the identity keypair (used when regenerating identity) */
    fun deleteIdentity() {
        prefs.edit().clear().apply()
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                keyStore.deleteEntry(KEYSTORE_ALIAS)
            }
        } catch (_: Exception) {}
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun ensureWrappingKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGen.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(false) // we manage IV ourselves
                    .build()
            )
            keyGen.generateKey()
        }
    }

    private fun encryptWithKeystore(data: ByteArray): Pair<ByteArray, ByteArray> {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val key = keyStore.getKey(KEYSTORE_ALIAS, null) as javax.crypto.SecretKey

        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(data)
        return encrypted to iv
    }

    private fun decryptWithKeystore(encrypted: ByteArray, iv: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val key = keyStore.getKey(KEYSTORE_ALIAS, null) as javax.crypto.SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encrypted)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Extension helpers
    // ──────────────────────────────────────────────────────────────────────────
    private fun ByteArray.toBase64(): String =
        android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray =
        android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
}

// ══════════════════════════════════════════════════════════════════════════════
// IdentityHash utility
// ══════════════════════════════════════════════════════════════════════════════
object IdentityHash {
    fun compute(publicKeyBytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(publicKeyBytes)

    fun toHexString(hash: ByteArray): String = hash.joinToString("") { "%02x".format(it) }
}

// ══════════════════════════════════════════════════════════════════════════════
// Ed25519 Verifier (static, no private key needed)
// ══════════════════════════════════════════════════════════════════════════════
object Ed25519Verifier {
    /**
     * Verifies an Ed25519 signature.
     * @param publicKeyBytes 32-byte public key
     * @param data data that was signed
     * @param signature 64-byte signature
     * @return true if valid
     */
    fun verify(publicKeyBytes: ByteArray, data: ByteArray, signature: ByteArray): Boolean {
        return try {
            val publicParams = Ed25519PublicKeyParameters(publicKeyBytes, 0)
            val verifier = Ed25519Signer()
            verifier.init(false, publicParams)
            verifier.update(data, 0, data.size)
            verifier.verifySignature(signature)
        } catch (e: Exception) {
            false
        }
    }
}
