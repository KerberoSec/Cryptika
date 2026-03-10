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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0004\u00a8\u0006\t"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/IdentityHash;", "", "()V", "compute", "", "publicKeyBytes", "toHexString", "", "hash", "Cryptika_debug"})
public final class IdentityHash {
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.IdentityHash INSTANCE = null;
    
    private IdentityHash() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] compute(@org.jetbrains.annotations.NotNull()
    byte[] publicKeyBytes) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String toHexString(@org.jetbrains.annotations.NotNull()
    byte[] hash) {
        return null;
    }
}