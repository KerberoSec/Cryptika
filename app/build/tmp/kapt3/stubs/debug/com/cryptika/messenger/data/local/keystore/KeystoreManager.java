package com.cryptika.messenger.data.local.keystore;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0005\u0018\u0000  2\u00020\u0001:\u0001 B\u0005\u00a2\u0006\u0002\u0010\u0002J \u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006H\u0002J\u0016\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u0006J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\f\u001a\u00020\bJ$\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00060\u00122\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\u0006H\u0002J\u0016\u0010\u0014\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u0006J\u0010\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u000e\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010\u001a\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\bJ\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\f\u001a\u00020\bJ\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0018\u001a\u00020\u0019J\f\u0010\u001e\u001a\u00020\u0006*\u00020\bH\u0002J\f\u0010\u001f\u001a\u00020\b*\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;", "", "()V", "keyStore", "Ljava/security/KeyStore;", "decrypt", "", "alias", "", "encrypted", "iv", "decryptMessageFromStorage", "messageId", "blob", "deleteKeyByAlias", "", "deleteMessageKey", "encrypt", "Lkotlin/Pair;", "data", "encryptMessageForStorage", "plaintext", "ensureKey", "generateAndStoreDbPassphrase", "prefs", "Landroid/content/SharedPreferences;", "generateMessageKey", "hasMessageKey", "", "retrieveDbPassphrase", "fromBase64", "toBase64", "Companion", "Cryptika_debug"})
public final class KeystoreManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ANDROID_KEYSTORE = "AndroidKeyStore";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String DB_PASSPHRASE_KEY_ALIAS = "cryptika_db_passphrase_key";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String MESSAGE_KEY_PREFIX = "msg_";
    @org.jetbrains.annotations.NotNull()
    private final java.security.KeyStore keyStore = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.local.keystore.KeystoreManager.Companion Companion = null;
    
    public KeystoreManager() {
        super();
    }
    
    /**
     * Generates a 32-byte random DB passphrase, encrypts it with a Keystore key,
     * and stores the encrypted blob in SharedPreferences.
     *
     * Returns the raw 32-byte passphrase (caller must zeroize after use).
     */
    @org.jetbrains.annotations.NotNull()
    public final byte[] generateAndStoreDbPassphrase(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        return null;
    }
    
    /**
     * Retrieves and decrypts the stored DB passphrase.
     * Returns null if not yet stored.
     */
    @org.jetbrains.annotations.Nullable()
    public final byte[] retrieveDbPassphrase(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        return null;
    }
    
    /**
     * Generates a fresh AES-256-GCM key for a specific message.
     * @param messageId UUID of the message
     * @return key alias to store in DB
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateMessageKey(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId) {
        return null;
    }
    
    /**
     * Encrypts message plaintext with its dedicated storage key.
     * @return encrypted blob (IV prepended)
     */
    @org.jetbrains.annotations.NotNull()
    public final byte[] encryptMessageForStorage(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    byte[] plaintext) {
        return null;
    }
    
    /**
     * Decrypts a stored message blob.
     * @throws IllegalStateException if key has been deleted (message expired)
     */
    @org.jetbrains.annotations.NotNull()
    public final byte[] decryptMessageFromStorage(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    byte[] blob) {
        return null;
    }
    
    /**
     * Deletes a message's storage key from Keystore.
     * After this, the stored ciphertext is UNRECOVERABLE noise.
     * This is the core of the expiry mechanism.
     */
    public final void deleteMessageKey(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId) {
    }
    
    /**
     * Deletes a key by its full alias (used for direct alias management).
     */
    public final void deleteKeyByAlias(@org.jetbrains.annotations.NotNull()
    java.lang.String alias) {
    }
    
    public final boolean hasMessageKey(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId) {
        return false;
    }
    
    private final void ensureKey(java.lang.String alias) {
    }
    
    private final kotlin.Pair<byte[], byte[]> encrypt(java.lang.String alias, byte[] data) {
        return null;
    }
    
    private final byte[] decrypt(java.lang.String alias, byte[] encrypted, byte[] iv) {
        return null;
    }
    
    private final java.lang.String toBase64(byte[] $this$toBase64) {
        return null;
    }
    
    private final byte[] fromBase64(java.lang.String $this$fromBase64) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/cryptika/messenger/data/local/keystore/KeystoreManager$Companion;", "", "()V", "ANDROID_KEYSTORE", "", "DB_PASSPHRASE_KEY_ALIAS", "GCM_IV_LENGTH", "", "GCM_TAG_LENGTH", "MESSAGE_KEY_PREFIX", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}