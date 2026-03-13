package com.cryptika.messenger.domain.crypto;

import com.cryptika.messenger.domain.model.CryptoError;
import com.cryptika.messenger.domain.model.VerifiedTicket;
import java.security.MessageDigest;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages the ephemeral X25519 Diffie-Hellman handshake protocol.
 *
 * Both peers independently send a HANDSHAKE_OFFER when they connect to the relay.
 * On receiving the peer's offer, each side derives the shared session key and the
 * encrypted conversation can begin.
 *
 * ── Handshake offer wire format (97 bytes) ──────────────────────────────────
 *  [1]    magic = 0x01  — distinguishes from WirePacket (which always starts with 0x00,
 *                         since its first 4 bytes encode headerLen whose high byte is 0x00
 *                         for any realistic JSON header < 16 million bytes)
 *  [32]   ephemeral X25519 public key (generated fresh per WebSocket connection)
 *  [64]   Ed25519 signature of SHA-256(bytes[0..32])
 *
 * ── Session key derivation ───────────────────────────────────────────────────
 *  shared_secret = X25519(our_eph_priv, peer_eph_pub)   // symmetric: A⊗B == B⊗A
 *  (id_min, id_max) = sorted-by-hex(my_identity_hash, peer_identity_hash)
 *  session_key = SHA-256(shared_secret ‖ id_min ‖ id_max)
 *
 * Sorting the identity hashes ensures both sides compute the identical key
 * regardless of who sends their offer first.
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00172\u00020\u0001:\u0001\u0017B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\bJL\u0010\u000b\u001a\u0014\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\f2\u0006\u0010\r\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\t2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\t2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0013J\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/HandshakeManager;", "", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "sessionKeyManager", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;", "(Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;)V", "createOffer", "Lkotlin/Pair;", "", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "deriveSessionKey", "Lkotlin/Triple;", "offerBytes", "peerIdentityPublicKey", "ourEphemeralPair", "myIdentityHash", "peerIdentityHash", "verifiedTicket", "Lcom/cryptika/messenger/domain/model/VerifiedTicket;", "isHandshakeOffer", "", "packetBytes", "Companion", "Cryptika_debug"})
public final class HandshakeManager {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.SessionKeyManager sessionKeyManager = null;
    
    /**
     * First byte of every HANDSHAKE_OFFER — cannot appear as the high byte of a
     * WirePacket headerLen for any header < 16,777,216 bytes (impossible in practice).
     */
    public static final byte PACKET_TYPE = (byte)1;
    
    /**
     * Exact byte count of a HANDSHAKE_OFFER: 1 + 32 + 64
     */
    public static final int OFFER_SIZE = 97;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.HandshakeManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public HandshakeManager(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.SessionKeyManager sessionKeyManager) {
        super();
    }
    
    /**
     * Generates a fresh ephemeral X25519 keypair, signs the offer, and returns
     * the serialized 97-byte packet together with the keypair.
     *
     * Call exactly once per new WebSocket connection.
     * Hold the returned [EphemeralKeyPair] until [deriveSessionKey] is called.
     *
     * @return Pair of (serialized offer bytes, ephemeral keypair)
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<byte[], com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair> createOffer() {
        return null;
    }
    
    /**
     * Returns true if [packetBytes] has the correct size and magic byte for a
     * HANDSHAKE_OFFER, distinguishing it from a [MessageProcessor] wire packet.
     */
    public final boolean isHandshakeOffer(@org.jetbrains.annotations.NotNull()
    byte[] packetBytes) {
        return false;
    }
    
    /**
     * @param verifiedTicket when provided, the full K₀ formula is used:
     *  SHA-256(secret ∥ a_id ∥ b_id ∥ ticketHash ∥ ts)
     *  where a_id/b_id come from the server-signed ticket (canonical order).
     *  When null, falls back to SHA-256(secret ∥ id_min ∥ id_max).
     *
     * @return Triple of (sessionKey, sendRoot, recvRoot)
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Triple<byte[], byte[], byte[]> deriveSessionKey(@org.jetbrains.annotations.NotNull()
    byte[] offerBytes, @org.jetbrains.annotations.NotNull()
    byte[] peerIdentityPublicKey, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair ourEphemeralPair, @org.jetbrains.annotations.NotNull()
    byte[] myIdentityHash, @org.jetbrains.annotations.NotNull()
    byte[] peerIdentityHash, @org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.domain.model.VerifiedTicket verifiedTicket) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0005\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/HandshakeManager$Companion;", "", "()V", "OFFER_SIZE", "", "PACKET_TYPE", "", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}