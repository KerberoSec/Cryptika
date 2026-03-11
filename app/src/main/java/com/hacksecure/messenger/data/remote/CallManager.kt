// data/remote/CallManager.kt
// Encrypted voice calls over the relay WebSocket.
package com.cryptika.messenger.data.remote

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import com.cryptika.messenger.domain.crypto.AEADCipher
import com.cryptika.messenger.domain.crypto.Ed25519Verifier
import com.cryptika.messenger.domain.crypto.IdentityKeyManager
import com.cryptika.messenger.domain.crypto.SessionKeyManager
import com.cryptika.messenger.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the complete lifecycle of an encrypted voice call.
 *
 * ── Architecture ──────────────────────────────────────────────────────────────
 * Calls use the existing relay WebSocket (same WebSocket as messaging) by prefixing
 * packets with distinct magic bytes so BackgroundConnectionManager can route them here.
 *
 * Two magic bytes are reserved:
 *   0x02 — CALL_SIGNAL : call control (OFFER / ANSWER / REJECT / HANGUP / BUSY)
 *   0x03 — AUDIO_FRAME : encrypted PCM audio sent during an active call
 *
 * ── Signal packet format (122 bytes — fixed size) ─────────────────────────────
 *   [0]      0x02 magic
 *   [1]      type byte (see CallSignalType)
 *   [2..17]  call_id — 16 random bytes identifying this call instance
 *   [18..25] timestamp_ms — big-endian long: replay protection (±5 min skew)
 *   [26..57] ephemeral X25519 public key — 32 bytes (zeroed for REJECT/HANGUP/BUSY)
 *   [58..121] Ed25519 signature over SHA-256(bytes[0..57]) — 64 bytes
 * Total: 1 + 1 + 16 + 8 + 32 + 64 = 122 bytes
 *
 * ── Audio frame format ────────────────────────────────────────────────────────
 *   [0]      0x03 magic
 *   [1..4]   sequence_number — big-endian int (replay / ordering)
 *   [5..16]  nonce — 12 bytes = SHA-256(encryptKey ∥ seqBytes)[0..11]
 *   [17..N]  ChaCha20-Poly1305(plaintext=pcm, key=encryptKey, nonce, ad=empty) + 16-byte tag
 *
 * ── Call key derivation ──────────────────────────────────────────────────────
 *   sharedSecret = X25519(ourEphPriv, peerEphPub)    — symmetric both sides
 *   callerEncKey = SHA-256(secret ∥ "caller_send" ∥ callIdBytes)
 *   calleeEncKey = SHA-256(secret ∥ "callee_send" ∥ callIdBytes)
 *
 *   Caller  → encryptKey = callerEncKey, decryptKey = calleeEncKey
 *   Callee  → encryptKey = calleeEncKey, decryptKey = callerEncKey
 *
 *   Using direction-specific keys ensures nonces are never reused even though
 *   both sides use the same sequence counter starting at 0.
 *
 * ── Audio parameters ──────────────────────────────────────────────────────────
 *   Sample rate : 16 000 Hz (narrow-band voice)
 *   Channels    : mono
 *   Encoding    : 16-bit PCM
 *   Frame size  : 160 samples = 10 ms → 320 bytes raw PCM per frame
 *   Encrypted   : 320 + 16 (tag) = 336 bytes per frame
 *   Wire size   : 1 + 4 + 12 + 336 = 353 bytes ≈ 35.3 KB/s at 100 fps
 */
@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backgroundConnectionManager: BackgroundConnectionManager,
    private val identityKeyManager: IdentityKeyManager,
    private val sessionKeyManager: SessionKeyManager
) {

    companion object {
        private const val TAG = "CallManager"
        /** Magic byte at position [0] of every call-signal packet. */
        const val CALL_SIGNAL_MAGIC: Byte = 0x02
        /** Magic byte at position [0] of every audio-frame packet. */
        const val AUDIO_FRAME_MAGIC: Byte = 0x03
        /** Fixed byte-length of a call signal packet. */
        const val SIGNAL_SIZE = 122

        // Audio parameters
        private const val SAMPLE_RATE     = 16_000       // Hz
        private const val FRAME_SAMPLES   = 160          // 10 ms at 16 kHz
        private const val FRAME_BYTES     = FRAME_SAMPLES * 2  // 16-bit mono PCM = 320 bytes

        // Inactivity timeout: auto-cancel OUTGOING ring after 60 s
        private const val RING_TIMEOUT_MS = 60_000L
        // Audio watchdog: end call if no audio received from peer for this long
        private const val AUDIO_WATCHDOG_MS = 15_000L
    }

    // ── Coroutine scope ───────────────────────────────────────────────────────

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val random = SecureRandom()

    // ── Observable state ──────────────────────────────────────────────────────

    private val _callState = MutableStateFlow(CallState.IDLE)
    val callState: StateFlow<CallState> = _callState

    /** Non-null while an incoming call is ringing; cleared after accept/reject/hangup. */
    private val _incomingCallData = MutableStateFlow<IncomingCallData?>(null)
    val incomingCallData: StateFlow<IncomingCallData?> = _incomingCallData

    // ── Active call context ───────────────────────────────────────────────────

    @Volatile private var activeCallId: String? = null
    @Volatile private var activeConvId: String? = null
    @Volatile private var activeContact: Contact? = null       // peer's Contact, for sig verification
    @Volatile private var isCallerRole: Boolean = false        // true = we sent OFFER, false = we sent ANSWER
    @Volatile private var pendingOfferEphPub: ByteArray? = null // peer's eph key from OFFER (for callee DH)
    @Volatile private var ourEphemeralPair: SessionKeyManager.EphemeralKeyPair? = null

    // Per-direction keys (prevent nonce reuse between caller↔callee streams)
    @Volatile private var encryptKey: ByteArray? = null
    @Volatile private var decryptKey: ByteArray? = null

    // ── Audio objects ─────────────────────────────────────────────────────────

    @Volatile private var audioRecord: AudioRecord? = null
    @Volatile private var audioTrack: AudioTrack? = null
    @Volatile private var captureJob: Job? = null
    @Volatile private var playbackJob: Job? = null
    @Volatile private var ringTimeoutJob: Job? = null
    @Volatile private var watchdogJob: Job? = null
    // AtomicInteger makes the sequence counter thread-safe without synchronization overhead
    private val sendSequenceAtomic = AtomicInteger(0)
    @Volatile private var lastAudioRxMs: Long = 0L
    @Volatile private var isMuted: Boolean = false
    private val cleanupLock = Any()
    // Channel for ordered audio playback — single consumer ensures AudioTrack thread safety
    private var audioPlaybackChannel = Channel<ByteArray>(capacity = 64)

    // ── Speaker state (AudioManager) ──────────────────────────────────────────
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn
    private val _isMutedState = MutableStateFlow(false)
    val isMutedState: StateFlow<Boolean> = _isMutedState

    // ═════════════════════════════════════════════════════════════════════════
    // PUBLIC API — called by CallViewModel
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Initiates an outgoing call to the contact owning [convId].
     * Sends a signed CALL_OFFER packet over the relay; starts a 60 s ring timeout.
     */
    fun startCall(convId: String, contact: Contact) {
        if (_callState.value != CallState.IDLE) return

        scope.launch {
            try {
                val callId = generateCallId()
                activeCallId = callId
                activeConvId = convId
                activeContact = contact
                isCallerRole = true

                val ephemeral = sessionKeyManager.generateEphemeralKeyPair()
                ourEphemeralPair = ephemeral

                val packet = buildSignalPacket(CallSignalType.OFFER, callId, ephemeral.publicKeyBytes)
                backgroundConnectionManager.sendPacket(convId, "call_offer_${UUID.randomUUID()}", packet)

                _callState.value = CallState.OUTGOING_RINGING

                // Auto-cancel after timeout
                ringTimeoutJob = launch {
                    delay(RING_TIMEOUT_MS)
                    if (_callState.value == CallState.OUTGOING_RINGING) hangup()
                }
            } catch (_: Exception) {
                cleanup()
            }
        }
    }

    /**
     * Accepts the pending incoming call.
     * Performs the callee side of the ephemeral X25519 DH and starts audio I/O.
     * Must only be called when [callState] == [CallState.INCOMING_RINGING].
     */
    fun answerCall() {
        if (_callState.value != CallState.INCOMING_RINGING) return
        val convId = activeConvId ?: return
        val callerEphPub = pendingOfferEphPub ?: return

        scope.launch {
            try {
                val ephemeral = sessionKeyManager.generateEphemeralKeyPair()
                ourEphemeralPair = ephemeral

                // Derive call keys — callee side
                val sharedSecret = sessionKeyManager.computeSharedSecret(ephemeral, callerEphPub)
                val callId = activeCallId ?: return@launch
                val (enc, dec) = deriveCallKeys(sharedSecret, callId, isCaller = false)
                encryptKey = enc
                decryptKey = dec
                sharedSecret.fill(0)
                ephemeral.zeroizePrivate()
                ourEphemeralPair = null

                val packet = buildSignalPacket(CallSignalType.ANSWER, callId, ephemeral.publicKeyBytes)
                backgroundConnectionManager.sendPacket(convId, "call_answer_${UUID.randomUUID()}", packet)

                pendingOfferEphPub = null
                _callState.value = CallState.ACTIVE
                startAudio()
            } catch (e: Exception) {
                Log.e(TAG, "answerCall failed", e)
                cleanup()
            }
        }
    }

    /**
     * Rejects the pending incoming call.
     * Sends REJECT and returns to IDLE immediately.
     */
    fun rejectCall() {
        if (_callState.value != CallState.INCOMING_RINGING) return
        val convId = activeConvId ?: run { cleanup(); return }
        val callId = activeCallId ?: run { cleanup(); return }

        scope.launch {
            try {
                val packet = buildSignalPacket(CallSignalType.REJECT, callId, ByteArray(32))
                backgroundConnectionManager.sendPacket(convId, "call_reject_${UUID.randomUUID()}", packet)
            } catch (_: Exception) {}
            cleanup()
        }
    }

    /** Ends the active call or cancels an outgoing/incoming ringing call. */
    fun hangup() {
        if (_callState.value == CallState.IDLE) return   // already ended — nothing to do
        val convId = activeConvId ?: run { cleanup(); return }
        val callId = activeCallId ?: run { cleanup(); return }

        scope.launch {
            try {
                val packet = buildSignalPacket(CallSignalType.HANGUP, callId, ByteArray(32))
                backgroundConnectionManager.sendPacket(convId, "call_hangup_${UUID.randomUUID()}", packet)
            } catch (_: Exception) {}
            cleanup()
        }
    }

    fun toggleMute() {
        isMuted = !isMuted
        _isMutedState.value = isMuted
    }

    fun toggleSpeaker() {
        val on = !_isSpeakerOn.value
        audioManager.isSpeakerphoneOn = on
        _isSpeakerOn.value = on
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PACKET ROUTING — called by BackgroundConnectionManager
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Entry point for all call-related relay packets.
     * Called by [BackgroundConnectionManager] whenever a packet with magic byte 0x02 or 0x03 arrives.
     *
     * @param contact the Contact associated with this conversation (for signature verification)
     */
    fun onRelayPacket(convId: String, msgId: String, packet: ByteArray, contact: Contact) {
        when {
            packet.isNotEmpty() && packet[0] == CALL_SIGNAL_MAGIC ->
                onSignalReceived(convId, packet, contact)
            packet.isNotEmpty() && packet[0] == AUDIO_FRAME_MAGIC ->
                onAudioFrameReceived(packet)
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SIGNAL HANDLING
    // ═════════════════════════════════════════════════════════════════════════

    private fun onSignalReceived(convId: String, packet: ByteArray, contact: Contact) {
        if (packet.size != SIGNAL_SIZE) return

        val typeByte = packet[1]
        val type = CallSignalType.fromByte(typeByte) ?: return

        // Parse fields
        val callIdBytes = packet.copyOfRange(2, 18)
        val callId = callIdBytes.toHexString()
        val buf = ByteBuffer.wrap(packet, 18, 8)
        val tsMs = buf.long
        val ephPub = packet.copyOfRange(26, 58)
        val signature = packet.copyOfRange(58, 122)

        // Ed25519 signature verification — signed data is bytes[0..57]
        val signedData = packet.copyOfRange(0, 58)
        val sigDigest = MessageDigest.getInstance("SHA-256").digest(signedData)
        if (!Ed25519Verifier.verify(contact.publicKeyBytes, sigDigest, signature)) return

        // Timestamp freshness ±5 minutes
        val now = System.currentTimeMillis()
        if (kotlin.math.abs(now - tsMs) > 5 * 60_000L) return

        when (type) {
            CallSignalType.OFFER  -> handleIncomingOffer(convId, callId, ephPub, contact)
            CallSignalType.ANSWER -> handleAnswer(callId, ephPub)
            CallSignalType.REJECT -> handleRejectOrHangup(callId)
            CallSignalType.HANGUP -> handleRejectOrHangup(callId)
            CallSignalType.BUSY   -> handleRejectOrHangup(callId)
        }
    }

    private fun handleIncomingOffer(convId: String, callId: String, callerEphPub: ByteArray, contact: Contact) {
        if (_callState.value != CallState.IDLE) {
            // Already busy — send BUSY and discard
            scope.launch {
                try {
                    val packet = buildSignalPacket(CallSignalType.BUSY, callId, ByteArray(32))
                    backgroundConnectionManager.sendPacket(convId, "call_busy_${UUID.randomUUID()}", packet)
                } catch (_: Exception) {}
            }
            return
        }

        activeCallId = callId
        activeConvId = convId
        activeContact = contact
        isCallerRole = false
        pendingOfferEphPub = callerEphPub.copyOf()

        _incomingCallData.value = IncomingCallData(
            convId = convId,
            contactId = contact.id,
            contactName = contact.displayName,
            callId = callId
        )
        _callState.value = CallState.INCOMING_RINGING
    }

    private fun handleAnswer(callId: String, calleeEphPub: ByteArray) {
        if (_callState.value != CallState.OUTGOING_RINGING) return
        if (callId != activeCallId) return

        ringTimeoutJob?.cancel()
        ringTimeoutJob = null

        scope.launch {
            try {
                val ephemeral = ourEphemeralPair ?: return@launch
                // Derive call keys — caller side
                val sharedSecret = sessionKeyManager.computeSharedSecret(ephemeral, calleeEphPub)
                val (enc, dec) = deriveCallKeys(sharedSecret, callId, isCaller = true)
                encryptKey = enc
                decryptKey = dec
                sharedSecret.fill(0)
                ephemeral.zeroizePrivate()   // wipe private key bytes now that DH is done
                ourEphemeralPair = null

                _callState.value = CallState.ACTIVE
                startAudio()
            } catch (e: Exception) {
                Log.e(TAG, "handleAnswer failed", e)
                cleanup()
            }
        }
    }

    private fun handleRejectOrHangup(incomingCallId: String) {
        // Ignore stale signals that belong to a previous call
        if (incomingCallId != activeCallId) return
        when (_callState.value) {
            CallState.OUTGOING_RINGING,
            CallState.INCOMING_RINGING,
            CallState.ACTIVE -> scope.launch { cleanup() }
            else -> {}
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // AUDIO
    // ═════════════════════════════════════════════════════════════════════════

    private fun startAudio() {
        sendSequenceAtomic.set(0)
        lastAudioRxMs = System.currentTimeMillis()   // baseline for watchdog

        // Start foreground service so Android keeps AudioRecord/AudioTrack alive in background.
        // Without this the OS kills audio capture within seconds of the app being minimized.
        try {
            CallForegroundService.start(context, activeContact?.displayName ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start call foreground service", e)
            // Continue — audio will still work while app is in foreground
        }

        // Set audio mode for voice communication (enables hardware echo cancellation)
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        // Default to speakerphone so both sides hear each other clearly
        audioManager.isSpeakerphoneOn = true
        _isSpeakerOn.value = true

        // ── AudioTrack (playback) ──────────────────────────────────────────────
        val minTrackBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minTrackBuf * 4, FRAME_BYTES * 8))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AudioTrack", e)
            cleanup()
            return
        }

        // ── Playback coroutine — single consumer for ordered AudioTrack writes ──
        audioPlaybackChannel = Channel(capacity = 64)
        playbackJob = scope.launch(Dispatchers.IO) {
            val track = audioTrack ?: return@launch
            try {
                for (pcm in audioPlaybackChannel) {
                    if (_callState.value != CallState.ACTIVE) break
                    try {
                        track.write(pcm, 0, pcm.size)
                    } catch (e: Exception) {
                        Log.w(TAG, "AudioTrack write failed", e)
                        break
                    }
                }
            } catch (_: Exception) { /* channel closed */ }
        }

        // ── AudioRecord (capture) ─────────────────────────────────────────────
        val minRecBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minRecBuf, FRAME_BYTES * 8)
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                // Permission not granted or hardware issue — abort call
                Log.e(TAG, "AudioRecord not initialized — aborting call")
                cleanup()
                return
            }
            audioRecord?.startRecording()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AudioRecord", e)
            cleanup()
            return
        }

        // ── Capture loop ──────────────────────────────────────────────────────
        captureJob = scope.launch(Dispatchers.IO) {
            val convId = activeConvId ?: return@launch
            val pcmBuf = ByteArray(FRAME_BYTES)

            while (isActive && _callState.value == CallState.ACTIVE) {
                try {
                    val bytesRead = audioRecord?.read(pcmBuf, 0, FRAME_BYTES) ?: break
                    if (bytesRead <= 0) continue

                    val key = encryptKey ?: break
                    if (isMuted) continue  // capture but discard, so buffer stays drained

                    val seq = sendSequenceAtomic.getAndIncrement()
                    val framePacket = buildAudioFrame(key, pcmBuf.copyOf(bytesRead), seq)
                    backgroundConnectionManager.sendPacket(convId, "af_${seq}", framePacket)
                } catch (_: Exception) { /* drop frame on transient error */ }
            }
        }
        startAudioWatchdog()   // terminate call if peer stops sending audio
    }

    /**
     * Watchdog coroutine: ends the call if no audio frames arrive from the peer within
     * [AUDIO_WATCHDOG_MS] ms. Handles the case where the peer's device disconnects
     * silently (network loss, Doze mode, app killed) without sending a HANGUP signal.
     */
    private fun startAudioWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(5_000)                   // grace period for state to reach ACTIVE
            while (isActive) {
                if (_callState.value != CallState.ACTIVE) break
                if (System.currentTimeMillis() - lastAudioRxMs > AUDIO_WATCHDOG_MS) {
                    Log.w(TAG, "Audio watchdog: no frames received for ${AUDIO_WATCHDOG_MS}ms — ending call")
                    cleanup()
                    break
                }
                delay(5_000)
            }
        }
    }

    private fun onAudioFrameReceived(packet: ByteArray) {
        if (_callState.value != CallState.ACTIVE) return
        // Minimum: magic(1) + seq(4) + nonce(12) + aead_tag(16) = 33 bytes
        if (packet.size < 33) return
        val key = decryptKey ?: return

        // Update watchdog timer immediately (not inside a deferred coroutine)
        lastAudioRxMs = System.currentTimeMillis()

        try {
            val nonce = packet.copyOfRange(5, 17)
            val ciphertext = packet.copyOfRange(17, packet.size)

            val pcm = AEADCipher.decrypt(
                ciphertext = ciphertext,
                key = key,
                nonce = nonce,
                additionalData = byteArrayOf()
            )
            // Send to single playback coroutine — ordered writes, no thread contention
            audioPlaybackChannel.trySend(pcm)
        } catch (e: Exception) {
            Log.w(TAG, "Audio frame decrypt/playback failed", e)
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PACKET BUILDERS
    // ═════════════════════════════════════════════════════════════════════════

    private fun buildSignalPacket(
        type: CallSignalType,
        callId: String,
        ephemeralPub: ByteArray  // 32 bytes or zeroes
    ): ByteArray {
        require(ephemeralPub.size == 32)
        val callIdBytes = callId.hexToBytes()
        val packet = ByteArray(SIGNAL_SIZE)
        val buf = ByteBuffer.wrap(packet)

        buf.put(CALL_SIGNAL_MAGIC)      // [0]
        buf.put(type.code)              // [1]
        buf.put(callIdBytes)            // [2..17]
        buf.putLong(System.currentTimeMillis())  // [18..25]
        buf.put(ephemeralPub)           // [26..57]
        // Signature over bytes [0..57]
        val signedData = packet.copyOfRange(0, 58)
        val sigDigest = MessageDigest.getInstance("SHA-256").digest(signedData)
        val signature = identityKeyManager.sign(sigDigest)  // 64 bytes
        buf.put(signature)              // [58..121]

        return packet
    }

    private fun buildAudioFrame(key: ByteArray, pcm: ByteArray, seq: Int): ByteArray {
        // Deterministic nonce: SHA-256(encryptKey ∥ seqBytes)[0..11]
        val seqBytes = ByteBuffer.allocate(4).putInt(seq).array()
        val nonceInput = ByteArray(key.size + 4)
        key.copyInto(nonceInput)
        seqBytes.copyInto(nonceInput, key.size)
        val nonce = MessageDigest.getInstance("SHA-256").digest(nonceInput).copyOfRange(0, 12)

        val ciphertext = AEADCipher.encrypt(
            plaintext = pcm,
            key = key,
            nonce = nonce,
            additionalData = byteArrayOf()
        )

        return ByteBuffer.allocate(1 + 4 + 12 + ciphertext.size).apply {
            put(AUDIO_FRAME_MAGIC)
            putInt(seq)
            put(nonce)
            put(ciphertext)
        }.array()
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Derives the two directional call keys.
     * Both sides compute the same [callerEncKey] / [calleeEncKey] since sharedSecret is symmetric.
     *
     * @return Pair(encryptKey, decryptKey) from THIS device's perspective
     */
    private fun deriveCallKeys(
        sharedSecret: ByteArray,
        callId: String,
        isCaller: Boolean
    ): Pair<ByteArray, ByteArray> {
        val callIdBytes = callId.hexToBytes()

        fun derive(label: String): ByteArray = MessageDigest.getInstance("SHA-256").run {
            update(sharedSecret)
            update(label.toByteArray(Charsets.UTF_8))
            update(callIdBytes)
            digest()
        }

        val callerEncKey = derive("caller_send")
        val calleeEncKey = derive("callee_send")

        return if (isCaller) callerEncKey to calleeEncKey else calleeEncKey to callerEncKey
    }

    private fun generateCallId(): String =
        ByteArray(16).also { random.nextBytes(it) }.toHexString()

    private fun cleanup() {
        synchronized(cleanupLock) {
            // Guard: prevent concurrent or duplicate cleanups
            if (_callState.value == CallState.IDLE) return
            _callState.value = CallState.ENDING

            Log.d(TAG, "cleanup() — tearing down call")

            // Stop the call foreground service (no-op if never started)
            CallForegroundService.stop(context)

            ringTimeoutJob?.cancel(); ringTimeoutJob = null
            captureJob?.cancel(); captureJob = null
            playbackJob?.cancel(); playbackJob = null
            audioPlaybackChannel.close()
            watchdogJob?.cancel(); watchdogJob = null

            try { audioRecord?.stop() } catch (_: Exception) {}
            try { audioRecord?.release() } catch (_: Exception) {}
            try { audioTrack?.stop() } catch (_: Exception) {}
            try { audioTrack?.release() } catch (_: Exception) {}
            audioRecord = null
            audioTrack = null

            // Restore audio mode
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            _isSpeakerOn.value = false
            _isMutedState.value = false
            isMuted = false

            // Zeroize call keys
            encryptKey?.fill(0); encryptKey = null
            decryptKey?.fill(0); decryptKey = null
            ourEphemeralPair?.zeroizePrivate(); ourEphemeralPair = null
            pendingOfferEphPub?.fill(0); pendingOfferEphPub = null

            activeCallId = null
            activeConvId = null
            activeContact = null
            _incomingCallData.value = null

            _callState.value = CallState.IDLE
        }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        val s = if (length % 2 != 0) "0$this" else this
        return ByteArray(s.length / 2) { i ->
            ((s[i * 2].digitToInt(16) shl 4) or s[i * 2 + 1].digitToInt(16)).toByte()
        }
    }
}
