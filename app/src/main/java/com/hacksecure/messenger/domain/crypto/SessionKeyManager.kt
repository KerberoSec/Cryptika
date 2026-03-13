// domain/crypto/SessionKeyManager.kt
// Stage 3 — X25519 DH + Session Key Derivation
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages X25519 ephemeral key exchange and session key derivation.
 *
 * The ephemeral key pair is generated once per session.
 * Private key is ZEROIZED immediately after shared secret computation.
 */
class SessionKeyManager {

    /** Holds the ephemeral keypair during DH exchange */
    data class EphemeralKeyPair(
        val publicKeyBytes: ByteArray,      // 32 bytes — sent to peer
        private val _privateKeyBytes: ByteArray    // 32 bytes — NEVER transmitted, zeroized after DH
    ) {
        fun getPrivateKey(): ByteArray = _privateKeyBytes.copyOf()
        fun zeroizePrivate() = _privateKeyBytes.fill(0)
    }

    /**
     * Generates a fresh X25519 ephemeral keypair for this session.
     * Call this once per session handshake.
     */
    fun generateEphemeralKeyPair(): EphemeralKeyPair {
        val random = SecureRandom()
        val generator = X25519KeyPairGenerator()
        generator.init(X25519KeyGenerationParameters(random))
        val keyPair = generator.generateKeyPair()

        val privateParams = keyPair.private as X25519PrivateKeyParameters
        val publicParams = keyPair.public as X25519PublicKeyParameters

        return EphemeralKeyPair(
            publicKeyBytes = publicParams.encoded,
            _privateKeyBytes = privateParams.encoded
        )
    }

    /**
     * Computes X25519 shared secret from our ephemeral private key and peer's ephemeral public key.
     * ZEROIZES the ephemeral private key immediately after computing shared secret.
     *
     * @param ephemeralKeyPair our ephemeral keypair
     * @param peerPublicKeyBytes peer's 32-byte ephemeral public key
     * @return 32-byte shared secret (caller must zeroize after use)
     */
    fun computeSharedSecret(
        ephemeralKeyPair: EphemeralKeyPair,
        peerPublicKeyBytes: ByteArray
    ): ByteArray {
        require(peerPublicKeyBytes.size == 32) { "X25519 public key must be 32 bytes" }

        val privateKeyBytes = ephemeralKeyPair.getPrivateKey()
        return try {
            val privateParams = X25519PrivateKeyParameters(privateKeyBytes, 0)
            val peerPublicParams = X25519PublicKeyParameters(peerPublicKeyBytes, 0)

            val sharedSecret = ByteArray(32)
            privateParams.generateSecret(peerPublicParams, sharedSecret, 0)
            sharedSecret
        } catch (e: Exception) {
            throw CryptoError.DhExchangeFailed
        } finally {
            privateKeyBytes.fill(0)
            ephemeralKeyPair.zeroizePrivate()
        }
    }

    /**
     * Derives the session key (K₀) from DH output + all bound parameters.
     *
     * K₀ = SHA-256(sharedSecret ∥ A_id ∥ B_id ∥ ticketHash ∥ timestampMs)
     *
     * Both A_id and B_id are bound to prevent server from swapping one participant.
     * ticketHash binds the server-authenticated session context.
     *
     * @return 32-byte session key (K₀) — ratchet root
     */
    fun deriveSessionKey(
        sharedSecret: ByteArray,
        aIdentityHash: ByteArray,
        bIdentityHash: ByteArray,
        ticketHash: ByteArray,
        timestampMs: Long
    ): ByteArray {
        require(sharedSecret.size == 32) { "Shared secret must be 32 bytes" }
        require(aIdentityHash.size == 32) { "A identity hash must be 32 bytes" }
        require(bIdentityHash.size == 32) { "B identity hash must be 32 bytes" }
        require(ticketHash.size == 32) { "Ticket hash must be 32 bytes" }

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(sharedSecret)
        digest.update(aIdentityHash)
        digest.update(bIdentityHash)
        digest.update(ticketHash)
        digest.update(ByteBuffer.allocate(8).putLong(timestampMs).array())
        return digest.digest()
    }

    /**
     * Derives direction-separated ratchet roots from a session key.
     *
     * Both peers MUST agree on who is "A" and who is "B" (smaller identity hash = A).
     * A's send root = B's receive root and vice versa, preventing key/nonce reuse.
     *
     * sendRoot = SHA-256(sessionKey ∥ "SEND" ∥ myIdentityHash)
     * recvRoot = SHA-256(sessionKey ∥ "RECV" ∥ myIdentityHash)
     *
     * Since both peers use their own identity hash with complementary labels,
     * A_send = SHA-256(K ∥ "SEND" ∥ A_id) == B_recv because B computes
     * SHA-256(K ∥ "RECV" ∥ B_id)... — NO. Instead, use canonical roles:
     *
     * roleA_root = SHA-256(sessionKey ∥ "A_TO_B")
     * roleB_root = SHA-256(sessionKey ∥ "B_TO_A")
     *
     * The peer with the lexicographically smaller identity hash is "A".
     *
     * @return Pair of (sendRoot, recvRoot) — each 32 bytes
     */
    fun deriveDirectionalRoots(
        sessionKey: ByteArray,
        myIdentityHash: ByteArray,
        peerIdentityHash: ByteArray
    ): Pair<ByteArray, ByteArray> {
        require(sessionKey.size == 32) { "Session key must be 32 bytes" }
        val myHex = myIdentityHash.joinToString("") { "%02x".format(it) }
        val peerHex = peerIdentityHash.joinToString("") { "%02x".format(it) }
        val iAmA = myHex < peerHex // lexicographic ordering determines role

        val aToBRoot = MessageDigest.getInstance("SHA-256").run {
            update(sessionKey)
            update("A_TO_B".toByteArray(Charsets.UTF_8))
            digest()
        }
        val bToARoot = MessageDigest.getInstance("SHA-256").run {
            update(sessionKey)
            update("B_TO_A".toByteArray(Charsets.UTF_8))
            digest()
        }

        return if (iAmA) {
            aToBRoot to bToARoot   // A sends on A_TO_B, receives on B_TO_A
        } else {
            bToARoot to aToBRoot   // B sends on B_TO_A, receives on A_TO_B
        }
    }

    /**
     * Securely zeroizes a byte array.
     * Use immediately after any sensitive material is no longer needed.
     */
    fun zeroize(key: ByteArray) = key.fill(0)
}
