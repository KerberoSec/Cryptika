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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0004H\u0007J\b\u0010\u0006\u001a\u00020\u0004H\u0007\u00a8\u0006\u0007"}, d2 = {"Lcom/cryptika/messenger/crypto/IdentityHashTest;", "", "()V", "SHA256 is deterministic", "", "SHA256 of different inputs produces different hashes", "SHA256 of zeros 32 bytes matches known vector", "Cryptika_debugUnitTest"})
public final class IdentityHashTest {
    
    public IdentityHashTest() {
        super();
    }
}