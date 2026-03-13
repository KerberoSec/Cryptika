package com.cryptika.messenger.domain.crypto;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import com.cryptika.messenger.domain.model.CryptoError;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0016\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eB\u000f\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\fH\u0002J\u0006\u0010\u000f\u001a\u00020\u0010J\u001c\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u00122\u0006\u0010\u0013\u001a\u00020\fH\u0002J\b\u0010\u0014\u001a\u00020\u0010H\u0002J\u0014\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00160\u0012H\u0016J\b\u0010\u0017\u001a\u00020\fH\u0016J\b\u0010\u0018\u001a\u00020\u0019H\u0016J\u0010\u0010\u001a\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\fH\u0016J\f\u0010\u001b\u001a\u00020\f*\u00020\u001cH\u0002J\f\u0010\u001d\u001a\u00020\u001c*\u00020\fH\u0002R\u0010\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\u001f"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "prefs", "Landroid/content/SharedPreferences;", "getPrefs", "()Landroid/content/SharedPreferences;", "prefs$delegate", "Lkotlin/Lazy;", "decryptWithKeystore", "", "encrypted", "iv", "deleteIdentity", "", "encryptWithKeystore", "Lkotlin/Pair;", "data", "ensureWrappingKey", "generateIdentityIfNeeded", "", "getPublicKeyBytes", "hasIdentity", "", "sign", "fromBase64", "", "toBase64", "Companion", "Cryptika_debug"})
public class IdentityKeyManager {
    @org.jetbrains.annotations.Nullable()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEYSTORE_ALIAS = "cryptika_identity_wrapping_key";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "cryptika_identity_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PUBLIC = "identity_public_key_b64";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ENCRYPTED_PRIVATE = "identity_encrypted_private_b64";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_IV = "identity_iv_b64";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_CREATED_AT = "identity_created_at";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ANDROID_KEYSTORE = "AndroidKeyStore";
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy prefs$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.IdentityKeyManager.Companion Companion = null;
    
    public IdentityKeyManager(@org.jetbrains.annotations.Nullable()
    android.content.Context context) {
        super();
    }
    
    private final android.content.SharedPreferences getPrefs() {
        return null;
    }
    
    /**
     * Returns true if an identity key has already been generated
     */
    public boolean hasIdentity() {
        return false;
    }
    
    /**
     * Generates a new Ed25519 keypair on first launch.
     * Idempotent if already exists.
     */
    @org.jetbrains.annotations.NotNull()
    public kotlin.Pair<byte[], java.lang.Long> generateIdentityIfNeeded() {
        return null;
    }
    
    /**
     * Returns the raw 32-byte Ed25519 public key
     */
    @org.jetbrains.annotations.NotNull()
    public byte[] getPublicKeyBytes() {
        return null;
    }
    
    /**
     * Signs data using the Ed25519 private key.
     * The data is pre-hashed (SHA-256) before signing.
     * Returns 64-byte signature.
     */
    @org.jetbrains.annotations.NotNull()
    public byte[] sign(@org.jetbrains.annotations.NotNull()
    byte[] data) {
        return null;
    }
    
    /**
     * Deletes the identity keypair (used when regenerating identity)
     */
    public final void deleteIdentity() {
    }
    
    private final void ensureWrappingKey() {
    }
    
    private final kotlin.Pair<byte[], byte[]> encryptWithKeystore(byte[] data) {
        return null;
    }
    
    private final byte[] decryptWithKeystore(byte[] encrypted, byte[] iv) {
        return null;
    }
    
    private final java.lang.String toBase64(byte[] $this$toBase64) {
        return null;
    }
    
    private final byte[] fromBase64(java.lang.String $this$fromBase64) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager$Companion;", "", "()V", "ANDROID_KEYSTORE", "", "GCM_IV_LENGTH", "", "GCM_TAG_LENGTH", "KEYSTORE_ALIAS", "KEY_CREATED_AT", "KEY_ENCRYPTED_PRIVATE", "KEY_IV", "KEY_PUBLIC", "PREFS_NAME", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}