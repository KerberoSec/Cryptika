package com.cryptika.messenger.crypto;

import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.model.CryptoError;
import org.junit.Test;
import java.security.MessageDigest;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import java.security.SecureRandom;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0004H\u0007J\b\u0010\u0006\u001a\u00020\u0004H\u0007J\b\u0010\u0007\u001a\u00020\u0004H\u0007J\b\u0010\b\u001a\u00020\u0004H\u0007J\b\u0010\t\u001a\u00020\nH\u0002J\b\u0010\u000b\u001a\u00020\nH\u0002J\b\u0010\f\u001a\u00020\nH\u0002\u00a8\u0006\r"}, d2 = {"Lcom/cryptika/messenger/crypto/AEADCipherTest;", "", "()V", "Different counters produce different nonces", "", "Encrypt then decrypt produces original plaintext", "Flipped bit in additional data causes AEADAuthFailed", "Flipped bit in ciphertext causes AEADAuthFailed", "Nonce derivation is deterministic", "makeAdditionalData", "", "makeKey", "makeNonce", "Cryptika_debugUnitTest"})
public final class AEADCipherTest {
    
    public AEADCipherTest() {
        super();
    }
    
    private final byte[] makeKey() {
        return null;
    }
    
    private final byte[] makeNonce() {
        return null;
    }
    
    private final byte[] makeAdditionalData() {
        return null;
    }
}