package com.cryptika.messenger.domain.crypto;

import com.cryptika.messenger.domain.model.CryptoError;
import com.cryptika.messenger.domain.model.VerifiedTicket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

/**
 * Verifies server-issued session tickets.
 *
 * Ticket binary format (140 bytes total):
 *  [32 bytes] a_id
 *  [32 bytes] b_id
 *  [8 bytes]  timestamp_ms (big-endian)
 *  [4 bytes]  expiry_seconds (big-endian)
 *  [64 bytes] Ed25519 signature of the 76-byte payload above
 *
 * Clock skew tolerance: ±300 seconds
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 \u000b2\u00020\u0001:\u0001\u000bB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0003J\u000e\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0003R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/TicketManager;", "", "serverPublicKey", "", "([B)V", "createMockTicket", "Lcom/cryptika/messenger/domain/model/VerifiedTicket;", "aId", "bId", "verifyTicket", "ticketBytes", "Companion", "Cryptika_debug"})
public final class TicketManager {
    @org.jetbrains.annotations.NotNull()
    private final byte[] serverPublicKey = null;
    public static final int TICKET_PAYLOAD_SIZE = 76;
    public static final int SIGNATURE_SIZE = 64;
    public static final int TICKET_TOTAL_SIZE = 140;
    public static final long CLOCK_SKEW_TOLERANCE_MS = 300000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.crypto.TicketManager.Companion Companion = null;
    
    public TicketManager(@org.jetbrains.annotations.NotNull()
    byte[] serverPublicKey) {
        super();
    }
    
    /**
     * Verifies a raw ticket bytes from the server.
     *
     * @throws CryptoError.TicketSignatureInvalid if signature doesn't verify
     * @throws CryptoError.TicketExpired if ticket has expired
     * @throws CryptoError.TimestampStale if ticket timestamp is out of clock skew tolerance
     */
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.VerifiedTicket verifyTicket(@org.jetbrains.annotations.NotNull()
    byte[] ticketBytes) {
        return null;
    }
    
    /**
     * Creates a mock ticket for offline/testing use.
     * In production, tickets come from the server.
     */
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.VerifiedTicket createMockTicket(@org.jetbrains.annotations.NotNull()
    byte[] aId, @org.jetbrains.annotations.NotNull()
    byte[] bId) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/cryptika/messenger/domain/crypto/TicketManager$Companion;", "", "()V", "CLOCK_SKEW_TOLERANCE_MS", "", "SIGNATURE_SIZE", "", "TICKET_PAYLOAD_SIZE", "TICKET_TOTAL_SIZE", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}