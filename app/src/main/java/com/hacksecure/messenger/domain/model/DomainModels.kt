// domain/model/DomainModels.kt
package com.cryptika.messenger.domain.model

// ══════════════════════════════════════════════════════════════════════════════
// APP VERSION INFO
// ══════════════════════════════════════════════════════════════════════════════
object AppVersion {
    const val MAJOR = 1
    const val MINOR = 0
    const val PATCH = 0
    const val NAME = "1.0.0"
    const val CODE = 1

    // Feature flags — future voice calling will be gated here
    const val FEATURE_MESSAGING = true
    const val FEATURE_VOICE_CALLS = true    // Encrypted relay-based voice calls
    const val FEATURE_VIDEO_CALLS = false   // Phase 3 — future roadmap
}

// ══════════════════════════════════════════════════════════════════════════════
// STAGE 1: IDENTITY
// ══════════════════════════════════════════════════════════════════════════════
data class LocalIdentity(
    val publicKeyBytes: ByteArray,      // 32 bytes Ed25519 public key
    val identityHash: ByteArray,        // SHA-256 of public key (32 bytes)
    val createdAt: Long                 // Unix ms
) {
    val identityHex: String get() = identityHash.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalIdentity) return false
        return publicKeyBytes.contentEquals(other.publicKeyBytes)
    }

    override fun hashCode(): Int = publicKeyBytes.contentHashCode()
}

data class Contact(
    val id: String,                     // UUID
    val identityHash: ByteArray,        // SHA-256 of their public key
    val publicKeyBytes: ByteArray,      // 32-byte Ed25519 public key
    val displayName: String,
    val verifiedAt: Long,
    val keyChangedAt: Long? = null      // non-null = warn user
) {
    val identityHex: String get() = identityHash.toHexString()
    val hasKeyChanged: Boolean get() = keyChangedAt != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false
        return id == other.id
    }
    override fun hashCode(): Int = id.hashCode()
}

// ══════════════════════════════════════════════════════════════════════════════
// STAGE 2: SESSION TICKET
// ══════════════════════════════════════════════════════════════════════════════
data class VerifiedTicket(
    val rawBytes: ByteArray,
    val ticketHash: ByteArray,          // SHA-256 of rawBytes
    val aId: ByteArray,                 // 32 bytes
    val bId: ByteArray,                 // 32 bytes
    val timestamp: Long,
    val expirySeconds: Int
)

// ══════════════════════════════════════════════════════════════════════════════
// STAGE 3: SESSION
// ══════════════════════════════════════════════════════════════════════════════
data class Session(
    val sessionId: String,              // UUID
    val conversationId: String,         // sort(A_id_hex, B_id_hex) + "_session"
    val contactId: String,
    val sessionKey: ByteArray,          // K₀ — ratchet root (zeroize after init)
    val establishedAt: Long,
    val isRelayed: Boolean,
    val connectionState: ConnectionState
)

enum class ConnectionState {
    CONNECTING,
    CONNECTED_P2P,
    CONNECTED_RELAY,
    DISCONNECTED,
    ERROR
}

// ══════════════════════════════════════════════════════════════════════════════
// STAGE 5: MESSAGE
// ══════════════════════════════════════════════════════════════════════════════
data class MessageHeader(
    val senderId: ByteArray,            // 32-byte identity hash
    val timestampMs: Long,
    val counter: Long,
    val expirySeconds: Int,             // 0 = no expiry
    val messageType: MessageType = MessageType.TEXT   // extensible for voice
)

enum class MessageType {
    TEXT,
    VOICE_NOTE,                         // Phase 2 — future voice messaging
    SYSTEM                              // key change notices etc.
}

data class Message(
    val id: String,                     // UUID
    val conversationId: String,
    val senderId: String,               // identity hash hex
    val content: String,                // decrypted plaintext (in-memory only)
    val timestampMs: Long,
    val counter: Long,
    val expirySeconds: Int,
    val expiryDeadlineMs: Long?,        // null = permanent
    val isOutgoing: Boolean,
    val state: MessageState,
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageState {
    SENDING,
    SENT,
    DELIVERED,
    FAILED,         // network send failed — eligible for retry
    REJECTED,       // signature/AEAD/timestamp/counter check failed
    EXPIRED
}

// Wire packet — what travels over the network
data class WirePacket(
    val headerBytes: ByteArray,         // JSON-serialized MessageHeader
    val ciphertext: ByteArray,          // encrypted payload + 16-byte poly1305 tag
    val signature: ByteArray            // 64-byte Ed25519 signature
)

// Relay envelope — outermost layer for the relay server
data class RelayEnvelope(
    val conversationId: String,
    val messageId: String,
    val packetBytes: ByteArray          // serialized WirePacket
)

// ══════════════════════════════════════════════════════════════════════════════
// CONVERSATION
// ══════════════════════════════════════════════════════════════════════════════
data class Conversation(
    val id: String,                     // conversationId
    val contact: Contact,
    val lastMessagePreview: String,     // always "🔒 Encrypted message" in UI
    val lastMessageAt: Long,
    val unreadCount: Int,
    val connectionState: ConnectionState
)

// ══════════════════════════════════════════════════════════════════════════════
// CRYPTO ERROR HIERARCHY
// ══════════════════════════════════════════════════════════════════════════════
sealed class CryptoError : Exception() {
    object SignatureInvalid : CryptoError()
    object TimestampStale : CryptoError()
    object ReplayDetected : CryptoError()
    object AEADAuthFailed : CryptoError()
    object TicketExpired : CryptoError()
    object TicketSignatureInvalid : CryptoError()
    object DhExchangeFailed : CryptoError()
    object InvalidPublicKey : CryptoError()
    object KeystoreError : CryptoError()
    object StorageTampered : CryptoError()  // storage_hash mismatch on read
    data class Unknown(override val cause: Throwable?) : CryptoError()
}

// ══════════════════════════════════════════════════════════════════════════════
// UTILITY EXTENSION
// ══════════════════════════════════════════════════════════════════════════════
fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
fun String.hexToByteArray(): ByteArray {
    check(length % 2 == 0) { "Hex string must have even length" }
    return ByteArray(length / 2) { i -> ((get(i * 2).digitToInt(16) shl 4) or get(i * 2 + 1).digitToInt(16)).toByte() }
}
