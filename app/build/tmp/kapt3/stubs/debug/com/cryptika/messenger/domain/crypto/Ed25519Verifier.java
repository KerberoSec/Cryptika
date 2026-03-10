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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0006\u00a8\u0006\t"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/Ed25519Verifier;", "", "()V", "verify", "", "publicKeyBytes", "", "data", "signature", "Cryptika_debug"})
public final class Ed25519Verifier {
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.Ed25519Verifier INSTANCE = null;
    
    private Ed25519Verifier() {
        super();
    }
    
    /**
     * Verifies an Ed25519 signature.
     * @param publicKeyBytes 32-byte public key
     * @param data data that was signed
     * @param signature 64-byte signature
     * @return true if valid
     */
    public final boolean verify(@org.jetbrains.annotations.NotNull()
    byte[] publicKeyBytes, @org.jetbrains.annotations.NotNull()
    byte[] data, @org.jetbrains.annotations.NotNull()
    byte[] signature) {
        return false;
    }
}