// domain/crypto/HashRatchet.kt
// Stage 4 — SHA-256 Hash Ratchet for Forward Secrecy
package com.cryptika.messenger.domain.crypto

import com.cryptika.messenger.domain.model.CryptoError
import java.security.MessageDigest

data class RatchetKey(
    val key: ByteArray,     // 32 bytes — this message's encryption key
    val counter: Long       // strictly monotonically increasing
) {
    fun zeroize() = key.fill(0)
}

/**
 * Hash ratchet providing forward secrecy.
 *
 * K₀ = session_key (root — never used for encryption)
 * K₁ = SHA-256(K₀) — first message key
 * Kₙ = SHA-256(Kₙ₋₁) — subsequent keys
 *
 * Once a key is advanced past, it is ZEROIZED and UNRECOVERABLE.
 * This provides forward secrecy: compromising Kₙ reveals nothing about K₁...Kₙ₋₁.
 */
class HashRatchet(initialKey: ByteArray) {

    companion object {
        const val MAX_LOOKAHEAD = 50
        const val LOOKAHEAD_TIMEOUT_MS = 30_000L
    }

    private var currentKey: ByteArray = initialKey.copyOf()
    private var counter: Long = 0L

    // Lookahead buffer for out-of-order messages
    // Maps counter → (key, cachedAtMs)
    private val lookaheadBuffer = mutableMapOf<Long, Pair<ByteArray, Long>>()

    /**
     * Advances the ratchet and returns the next key.
     * Previous key is ZEROIZED immediately — it cannot be recovered.
     */
    fun advance(): RatchetKey {
        val nextKey = sha256(currentKey)
        currentKey.fill(0)  // zeroize previous key
        currentKey = nextKey
        counter++
        return RatchetKey(key = nextKey.copyOf(), counter = counter)
    }

    /**
     * Returns the ratchet key for a specific counter position.
     * Used for out-of-order message reception.
     *
     * Checks lookahead buffer first for out-of-order keys.
     * If targetCounter > currentCounter: advances ratchet, caches intermediate keys.
     * If targetCounter <= currentCounter and not in lookahead: replay — throws ReplayDetected.
     */
    fun keyForCounter(targetCounter: Long): ByteArray {
        // Check lookahead buffer first — handles out-of-order arrivals
        lookaheadBuffer[targetCounter]?.let { (key, _) ->
            lookaheadBuffer.remove(targetCounter)
            evictExpiredLookahead()
            return key
        }

        // Not in lookahead and behind the ratchet — replay or already consumed
        if (targetCounter <= counter) {
            throw CryptoError.ReplayDetected
        }

        // Advance ratchet to target position, caching all intermediate keys
        // keysToCache must be declared here so it's in scope for the flush below
        val keysToCache = mutableListOf<Pair<Long, ByteArray>>()

        while (counter < targetCounter) {
            val ratchetKey = advance()  // increments counter
            if (counter < targetCounter) {
                // Intermediate key — cache for potential future out-of-order messages
                if (keysToCache.size < MAX_LOOKAHEAD) {
                    keysToCache.add(counter to ratchetKey.key.copyOf())
                }
                ratchetKey.zeroize()
            } else {
                // counter == targetCounter — this is the key we need
                // Flush intermediate keys to lookahead buffer BEFORE returning
                val now = System.currentTimeMillis()
                keysToCache.forEach { (c, k) ->
                    lookaheadBuffer[c] = k to now
                }
                evictExpiredLookahead()
                // Return a copy; caller is responsible for zeroizing
                return ratchetKey.key.copyOf().also { ratchetKey.zeroize() }
            }
        }

        // counter was already == targetCounter when we entered (edge case: first message)
        // This happens when targetCounter == 1 and counter is already 0 after init
        val ratchetKey = advance()
        val now = System.currentTimeMillis()
        keysToCache.forEach { (c, k) -> lookaheadBuffer[c] = k to now }
        return ratchetKey.key.copyOf().also { ratchetKey.zeroize() }
    }

    fun currentCounter(): Long = counter

    fun zeroizeAll() {
        currentKey.fill(0)
        lookaheadBuffer.values.forEach { (key, _) -> key.fill(0) }
        lookaheadBuffer.clear()
    }

    private fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)

    private fun evictExpiredLookahead() {
        val now = System.currentTimeMillis()
        val expired = lookaheadBuffer.entries
            .filter { (_, v) -> now - v.second > LOOKAHEAD_TIMEOUT_MS }
            .map { it.key }
        expired.forEach { counter ->
            lookaheadBuffer[counter]?.first?.fill(0)
            lookaheadBuffer.remove(counter)
        }
    }
}
