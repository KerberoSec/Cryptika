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
 * Two offer types:
 *  0x01 (PACKET_TYPE)             — 97 bytes:  type(1) + ephem_pubkey(32) + sig(64)
 *  0x02 (PACKET_TYPE_WITH_TICKET) — 301 bytes: type(1) + ephem_pubkey(32) + sig(64) + ticket(204)
 *
 * The initiator (lower identity hash) sends type 0x02 with the dual-signed ticket embedded.
 * The acceptor (higher identity hash) sends type 0x01 without a ticket.
 *
 * Signing digest:
 *  Type 0x01 — SHA-256(type || ephem_pubkey)             — 33 bytes committed
 *  Type 0x02 — SHA-256(type || ephem_pubkey || ticketHash) — 65 bytes committed
 *
 * Binding the ticketHash to the offer signature prevents a relay operator
 * from substituting a different valid ticket inside a type-0x02 offer.
 *
 * Session key derivation
 *  shared_secret = X25519(our_eph_priv, peer_eph_pub)
 *  K₀ = SHA-256(secret ∥ a_id ∥ b_id ∥ ticketHash ∥ ts)   when ticket present
 *  K₀ = SHA-256(secret ∥ id_min ∥ id_max)                  fallback (ephemeral sessions)
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u0000 \u001b2\u00020\u0001:\u0001\u001bB\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\n0\b2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fJL\u0010\r\u001a\u0014\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\t0\u000e2\u0006\u0010\u000f\u001a\u00020\t2\u0006\u0010\u0010\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fJ \u0010\u0014\u001a\u0004\u0018\u00010\f2\u0006\u0010\u000f\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/HandshakeManager;", "", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "sessionKeyManager", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;", "(Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;)V", "createOffer", "Lkotlin/Pair;", "", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "verifiedTicket", "Lcom/cryptika/messenger/domain/model/VerifiedTicket;", "deriveSessionKey", "Lkotlin/Triple;", "offerBytes", "peerIdentityPublicKey", "ourEphemeralPair", "myIdentityHash", "peerIdentityHash", "extractTicketFromOffer", "userAPublicKey", "ticketManager", "Lcom/cryptika/messenger/domain/crypto/TicketManager;", "isHandshakeOffer", "", "packetBytes", "Companion", "Cryptika_debug"})
public final class HandshakeManager {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.SessionKeyManager sessionKeyManager = null;
    
    /**
     * First byte of a HANDSHAKE_OFFER without an embedded ticket.
     */
    public static final byte PACKET_TYPE = (byte)1;
    
    /**
     * First byte of a HANDSHAKE_OFFER with an embedded 204-byte dual-signed ticket.
     */
    public static final byte PACKET_TYPE_WITH_TICKET = (byte)2;
    
    /**
     * Byte count of a HANDSHAKE_OFFER without ticket: 1 + 32 + 64
     */
    public static final int OFFER_SIZE = 97;
    
    /**
     * Byte count of a HANDSHAKE_OFFER with ticket: 97 + 204
     */
    public static final int OFFER_WITH_TICKET_SIZE = 301;
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
     * the serialized packet together with the keypair.
     *
     * @param verifiedTicket When provided (initiator path), a 301-byte offer (type 0x02)
     *                      is produced with the dual-signed ticket appended.
     *                      When null (acceptor path), a 97-byte offer (type 0x01) is produced.
     *
     * @return Pair of (serialized offer bytes, ephemeral keypair)
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<byte[], com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair> createOffer(@org.jetbrains.annotations.Nullable()
    com.cryptika.messenger.domain.model.VerifiedTicket verifiedTicket) {
        return null;
    }
    
    /**
     * Returns true if [packetBytes] is a HANDSHAKE_OFFER (either type), distinguishing
     * it from a [MessageProcessor] wire packet.
     */
    public final boolean isHandshakeOffer(@org.jetbrains.annotations.NotNull()
    byte[] packetBytes) {
        return false;
    }
    
    /**
     * Extracts and verifies the dual-signed ticket embedded in a type-0x02 offer.
     * Returns null if the offer is not type 0x02 (i.e. acceptor's offer).
     *
     * @param offerBytes     the full 301-byte offer
     * @param userAPublicKey 32-byte Ed25519 public key of the initiator (contact.publicKeyBytes)
     * @param ticketManager  used to verify both signatures and temporal checks
     */
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.domain.model.VerifiedTicket extractTicketFromOffer(@org.jetbrains.annotations.NotNull()
    byte[] offerBytes, @org.jetbrains.annotations.NotNull()
    byte[] userAPublicKey, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.TicketManager ticketManager) {
        return null;
    }
    
    /**
     * Verifies a received HANDSHAKE_OFFER and derives the shared 32-byte session key.
     *
     * Accepts offers of either size (97 or 301 bytes). Only the first 97 bytes are
     * used for the DH key exchange; any embedded ticket must be extracted and verified
     * separately via [extractTicketFromOffer] before this call.
     *
     * @param offerBytes           97- or 301-byte offer received over the relay
     * @param peerIdentityPublicKey peer's Ed25519 public key (32 bytes, from [Contact])
     * @param ourEphemeralPair     our ephemeral keypair; private key is zeroized inside this call
     * @param myIdentityHash       our 32-byte SHA-256 identity hash
     * @param peerIdentityHash     peer's 32-byte SHA-256 identity hash
     * @param verifiedTicket       when provided, the full K₀ formula is used:
     *                            SHA-256(secret ∥ a_id ∥ b_id ∥ ticketHash ∥ ts)
     *                            When null, falls back to SHA-256(secret ∥ id_min ∥ id_max).
     *
     * @return Triple of (sessionKey_zeroed, sendRoot, recvRoot)
     * @throws CryptoError.SignatureInvalid if the Ed25519 offer signature is invalid
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/HandshakeManager$Companion;", "", "()V", "OFFER_SIZE", "", "OFFER_WITH_TICKET_SIZE", "PACKET_TYPE", "", "PACKET_TYPE_WITH_TICKET", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}