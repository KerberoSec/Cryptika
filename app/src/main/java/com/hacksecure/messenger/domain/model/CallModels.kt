// domain/model/CallModels.kt
package com.cryptika.messenger.domain.model

// ══════════════════════════════════════════════════════════════════════════════
// CALL STATE
// ══════════════════════════════════════════════════════════════════════════════

enum class CallState {
    IDLE,
    OUTGOING_RINGING,   // we placed the call, waiting for peer to answer
    INCOMING_RINGING,   // peer is calling us, waiting for local accept/reject
    ACTIVE,             // audio flowing in both directions
    ENDING              // cleanup in progress before returning to IDLE
}

// ══════════════════════════════════════════════════════════════════════════════
// CALL SIGNAL TYPES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Byte codes embedded in the signal packet at offset [1].
 * Must never overlap with HandshakeManager.PACKET_TYPE (0x01) since signals
 * use magic byte 0x02 at offset [0] to distinguish themselves.
 */
enum class CallSignalType(val code: Byte) {
    OFFER(0x01),    // caller → callee: start call, includes caller's ephemeral X25519 pub key
    ANSWER(0x02),   // callee → caller: accept call, includes callee's ephemeral X25519 pub key
    REJECT(0x03),   // callee → caller: decline (busy=false scenario)
    HANGUP(0x04),   // either → other: end active call or cancel outgoing call
    BUSY(0x05);     // callee → caller: callee is already in another call

    companion object {
        fun fromByte(b: Byte): CallSignalType? = entries.find { it.code == b }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// INCOMING CALL DATA
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Populated by [CallManager] when an incoming OFFER signal is received.
 * Used by [CallViewModel] and navigation to drive incoming-call UI.
 * Cleared when the call is answered, rejected, or the peer hangs up.
 */
data class IncomingCallData(
    val convId: String,
    val contactId: String,      // UUID of the Contact
    val contactName: String,
    val callId: String          // hex of 16 random call_id bytes
)
