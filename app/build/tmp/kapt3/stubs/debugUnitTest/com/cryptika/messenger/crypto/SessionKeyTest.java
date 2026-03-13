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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\u0006H\u0007J\b\u0010\b\u001a\u00020\u0006H\u0007J\b\u0010\t\u001a\u00020\u0006H\u0007J\b\u0010\n\u001a\u00020\u0006H\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/cryptika/messenger/crypto/SessionKeyTest;", "", "()V", "sessionKeyManager", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;", "Directional roots are complementary between peers", "", "Directional roots are different from each other", "Session key changes when A_id and B_id are swapped", "Session key derivation is deterministic", "X25519 DH produces same shared secret from both sides", "Cryptika_debugUnitTest"})
public final class SessionKeyTest {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.SessionKeyManager sessionKeyManager = null;
    
    public SessionKeyTest() {
        super();
    }
}