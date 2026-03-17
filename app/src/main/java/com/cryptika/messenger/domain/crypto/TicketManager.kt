// domain/crypto/TicketManager.kt
// Stage 2: Dual-Signature Session Ticket Verification
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import com.cryptika.messenger.domain.model.VerifiedTicket
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * Verifies dual-signature session tickets.
 *
 * Ticket binary format (204 bytes total):
 *   [32 bytes] a_id             — lower identity hash (initiator, User A)
 *   [32 bytes] b_id             — higher identity hash (acceptor, User B)
 *   [8 bytes]  timestamp_ms     — big-endian int64, chosen by User A
 *   [4 bytes]  expiry_seconds   — big-endian int32
 *   ── payload: 76 bytes ──
 *   [64 bytes] user_a_sig       — Ed25519_sign(UserA_privkey, SHA-256(payload))
 *   ── payload + user_a_sig: 140 bytes ──
 *   [64 bytes] server_sig       — Ed25519_sign(server_privkey, SHA-256(payload || user_a_sig))
 *   ══════ total: 204 bytes ══════
 *
 * Verification order:
 *   1. User A's signature over SHA-256(payload)           → proves User A created this ticket
 *   2. Server's countersignature over SHA-256(payload || user_a_sig) → proves server authorised it
 *   3. Clock skew ±5 minutes
 *   4. Expiry check
 *
 * Clock skew tolerance: ±300 seconds
 */
class TicketManager(
    private val serverPublicKey: ByteArray   // pinned at compile time: 32 bytes Ed25519
) {

    companion object {
        const val TICKET_PAYLOAD_SIZE   = 76    // 32 + 32 + 8 + 4
        const val USER_SIGNATURE_SIZE   = 64
        const val SERVER_SIGNATURE_SIZE = 64
        const val TICKET_TOTAL_SIZE     = TICKET_PAYLOAD_SIZE + USER_SIGNATURE_SIZE + SERVER_SIGNATURE_SIZE  // 204
        const val CLOCK_SKEW_TOLERANCE_MS  = 300_000L  // 5 minutes
        const val DEFAULT_EXPIRY_SECONDS   = 3600      // 1 hour
    }

    /**
     * Verifies both signatures of a dual-signed ticket.
     *
     * @param ticketBytes    204-byte raw ticket from the server
     * @param userAPublicKey 32-byte Ed25519 public key of the initiator (User A).
     *                       The caller supplies this:
     *                         - Initiator self-verifying: identityKeyManager.getPublicKeyBytes()
     *                         - Acceptor verifying: contact.publicKeyBytes
     *
     * @throws CryptoError.TicketSignatureInvalid if either signature is invalid or participants mismatch
     * @throws CryptoError.TimestampStale         if timestamp is outside ±5 min clock skew
     * @throws CryptoError.TicketExpired          if ticket has expired
     */
    fun verifyTicket(ticketBytes: ByteArray, userAPublicKey: ByteArray): VerifiedTicket {
        if (ticketBytes.size != TICKET_TOTAL_SIZE) {
            throw CryptoError.TicketSignatureInvalid
        }

        val payload  = ticketBytes.copyOf(TICKET_PAYLOAD_SIZE)                     // bytes   0..75
        val userASig = ticketBytes.copyOfRange(TICKET_PAYLOAD_SIZE, 140)            // bytes  76..139
        val serverSig = ticketBytes.copyOfRange(140, TICKET_TOTAL_SIZE)             // bytes 140..203

        // 1. Verify User A's signature over SHA-256(payload)
        val payloadHash = MessageDigest.getInstance("SHA-256").digest(payload)
        if (!Ed25519Verifier.verify(userAPublicKey, payloadHash, userASig)) {
            throw CryptoError.TicketSignatureInvalid
        }

        // 2. Verify server's countersignature over SHA-256(payload || userASig)
        val combined = payload + userASig                                           // 140 bytes
        val combinedHash = MessageDigest.getInstance("SHA-256").digest(combined)
        if (!Ed25519Verifier.verify(serverPublicKey, combinedHash, serverSig)) {
            throw CryptoError.TicketSignatureInvalid
        }

        // 3. Parse fields
        val buffer = ByteBuffer.wrap(payload)
        val aId = ByteArray(32).also { buffer.get(it) }
        val bId = ByteArray(32).also { buffer.get(it) }
        val timestamp = buffer.long
        val expirySeconds = buffer.int

        // 4. Check clock skew
        val now = System.currentTimeMillis()
        if (Math.abs(now - timestamp) > CLOCK_SKEW_TOLERANCE_MS) {
            throw CryptoError.TimestampStale
        }

        // 5. Check expiry
        val expiryMs = timestamp + (expirySeconds * 1000L)
        if (now > expiryMs) {
            throw CryptoError.TicketExpired
        }

        // 6. Compute ticket hash for session key derivation
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
