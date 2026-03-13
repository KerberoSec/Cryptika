// domain/crypto/MessageProcessor.kt
// Stages 4+5 — Full Send/Receive Pipeline
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import com.cryptika.messenger.domain.model.MessageHeader
import com.cryptika.messenger.domain.model.MessageType
import com.cryptika.messenger.domain.model.WirePacket
import com.cryptika.messenger.domain.model.toHexString
import com.google.gson.Gson
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * MessageProcessor handles the full send/receive cryptographic pipeline:
 *
 * Send:
 *   plaintext → header → nonce derivation → AEAD encrypt → Ed25519 sign → wire packet
 *
 * Receive (strict order — any step failure rejects message):
 *   1. Parse wire packet
 *   2. Verify Ed25519 signature
 *   3. Verify timestamp (±5 min)
 *   4. Verify counter (strictly > last seen)
 *   5. Advance ratchet to counter position
 *   6. Derive nonce
 *   7. AEAD decrypt with header_hash as additional data
 *   8. Return plaintext
 */
class MessageProcessor(
    private val sendRatchet: HashRatchet,
    private val recvRatchet: HashRatchet,
    private val identityKeyManager: IdentityKeyManager,
    private val peerPublicKeyBytes: ByteArray,      // 32-byte peer Ed25519 public key
    private val myIdentityHash: ByteArray,          // 32-byte SHA-256 of our public key
    private val gson: Gson = Gson()
) {
    companion object {
        const val TIMESTAMP_TOLERANCE_MS = 300_000L  // 5 minutes
    }

    private var highWaterRecvCounter: Long = 0L
    private val processedCounters = mutableSetOf<Long>()

    /** Zeroize all crypto material — called during cryptographic erasure. */
    fun zeroize() {
        sendRatchet.zeroizeAll()
        recvRatchet.zeroizeAll()
        peerPublicKeyBytes.fill(0)
        myIdentityHash.fill(0)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // SEND PIPELINE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Encrypts, signs, and serializes a plaintext message.
     *
     * @param plaintext UTF-8 message text
     * @param expirySeconds 0 = no expiry
     * @param messageType TEXT by default; VOICE_NOTE for Phase 2
     * @return serialized WirePacket bytes ready to send over network
     */
    fun send(
        plaintext: ByteArray,
        expirySeconds: Int = 0,
        messageType: MessageType = MessageType.TEXT
    ): Pair<ByteArray, Long> {
        // 1. Advance ratchet to get message key
        val ratchetKey = sendRatchet.advance()

        // 2. Build header
        val header = MessageHeader(
            senderId = myIdentityHash,
            timestampMs = System.currentTimeMillis(),
            counter = ratchetKey.counter,
            expirySeconds = expirySeconds,
            messageType = messageType
        )
        val headerBytes = serializeHeader(header)

        // 3. Compute header_hash for AEAD additional data
        val headerHash = AEADCipher.computeHeaderHash(headerBytes)

        // 4. Derive deterministic nonce
        val nonce = AEADCipher.deriveNonce(ratchetKey.key, ratchetKey.counter)

        // 5. AEAD encrypt
        val ciphertext = AEADCipher.encrypt(
            plaintext = plaintext,
            key = ratchetKey.key,
            nonce = nonce,
            additionalData = headerHash
        )

        // 6. Compute signing digest: SHA-256(header_bytes || ciphertext)
        val signingDigest = MessageDigest.getInstance("SHA-256").run {
            update(headerBytes)
            update(ciphertext)
            digest()
        }

        // 7. Sign with Ed25519 identity key
        val signature = identityKeyManager.sign(signingDigest)

        // Save counter BEFORE zeroizing (Long is a value type — safe, but be explicit)
        val msgCounter = ratchetKey.counter

        // 8. Zeroize ratchet key — do this exactly ONCE after all uses of the key are done
        ratchetKey.zeroize()

        // 9. Serialize to wire format
        val wirePacket = WirePacket(
            headerBytes = headerBytes,
            ciphertext = ciphertext,
            signature = signature
        )

        return serializeWirePacket(wirePacket) to msgCounter
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RECEIVE PIPELINE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Receives, verifies, and decrypts a wire packet.
     *
     * Strict validation order — any failure throws CryptoError and rejects message.
     *
     * @return Pair of (decrypted plaintext, MessageHeader)
     * @throws CryptoError on any validation failure
     */
    fun receive(packetBytes: ByteArray): Pair<ByteArray, MessageHeader> {
        // 1. Parse wire packet
        val wirePacket = deserializeWirePacket(packetBytes)
        val header = deserializeHeader(wirePacket.headerBytes)

        // 2. VERIFY SIGNATURE FIRST — before any other processing
        val signingDigest = MessageDigest.getInstance("SHA-256").run {
            update(wirePacket.headerBytes)
            update(wirePacket.ciphertext)
            digest()
        }
        if (!Ed25519Verifier.verify(peerPublicKeyBytes, signingDigest, wirePacket.signature)) {
            throw CryptoError.SignatureInvalid
        }

        // 3. VERIFY TIMESTAMP
        val now = System.currentTimeMillis()
        if (Math.abs(now - header.timestampMs) > TIMESTAMP_TOLERANCE_MS) {
            throw CryptoError.TimestampStale
        }

        // 4. VERIFY COUNTER — reject replays (already processed) and ancient counters
        if (header.counter in processedCounters) {
            throw CryptoError.ReplayDetected
        }
        if (header.counter <= highWaterRecvCounter - HashRatchet.MAX_LOOKAHEAD) {
            throw CryptoError.ReplayDetected
        }

        // 5. ADVANCE RATCHET to counter position (handles out-of-order via lookahead)
        val ratchetKey = recvRatchet.keyForCounter(header.counter)

        // 6. Derive nonce
        val nonce = AEADCipher.deriveNonce(ratchetKey, header.counter)

        // 7. Compute header_hash for AEAD verification
        val headerHash = AEADCipher.computeHeaderHash(wirePacket.headerBytes)

        // 8. AEAD DECRYPT — throws AEADAuthFailed on tamper
        // ratchetKey is zeroized in both success and failure paths
        val plaintext: ByteArray
        try {
            plaintext = AEADCipher.decrypt(
                ciphertext = wirePacket.ciphertext,
                key = ratchetKey,
                nonce = nonce,
                additionalData = headerHash
            )
        } finally {
            ratchetKey.fill(0)  // Always zeroize, even on exception
        }

        // 9. Update counter tracking ONLY on complete success (after AEAD passes)
        processedCounters.add(header.counter)
        if (header.counter > highWaterRecvCounter) {
            highWaterRecvCounter = header.counter
        }
        // Evict processed counters outside the lookahead window to bound memory
        processedCounters.removeAll { it <= highWaterRecvCounter - HashRatchet.MAX_LOOKAHEAD }

        return plaintext to header
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Serialization helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun serializeHeader(header: MessageHeader): ByteArray {
        val map = mapOf(
            "sid" to header.senderId.toHexString(),
            "ts" to header.timestampMs,
            "ctr" to header.counter,
            "exp" to header.expirySeconds,
            "type" to header.messageType.name
        )
        return gson.toJson(map).toByteArray(Charsets.UTF_8)
    }

    private fun deserializeHeader(bytes: ByteArray): MessageHeader {
        val map = gson.fromJson(String(bytes, Charsets.UTF_8), Map::class.java)
        return MessageHeader(
            senderId = (map["sid"] as String).hexToByteArray(),
            timestampMs = (map["ts"] as Double).toLong(),
            counter = (map["ctr"] as Double).toLong(),
            expirySeconds = (map["exp"] as Double).toInt(),
            messageType = try {
                MessageType.valueOf(map["type"] as? String ?: "TEXT")
            } catch (_: Exception) { MessageType.TEXT }
        )
    }

    /**
     * Wire packet binary format:
     * [4 bytes big-endian: header_length]
     * [header_length bytes: JSON header]
     * [4 bytes big-endian: ciphertext_length]
     * [ciphertext_length bytes: ciphertext + poly1305 tag]
     * [64 bytes: Ed25519 signature]
     */
    fun serializeWirePacket(packet: WirePacket): ByteArray {
        val buf = ByteBuffer.allocate(
            4 + packet.headerBytes.size + 4 + packet.ciphertext.size + 64
        )
        buf.putInt(packet.headerBytes.size)
        buf.put(packet.headerBytes)
        buf.putInt(packet.ciphertext.size)
        buf.put(packet.ciphertext)
        buf.put(packet.signature)
        return buf.array()
    }

    fun deserializeWirePacket(bytes: ByteArray): WirePacket {
        if (bytes.size < 4 + 1 + 4 + 1 + 64) {
            throw CryptoError.Unknown(IllegalArgumentException("Packet too short: ${bytes.size} bytes"))
        }
        val buf = ByteBuffer.wrap(bytes)
        val headerLen = buf.int
        if (headerLen <= 0 || headerLen > buf.remaining() - (4 + 64)) {
            throw CryptoError.Unknown(IllegalArgumentException("Invalid header length: $headerLen"))
        }
        val headerBytes = ByteArray(headerLen).also { buf.get(it) }
        val ciphertextLen = buf.int
        if (ciphertextLen <= 0 || ciphertextLen > buf.remaining() - 64) {
            throw CryptoError.Unknown(IllegalArgumentException("Invalid ciphertext length: $ciphertextLen"))
        }
        val ciphertext = ByteArray(ciphertextLen).also { buf.get(it) }
        if (buf.remaining() < 64) {
            throw CryptoError.Unknown(IllegalArgumentException("Missing signature"))
        }
        val signature = ByteArray(64).also { buf.get(it) }
        return WirePacket(headerBytes, ciphertext, signature)
    }

    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0)
        return ByteArray(length / 2) { i ->
            ((get(i * 2).digitToInt(16) shl 4) or get(i * 2 + 1).digitToInt(16)).toByte()
        }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
}
