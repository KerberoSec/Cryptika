// domain/crypto/AEADCipher.kt
// Stage 5 — ChaCha20-Poly1305 AEAD Encryption
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * AEAD encryption using ChaCha20-Poly1305.
 *
 * Key: 32 bytes (ratchet key Kₙ)
 * Nonce: 12 bytes (derived deterministically — no randomness)
 * Additional Data: header_hash (32 bytes) — binds header to ciphertext
 * Output: ciphertext + 16-byte Poly1305 tag
 *
 * Deterministic nonce prevents nonce reuse which would be catastrophic for ChaCha20.
 */
object AEADCipher {

    private const val KEY_SIZE = 32
    private const val NONCE_SIZE = 12
    private const val TAG_SIZE = 16

    /**
     * Encrypts plaintext with ChaCha20-Poly1305.
     *
     * @param plaintext message bytes
     * @param key 32-byte ratchet key
     * @param nonce 12-byte deterministic nonce
     * @param additionalData header_hash (32 bytes) — authenticated but not encrypted
     * @return ciphertext || 16-byte Poly1305 tag
     */
    fun encrypt(
        plaintext: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        additionalData: ByteArray
    ): ByteArray {
        require(key.size == KEY_SIZE) { "Key must be $KEY_SIZE bytes, got ${key.size}" }
        require(nonce.size == NONCE_SIZE) { "Nonce must be $NONCE_SIZE bytes, got ${nonce.size}" }

        return try {
            val cipher = ChaCha20Poly1305()
            val parameters = AEADParameters(KeyParameter(key), TAG_SIZE * 8, nonce, additionalData)
            cipher.init(true, parameters)

            val output = ByteArray(cipher.getOutputSize(plaintext.size))
            var offset = cipher.processBytes(plaintext, 0, plaintext.size, output, 0)
            offset += cipher.doFinal(output, offset)
            output
        } catch (e: Exception) {
            throw CryptoError.Unknown(e)
        }
    }

    /**
     * Decrypts and authenticates ciphertext.
     *
     * @throws CryptoError.AEADAuthFailed if authentication tag is invalid
     */
    fun decrypt(
        ciphertext: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        additionalData: ByteArray
    ): ByteArray {
        require(key.size == KEY_SIZE) { "Key must be $KEY_SIZE bytes" }
        require(nonce.size == NONCE_SIZE) { "Nonce must be $NONCE_SIZE bytes" }
        require(ciphertext.size >= TAG_SIZE) { "Ciphertext too short to contain authentication tag" }

        return try {
            val cipher = ChaCha20Poly1305()
            val parameters = AEADParameters(KeyParameter(key), TAG_SIZE * 8, nonce, additionalData)
            cipher.init(false, parameters)

            val output = ByteArray(cipher.getOutputSize(ciphertext.size))
            var offset = cipher.processBytes(ciphertext, 0, ciphertext.size, output, 0)
            offset += cipher.doFinal(output, offset)
            output.copyOf(offset)
        } catch (e: org.bouncycastle.crypto.InvalidCipherTextException) {
            throw CryptoError.AEADAuthFailed
        } catch (e: Exception) {
            throw CryptoError.AEADAuthFailed
        }
    }

    /**
     * Derives a deterministic 12-byte nonce.
     * nonce = SHA-256(ratchetKey ∥ counter_bytes)[0:12]
     *
     * This is deterministic — given the same key and counter, always produces same nonce.
     * Since each key is used exactly ONCE, nonce uniqueness is guaranteed.
     */
    fun deriveNonce(ratchetKey: ByteArray, counter: Long): ByteArray {
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(ratchetKey)
        digest.update(counterBytes)
        return digest.digest().copyOf(NONCE_SIZE)
    }

    /**
     * Computes header hash for use as AEAD additional data.
     * header_hash = SHA-256(header_bytes)
     */
    fun computeHeaderHash(headerBytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(headerBytes)
}
