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
     * Securely zeroizes a byte array.
     * Use immediately after any sensitive material is no longer needed.
     */
    fun zeroize(key: ByteArray) = key.fill(0)
}
