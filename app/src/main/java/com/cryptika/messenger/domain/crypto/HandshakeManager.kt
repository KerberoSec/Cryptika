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
 * Two offer types:
 *   0x01 (PACKET_TYPE)             — 97 bytes:  type(1) + ephem_pubkey(32) + sig(64)
 *   0x02 (PACKET_TYPE_WITH_TICKET) — 301 bytes: type(1) + ephem_pubkey(32) + sig(64) + ticket(204)
 *
 * The initiator (lower identity hash) sends type 0x02 with the dual-signed ticket embedded.
 * The acceptor (higher identity hash) sends type 0x01 without a ticket.
 *
 * Signing digest:
 *   Type 0x01 — SHA-256(type || ephem_pubkey)             — 33 bytes committed
 *   Type 0x02 — SHA-256(type || ephem_pubkey || ticketHash) — 65 bytes committed
 *
 * Binding the ticketHash to the offer signature prevents a relay operator
 * from substituting a different valid ticket inside a type-0x02 offer.
 *
 * Session key derivation
 *   shared_secret = X25519(our_eph_priv, peer_eph_pub)
 *   K₀ = SHA-256(secret ∥ a_id ∥ b_id ∥ ticketHash ∥ ts)   when ticket present
 *   K₀ = SHA-256(secret ∥ id_min ∥ id_max)                  fallback (ephemeral sessions)
 */
@Singleton
class HandshakeManager @Inject constructor(
    private val identityKeyManager: IdentityKeyManager,
    private val sessionKeyManager: SessionKeyManager
) {
    companion object {
        /** First byte of a HANDSHAKE_OFFER without an embedded ticket. */
        const val PACKET_TYPE: Byte = 0x01

        /** First byte of a HANDSHAKE_OFFER with an embedded 204-byte dual-signed ticket. */
        const val PACKET_TYPE_WITH_TICKET: Byte = 0x02

        /** Byte count of a HANDSHAKE_OFFER without ticket: 1 + 32 + 64 */
        const val OFFER_SIZE = 97

        /** Byte count of a HANDSHAKE_OFFER with ticket: 97 + 204 */
        const val OFFER_WITH_TICKET_SIZE = 97 + 204   // 301
    }

    // OFFER CREATION

    /**
     * Generates a fresh ephemeral X25519 keypair, signs the offer, and returns
     * the serialized packet together with the keypair.
     *
     * @param verifiedTicket When provided (initiator path), a 301-byte offer (type 0x02)
     *                       is produced with the dual-signed ticket appended.
     *                       When null (acceptor path), a 97-byte offer (type 0x01) is produced.
     *
     * @return Pair of (serialized offer bytes, ephemeral keypair)
     */
    fun createOffer(
        verifiedTicket: VerifiedTicket? = null
    ): Pair<ByteArray, SessionKeyManager.EphemeralKeyPair> {
        val ephemeralPair = sessionKeyManager.generateEphemeralKeyPair()
        val packetType = if (verifiedTicket != null) PACKET_TYPE_WITH_TICKET else PACKET_TYPE

        // Type 0x01: sign SHA-256(type || ephem_pubkey)               — 33 bytes
        // Type 0x02: sign SHA-256(type || ephem_pubkey || ticketHash) — 65 bytes
        // Binding the ticketHash prevents substitution of a different ticket in the offer.
        val signedDataSize = if (verifiedTicket != null) 1 + 32 + 32 else 1 + 32
        val dataToSign = ByteArray(signedDataSize)
        dataToSign[0] = packetType
        ephemeralPair.publicKeyBytes.copyInto(dataToSign, destinationOffset = 1)
        verifiedTicket?.ticketHash?.copyInto(dataToSign, destinationOffset = 33)

        val signingDigest = MessageDigest.getInstance("SHA-256").digest(dataToSign)
        val signature = identityKeyManager.sign(signingDigest)

        return if (verifiedTicket != null) {
            val packet = ByteArray(OFFER_WITH_TICKET_SIZE)
            packet[0] = packetType
            ephemeralPair.publicKeyBytes.copyInto(packet, destinationOffset = 1)   // bytes 1..32
            signature.copyInto(packet, destinationOffset = 33)                      // bytes 33..96
            verifiedTicket.rawBytes.copyInto(packet, destinationOffset = 97)        // bytes 97..300
            packet to ephemeralPair
        } else {
            val packet = ByteArray(OFFER_SIZE)
            packet[0] = packetType
            ephemeralPair.publicKeyBytes.copyInto(packet, destinationOffset = 1)
            signature.copyInto(packet, destinationOffset = 33)
            packet to ephemeralPair
        }
    }

    // OFFER IDENTIFICATION

    /**
     * Returns true if [packetBytes] is a HANDSHAKE_OFFER (either type), distinguishing
     * it from a [MessageProcessor] wire packet.
     */
    fun isHandshakeOffer(packetBytes: ByteArray): Boolean =
        (packetBytes.size == OFFER_SIZE || packetBytes.size == OFFER_WITH_TICKET_SIZE) &&
        (packetBytes[0] == PACKET_TYPE || packetBytes[0] == PACKET_TYPE_WITH_TICKET)

    // TICKET EXTRACTION

    /**
     * Extracts and verifies the dual-signed ticket embedded in a type-0x02 offer.
     * Returns null if the offer is not type 0x02 (i.e. acceptor's offer).
     *
     * @param offerBytes     the full 301-byte offer
     * @param userAPublicKey 32-byte Ed25519 public key of the initiator (contact.publicKeyBytes)
     * @param ticketManager  used to verify both signatures and temporal checks
     */
    fun extractTicketFromOffer(
        offerBytes: ByteArray,
        userAPublicKey: ByteArray,
        ticketManager: TicketManager
    ): VerifiedTicket? {
        if (offerBytes.size != OFFER_WITH_TICKET_SIZE) return null
        val ticketBytes = offerBytes.copyOfRange(97, OFFER_WITH_TICKET_SIZE)
        return ticketManager.verifyTicket(ticketBytes, userAPublicKey)
    }

    // SESSION KEY DERIVATION

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
     *                             SHA-256(secret ∥ a_id ∥ b_id ∥ ticketHash ∥ ts)
     *                             When null, falls back to SHA-256(secret ∥ id_min ∥ id_max).
     *
     * @return Triple of (sessionKey_zeroed, sendRoot, recvRoot)
     * @throws CryptoError.SignatureInvalid if the Ed25519 offer signature is invalid
     */
    fun deriveSessionKey(
        offerBytes: ByteArray,
        peerIdentityPublicKey: ByteArray,
        ourEphemeralPair: SessionKeyManager.EphemeralKeyPair,
        myIdentityHash: ByteArray,
        peerIdentityHash: ByteArray,
        verifiedTicket: VerifiedTicket? = null
    ): Triple<ByteArray, ByteArray, ByteArray> {
        require(offerBytes.size == OFFER_SIZE || offerBytes.size == OFFER_WITH_TICKET_SIZE) {
            "Offer must be $OFFER_SIZE or $OFFER_WITH_TICKET_SIZE bytes, got ${offerBytes.size}"
        }
        require(offerBytes[0] == PACKET_TYPE || offerBytes[0] == PACKET_TYPE_WITH_TICKET) {
            "First byte is not a HANDSHAKE magic byte"
        }

        // Only the first 97 bytes contain the DH handshake data.
        val peerEphemeralPubKey = offerBytes.copyOfRange(1, 33)
        val signature = offerBytes.copyOfRange(33, 97)

        // Verify Ed25519 signature.
        // Type 0x01: signed data = type || ephem_pubkey (33 bytes)
        // Type 0x02: signed data = type || ephem_pubkey || ticketHash (65 bytes)
        // The ticketHash must be provided by the caller (extracted from the offer via
        // extractTicketFromOffer before calling deriveSessionKey).
        val signedData = if (offerBytes[0] == PACKET_TYPE_WITH_TICKET && verifiedTicket != null) {
            val buf = ByteArray(1 + 32 + 32)
            offerBytes.copyInto(buf, destinationOffset = 0, startIndex = 0, endIndex = 33)
            verifiedTicket.ticketHash.copyInto(buf, destinationOffset = 33)
            buf
        } else {
            offerBytes.copyOfRange(0, 33)
        }
        val signingDigest = MessageDigest.getInstance("SHA-256").digest(signedData)
        if (!Ed25519Verifier.verify(peerIdentityPublicKey, signingDigest, signature)) {
            throw CryptoError.SignatureInvalid
        }

        // X25519 shared secret: ephemeral private key is zeroized inside this call
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
