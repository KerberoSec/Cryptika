// domain/crypto/HandshakeManager.kt
// Ephemeral X25519 DH handshake for session establishment
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import com.cryptika.messenger.domain.model.VerifiedTicket
import com.cryptika.messenger.domain.model.toHexString
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the ephemeral X25519 Diffie-Hellman handshake protocol.
 *
 * Both peers independently send a HANDSHAKE_OFFER when they connect to the relay.
 * On receiving the peer's offer, each side derives the shared session key and the
 * encrypted conversation can begin.
 *
 * ── Handshake offer wire format (97 bytes) ──────────────────────────────────
 *   [1]    magic = 0x01  — distinguishes from WirePacket (which always starts with 0x00,
 *                          since its first 4 bytes encode headerLen whose high byte is 0x00
 *                          for any realistic JSON header < 16 million bytes)
 *   [32]   ephemeral X25519 public key (generated fresh per WebSocket connection)
 *   [64]   Ed25519 signature of SHA-256(bytes[0..32])
 *
 * ── Session key derivation ───────────────────────────────────────────────────
 *   shared_secret = X25519(our_eph_priv, peer_eph_pub)   // symmetric: A⊗B == B⊗A
 *   (id_min, id_max) = sorted-by-hex(my_identity_hash, peer_identity_hash)
 *   session_key = SHA-256(shared_secret ‖ id_min ‖ id_max)
 *
 * Sorting the identity hashes ensures both sides compute the identical key
 * regardless of who sends their offer first.
 */
@Singleton
class HandshakeManager @Inject constructor(
    private val identityKeyManager: IdentityKeyManager,
    private val sessionKeyManager: SessionKeyManager
) {
    companion object {
        /** First byte of every HANDSHAKE_OFFER — cannot appear as the high byte of a
         *  WirePacket headerLen for any header < 16,777,216 bytes (impossible in practice). */
        const val PACKET_TYPE: Byte = 0x01

        /** Exact byte count of a HANDSHAKE_OFFER: 1 + 32 + 64 */
        const val OFFER_SIZE = 97
    }

    // ──────────────────────────────────────────────────────────────────────────
    // OFFER CREATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generates a fresh ephemeral X25519 keypair, signs the offer, and returns
     * the serialized 97-byte packet together with the keypair.
     *
     * Call exactly once per new WebSocket connection.
     * Hold the returned [EphemeralKeyPair] until [deriveSessionKey] is called.
     *
     * @return Pair of (serialized offer bytes, ephemeral keypair)
     */
    fun createOffer(): Pair<ByteArray, SessionKeyManager.EphemeralKeyPair> {
        val ephemeralPair = sessionKeyManager.generateEphemeralKeyPair()

        // Signed data: type || ephemeral_pubkey  (33 bytes)
        val dataToSign = ByteArray(1 + 32)
        dataToSign[0] = PACKET_TYPE
        ephemeralPair.publicKeyBytes.copyInto(dataToSign, destinationOffset = 1)

        val signingDigest = MessageDigest.getInstance("SHA-256").digest(dataToSign)
        val signature = identityKeyManager.sign(signingDigest)

        val packet = ByteArray(OFFER_SIZE)
        packet[0] = PACKET_TYPE
        ephemeralPair.publicKeyBytes.copyInto(packet, destinationOffset = 1)  // bytes 1..32
        signature.copyInto(packet, destinationOffset = 33)                     // bytes 33..96

        return packet to ephemeralPair
    }

    // ──────────────────────────────────────────────────────────────────────────
    // OFFER IDENTIFICATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if [packetBytes] has the correct size and magic byte for a
     * HANDSHAKE_OFFER, distinguishing it from a [MessageProcessor] wire packet.
     */
    fun isHandshakeOffer(packetBytes: ByteArray): Boolean =
        packetBytes.size == OFFER_SIZE && packetBytes[0] == PACKET_TYPE

    // ──────────────────────────────────────────────────────────────────────────
    // SESSION KEY DERIVATION
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Verifies a received HANDSHAKE_OFFER and derives the shared 32-byte session key.
     *
     * @param offerBytes           the 97-byte offer received over the relay
     * @param peerIdentityPublicKey peer's Ed25519 public key (32 bytes, from [Contact])
     * @param ourEphemeralPair     our ephemeral keypair from [createOffer] — its private
     *                             key bytes are zeroized inside this call
     * @param myIdentityHash       our 32-byte SHA-256 identity hash
     * @param peerIdentityHash     peer's 32-byte SHA-256 identity hash
     * @return derived 32-byte session key — CALLER must pass it to [onSessionEstablished],
     *         which zeroizes it after creating the hash ratchets
     * @throws CryptoError.SignatureInvalid if the Ed25519 offer signature is invalid
     */
    /**
     * @param verifiedTicket when provided, the full K₀ formula is used:
     *   SHA-256(secret ∥ a_id ∥ b_id ∥ ticketHash ∥ ts)
     *   where a_id/b_id come from the server-signed ticket (canonical order).
     *   When null, falls back to SHA-256(secret ∥ id_min ∥ id_max).
     *
     * @return Triple of (sessionKey, sendRoot, recvRoot)
     */
    fun deriveSessionKey(
        offerBytes: ByteArray,
        peerIdentityPublicKey: ByteArray,
        ourEphemeralPair: SessionKeyManager.EphemeralKeyPair,
        myIdentityHash: ByteArray,
        peerIdentityHash: ByteArray,
        verifiedTicket: VerifiedTicket? = null
    ): Triple<ByteArray, ByteArray, ByteArray> {
        require(offerBytes.size == OFFER_SIZE) { "Offer must be $OFFER_SIZE bytes, got ${offerBytes.size}" }
        require(offerBytes[0] == PACKET_TYPE) { "First byte is not HANDSHAKE magic" }

        val peerEphemeralPubKey = offerBytes.copyOfRange(1, 33)
        val signature = offerBytes.copyOfRange(33, 97)

        // Verify Ed25519 signature over (type || ephemeral_pubkey)
        val signedData = offerBytes.copyOfRange(0, 33)
        val signingDigest = MessageDigest.getInstance("SHA-256").digest(signedData)
        if (!Ed25519Verifier.verify(peerIdentityPublicKey, signingDigest, signature)) {
            throw CryptoError.SignatureInvalid
        }

        // X25519 shared secret — ephemeral private key is zeroized inside this call
        val sharedSecret = sessionKeyManager.computeSharedSecret(ourEphemeralPair, peerEphemeralPubKey)

        // Reject all-zero shared secret (low-order public key attack)
        if (sharedSecret.all { it == 0.toByte() }) {
            sharedSecret.fill(0)
            throw CryptoError.DhExchangeFailed
        }

        val sessionKey = if (verifiedTicket != null) {
            sessionKeyManager.deriveSessionKey(
                sharedSecret  = sharedSecret,
                aIdentityHash = verifiedTicket.aId,
                bIdentityHash = verifiedTicket.bId,
                ticketHash    = verifiedTicket.ticketHash,
                timestampMs   = verifiedTicket.timestamp
            ).also { sharedSecret.fill(0) }
        } else {
            val myHex = myIdentityHash.toHexString()
            val peerHex = peerIdentityHash.toHexString()
            val (firstHash, secondHash) = if (myHex < peerHex)
                myIdentityHash to peerIdentityHash
            else
                peerIdentityHash to myIdentityHash

            MessageDigest.getInstance("SHA-256").run {
                update(sharedSecret)
                update(firstHash)
                update(secondHash)
                digest()
            }.also { sharedSecret.fill(0) }
        }

        // Derive direction-separated ratchet roots
        val (sendRoot, recvRoot) = sessionKeyManager.deriveDirectionalRoots(
            sessionKey, myIdentityHash, peerIdentityHash
        )
        sessionKey.fill(0) // zeroize undifferentiated root

        return Triple(sessionKey, sendRoot, recvRoot)
    }
}
