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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0004H\u0007J\b\u0010\u0006\u001a\u00020\u0004H\u0007J\b\u0010\u0007\u001a\u00020\u0004H\u0007\u00a8\u0006\b"}, d2 = {"Lcom/cryptika/messenger/crypto/HashRatchetTest;", "", "()V", "Forward secrecy \u2014 cannot derive K1 from K5", "", "Ratchet counter increments correctly", "Ratchet determinism \u2014 same root produces same key sequence", "Ratchet produces different keys at each step", "Cryptika_releaseUnitTest"})
public final class HashRatchetTest {
    
    public HashRatchetTest() {
        super();
    }
}