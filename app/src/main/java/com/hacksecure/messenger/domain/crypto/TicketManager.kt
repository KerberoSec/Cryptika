// domain/crypto/TicketManager.kt
// Stage 2 — Kerberos-inspired Session Ticket Verification
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import com.cryptika.messenger.domain.model.VerifiedTicket
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * Verifies server-issued session tickets.
 *
 * Ticket binary format (140 bytes total):
 *   [32 bytes] a_id
 *   [32 bytes] b_id
 *   [8 bytes]  timestamp_ms (big-endian)
 *   [4 bytes]  expiry_seconds (big-endian)
 *   [64 bytes] Ed25519 signature of the 76-byte payload above
 *
 * Clock skew tolerance: ±300 seconds
 */
class TicketManager(
    private val serverPublicKey: ByteArray   // pinned at compile time — 32 bytes Ed25519
) {

    companion object {
        const val TICKET_PAYLOAD_SIZE = 76   // 32 + 32 + 8 + 4
        const val SIGNATURE_SIZE = 64
        const val TICKET_TOTAL_SIZE = TICKET_PAYLOAD_SIZE + SIGNATURE_SIZE
        const val CLOCK_SKEW_TOLERANCE_MS = 300_000L  // 5 minutes
    }

    /**
     * Verifies a raw ticket bytes from the server.
     *
     * @throws CryptoError.TicketSignatureInvalid if signature doesn't verify
     * @throws CryptoError.TicketExpired if ticket has expired
     * @throws CryptoError.TimestampStale if ticket timestamp is out of clock skew tolerance
     */
    fun verifyTicket(ticketBytes: ByteArray): VerifiedTicket {
        if (ticketBytes.size != TICKET_TOTAL_SIZE) {
            throw CryptoError.TicketSignatureInvalid
        }

        val payload = ticketBytes.copyOf(TICKET_PAYLOAD_SIZE)
        val signature = ticketBytes.copyOfRange(TICKET_PAYLOAD_SIZE, TICKET_TOTAL_SIZE)

        // 1. Verify Ed25519 signature
        if (!Ed25519Verifier.verify(serverPublicKey, payload, signature)) {
            throw CryptoError.TicketSignatureInvalid
        }

        // 2. Parse fields
        val buffer = ByteBuffer.wrap(payload)
        val aId = ByteArray(32).also { buffer.get(it) }
        val bId = ByteArray(32).also { buffer.get(it) }
        val timestamp = buffer.long
        val expirySeconds = buffer.int

        // 3. Check clock skew
        val now = System.currentTimeMillis()
        if (Math.abs(now - timestamp) > CLOCK_SKEW_TOLERANCE_MS) {
            throw CryptoError.TimestampStale
        }

        // 4. Check expiry
        val expiryMs = timestamp + (expirySeconds * 1000L)
        if (now > expiryMs) {
            throw CryptoError.TicketExpired
        }

        // 5. Compute ticket hash for session key derivation
        val ticketHash = MessageDigest.getInstance("SHA-256").digest(ticketBytes)

        return VerifiedTicket(
            rawBytes = ticketBytes,
            ticketHash = ticketHash,
            aId = aId,
            bId = bId,
            timestamp = timestamp,
            expirySeconds = expirySeconds
        )
    }

    /**
     * Validates that a verified ticket's participant IDs match the expected
     * local and peer identity hashes (in either order, since ticket uses canonical sort).
     *
     * @throws CryptoError.TicketSignatureInvalid if participants don't match
     */
    fun validateParticipants(
        ticket: VerifiedTicket,
        myIdentityHash: ByteArray,
        peerIdentityHash: ByteArray
    ) {
        val matchForward = ticket.aId.contentEquals(myIdentityHash) && ticket.bId.contentEquals(peerIdentityHash)
        val matchReverse = ticket.aId.contentEquals(peerIdentityHash) && ticket.bId.contentEquals(myIdentityHash)
        if (!matchForward && !matchReverse) {
            throw CryptoError.TicketSignatureInvalid
        }
    }
}
