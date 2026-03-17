package com.cryptika.messenger.domain.crypto;

import com.cryptika.messenger.domain.model.CryptoError;
import com.cryptika.messenger.domain.model.VerifiedTicket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Verifies dual-signature session tickets.
 *
 * Ticket binary format (204 bytes total):
 *  [32 bytes] a_id             — lower identity hash (initiator, User A)
 *  [32 bytes] b_id             — higher identity hash (acceptor, User B)
 *  [8 bytes]  timestamp_ms     — big-endian int64, chosen by User A
 *  [4 bytes]  expiry_seconds   — big-endian int32
 *  ── payload: 76 bytes ──
 *  [64 bytes] user_a_sig       — Ed25519_sign(UserA_privkey, SHA-256(payload))
 *  ── payload + user_a_sig: 140 bytes ──
 *  [64 bytes] server_sig       — Ed25519_sign(server_privkey, SHA-256(payload || user_a_sig))
 *  ══════ total: 204 bytes ══════
 *
 * Verification order:
 *  1. User A's signature over SHA-256(payload)           → proves User A created this ticket
 *  2. Server's countersignature over SHA-256(payload || user_a_sig) → proves server authorised it
 *  3. Clock skew ±5 minutes
 *  4. Expiry check
 *
 * Clock skew tolerance: ±300 seconds
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u0003J\u0016\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\u00032\u0006\u0010\r\u001a\u00020\u0003R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/TicketManager;", "", "serverPublicKey", "", "([B)V", "validateParticipants", "", "ticket", "Lcom/cryptika/messenger/domain/model/VerifiedTicket;", "myIdentityHash", "peerIdentityHash", "verifyTicket", "ticketBytes", "userAPublicKey", "Companion", "Cryptika_debug"})
public final class TicketManager {
    @org.jetbrains.annotations.NotNull()
    private final byte[] serverPublicKey = null;
    public static final int TICKET_PAYLOAD_SIZE = 76;
    public static final int USER_SIGNATURE_SIZE = 64;
    public static final int SERVER_SIGNATURE_SIZE = 64;
    public static final int TICKET_TOTAL_SIZE = 204;
    public static final long CLOCK_SKEW_TOLERANCE_MS = 300000L;
    public static final int DEFAULT_EXPIRY_SECONDS = 3600;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.TicketManager.Companion Companion = null;
    
    public TicketManager(@org.jetbrains.annotations.NotNull()
    byte[] serverPublicKey) {
        super();
    }
    
    /**
     * Verifies both signatures of a dual-signed ticket.
     *
     * @param ticketBytes    204-byte raw ticket from the server
     * @param userAPublicKey 32-byte Ed25519 public key of the initiator (User A).
     *                      The caller supplies this:
     *                        - Initiator self-verifying: identityKeyManager.getPublicKeyBytes()
     *                        - Acceptor verifying: contact.publicKeyBytes
     *
     * @throws CryptoError.TicketSignatureInvalid if either signature is invalid or participants mismatch
     * @throws CryptoError.TimestampStale         if timestamp is outside ±5 min clock skew
     * @throws CryptoError.TicketExpired          if ticket has expired
     */
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.VerifiedTicket verifyTicket(@org.jetbrains.annotations.NotNull()
    byte[] ticketBytes, @org.jetbrains.annotations.NotNull()
    byte[] userAPublicKey) {
        return null;
    }
    
    /**
     * Validates that a verified ticket's participant IDs match the expected
     * local and peer identity hashes (in either order, since ticket uses canonical sort).
     *
     * @throws CryptoError.TicketSignatureInvalid if participants don't match
     */
    public final void validateParticipants(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.VerifiedTicket ticket, @org.jetbrains.annotations.NotNull()
    byte[] myIdentityHash, @org.jetbrains.annotations.NotNull()
    byte[] peerIdentityHash) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/TicketManager$Companion;", "", "()V", "CLOCK_SKEW_TOLERANCE_MS", "", "DEFAULT_EXPIRY_SECONDS", "", "SERVER_SIGNATURE_SIZE", "TICKET_PAYLOAD_SIZE", "TICKET_TOTAL_SIZE", "USER_SIGNATURE_SIZE", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}