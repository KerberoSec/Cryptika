package com.cryptika.messenger.data.remote;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;
import com.cryptika.messenger.domain.crypto.AEADCipher;
import com.cryptika.messenger.domain.crypto.Ed25519Verifier;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.crypto.SessionKeyManager;
import com.cryptika.messenger.domain.model.*;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.StateFlow;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages the complete lifecycle of an encrypted voice call.
 *
 * ── Architecture ──────────────────────────────────────────────────────────────
 * Calls use the existing relay WebSocket (same WebSocket as messaging) by prefixing
 * packets with distinct magic bytes so BackgroundConnectionManager can route them here.
 *
 * Two magic bytes are reserved:
 *  0x02 — CALL_SIGNAL : call control (OFFER / ANSWER / REJECT / HANGUP / BUSY)
 *  0x03 — AUDIO_FRAME : encrypted PCM audio sent during an active call
 *
 * ── Signal packet format (122 bytes — fixed size) ─────────────────────────────
 *  [0]      0x02 magic
 *  [1]      type byte (see CallSignalType)
 *  [2..17]  call_id — 16 random bytes identifying this call instance
 *  [18..25] timestamp_ms — big-endian long: replay protection (±5 min skew)
 *  [26..57] ephemeral X25519 public key — 32 bytes (zeroed for REJECT/HANGUP/BUSY)
 *  [58..121] Ed25519 signature over SHA-256(bytes[0..57]) — 64 bytes
 * Total: 1 + 1 + 16 + 8 + 32 + 64 = 122 bytes
 *
 * ── Audio frame format ────────────────────────────────────────────────────────
 *  [0]      0x03 magic
 *  [1..4]   sequence_number — big-endian int (replay / ordering)
 *  [5..16]  nonce — 12 bytes = SHA-256(encryptKey ∥ seqBytes)[0..11]
 *  [17..N]  ChaCha20-Poly1305(plaintext=pcm, key=encryptKey, nonce, ad=empty) + 16-byte tag
 *
 * ── Call key derivation ──────────────────────────────────────────────────────
 *  sharedSecret = X25519(ourEphPriv, peerEphPub)    — symmetric both sides
 *  callerEncKey = SHA-256(secret ∥ "caller_send" ∥ callIdBytes)
 *  calleeEncKey = SHA-256(secret ∥ "callee_send" ∥ callIdBytes)
 *
 *  Caller  → encryptKey = callerEncKey, decryptKey = calleeEncKey
 *  Callee  → encryptKey = calleeEncKey, decryptKey = callerEncKey
 *
 *  Using direction-specific keys ensures nonces are never reused even though
 *  both sides use the same sequence counter starting at 0.
 *
 * ── Audio parameters ──────────────────────────────────────────────────────────
 *  Sample rate : 16 000 Hz (narrow-band voice)
 *  Channels    : mono
 *  Encoding    : 16-bit PCM
 *  Frame size  : 160 samples = 10 ms → 320 bytes raw PCM per frame
 *  Encrypted   : 320 + 16 (tag) = 336 bytes per frame
 *  Wire size   : 1 + 4 + 12 + 336 = 353 bytes ≈ 35.3 KB/s at 100 fps
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00ba\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u001b\b\u0007\u0018\u0000 f2\u00020\u0001:\u0001fB)\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010>\u001a\u00020?J \u0010@\u001a\u00020\u001c2\u0006\u0010A\u001a\u00020\u001c2\u0006\u0010B\u001a\u00020\u001c2\u0006\u0010C\u001a\u00020DH\u0002J \u0010E\u001a\u00020\u001c2\u0006\u0010F\u001a\u00020G2\u0006\u0010H\u001a\u00020\u00142\u0006\u0010I\u001a\u00020\u001cH\u0002J\b\u0010J\u001a\u00020?H\u0002J,\u0010K\u001a\u000e\u0012\u0004\u0012\u00020\u001c\u0012\u0004\u0012\u00020\u001c0L2\u0006\u0010M\u001a\u00020\u001c2\u0006\u0010H\u001a\u00020\u00142\u0006\u0010N\u001a\u00020\u0011H\u0002J\b\u0010O\u001a\u00020\u0014H\u0002J\u0018\u0010P\u001a\u00020?2\u0006\u0010H\u001a\u00020\u00142\u0006\u0010Q\u001a\u00020\u001cH\u0002J(\u0010R\u001a\u00020?2\u0006\u0010S\u001a\u00020\u00142\u0006\u0010H\u001a\u00020\u00142\u0006\u0010T\u001a\u00020\u001c2\u0006\u0010U\u001a\u00020\u0016H\u0002J\u0010\u0010V\u001a\u00020?2\u0006\u0010W\u001a\u00020\u0014H\u0002J\u0006\u0010X\u001a\u00020?J\u0010\u0010Y\u001a\u00020?2\u0006\u0010Z\u001a\u00020\u001cH\u0002J&\u0010[\u001a\u00020?2\u0006\u0010S\u001a\u00020\u00142\u0006\u0010\\\u001a\u00020\u00142\u0006\u0010Z\u001a\u00020\u001c2\u0006\u0010U\u001a\u00020\u0016J \u0010]\u001a\u00020?2\u0006\u0010S\u001a\u00020\u00142\u0006\u0010Z\u001a\u00020\u001c2\u0006\u0010U\u001a\u00020\u0016H\u0002J\u0006\u0010^\u001a\u00020?J\b\u0010_\u001a\u00020?H\u0002J\b\u0010`\u001a\u00020?H\u0002J\u0016\u0010a\u001a\u00020?2\u0006\u0010S\u001a\u00020\u00142\u0006\u0010U\u001a\u00020\u0016J\u0006\u0010b\u001a\u00020?J\u0006\u0010c\u001a\u00020?J\f\u0010d\u001a\u00020\u001c*\u00020\u0014H\u0002J\f\u0010e\u001a\u00020\u0014*\u00020\u001cH\u0002R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00110\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001f\u001a\u0004\u0018\u00010 X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\r0\"\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0010\u0010%\u001a\u0004\u0018\u00010&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010(\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010)\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010*\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\"\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010$R\u000e\u0010,\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00110\"\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010$R\u0017\u0010/\u001a\b\u0012\u0004\u0012\u00020\u00110\"\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010$R\u000e\u00100\u001a\u000201X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00102\u001a\u0004\u0018\u000103X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00104\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u00105\u001a\u0004\u0018\u00010&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00106\u001a\u000207X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u00108\u001a\u0004\u0018\u00010&X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00109\u001a\u00020:X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010;\u001a\u00020<X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010=\u001a\u0004\u0018\u00010&X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006g"}, d2 = {"Lcom/cryptika/messenger/data/remote/CallManager;", "", "context", "Landroid/content/Context;", "backgroundConnectionManager", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "sessionKeyManager", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;", "(Landroid/content/Context;Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;)V", "_callState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/domain/model/CallState;", "_incomingCallData", "Lcom/cryptika/messenger/domain/model/IncomingCallData;", "_isMutedState", "", "_isSpeakerOn", "activeCallId", "", "activeContact", "Lcom/cryptika/messenger/domain/model/Contact;", "activeConvId", "audioManager", "Landroid/media/AudioManager;", "audioPlaybackChannel", "Lkotlinx/coroutines/channels/Channel;", "", "audioRecord", "Landroid/media/AudioRecord;", "audioTrack", "Landroid/media/AudioTrack;", "callState", "Lkotlinx/coroutines/flow/StateFlow;", "getCallState", "()Lkotlinx/coroutines/flow/StateFlow;", "captureJob", "Lkotlinx/coroutines/Job;", "cleanupLock", "decryptKey", "encryptKey", "incomingCallData", "getIncomingCallData", "isCallerRole", "isMuted", "isMutedState", "isSpeakerOn", "lastAudioRxMs", "", "ourEphemeralPair", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "pendingOfferEphPub", "playbackJob", "random", "Ljava/security/SecureRandom;", "ringTimeoutJob", "scope", "Lkotlinx/coroutines/CoroutineScope;", "sendSequenceAtomic", "Ljava/util/concurrent/atomic/AtomicInteger;", "watchdogJob", "answerCall", "", "buildAudioFrame", "key", "pcm", "seq", "", "buildSignalPacket", "type", "Lcom/cryptika/messenger/domain/model/CallSignalType;", "callId", "ephemeralPub", "cleanup", "deriveCallKeys", "Lkotlin/Pair;", "sharedSecret", "isCaller", "generateCallId", "handleAnswer", "calleeEphPub", "handleIncomingOffer", "convId", "callerEphPub", "contact", "handleRejectOrHangup", "incomingCallId", "hangup", "onAudioFrameReceived", "packet", "onRelayPacket", "msgId", "onSignalReceived", "rejectCall", "startAudio", "startAudioWatchdog", "startCall", "toggleMute", "toggleSpeaker", "hexToBytes", "toHexString", "Companion", "Cryptika_release"})
public final class CallManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.SessionKeyManager sessionKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "CallManager";
    
    /**
     * Magic byte at position [0] of every call-signal packet.
     */
    public static final byte CALL_SIGNAL_MAGIC = (byte)2;
    
    /**
     * Magic byte at position [0] of every audio-frame packet.
     */
    public static final byte AUDIO_FRAME_MAGIC = (byte)3;
    
    /**
     * Fixed byte-length of a call signal packet.
     */
    public static final int SIGNAL_SIZE = 122;
    private static final int SAMPLE_RATE = 16000;
    private static final int FRAME_SAMPLES = 160;
    private static final int FRAME_BYTES = 320;
    private static final long RING_TIMEOUT_MS = 60000L;
    private static final long AUDIO_WATCHDOG_MS = 30000L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final java.security.SecureRandom random = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.domain.model.CallState> _callState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.CallState> callState = null;
    
    /**
     * Non-null while an incoming call is ringing; cleared after accept/reject/hangup.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.domain.model.IncomingCallData> _incomingCallData = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.IncomingCallData> incomingCallData = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile java.lang.String activeCallId;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile java.lang.String activeConvId;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.cryptika.messenger.domain.model.Contact activeContact;
    @kotlin.jvm.Volatile()
    private volatile boolean isCallerRole = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile byte[] pendingOfferEphPub;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair ourEphemeralPair;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile byte[] encryptKey;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile byte[] decryptKey;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile android.media.AudioRecord audioRecord;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile android.media.AudioTrack audioTrack;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile kotlinx.coroutines.Job captureJob;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile kotlinx.coroutines.Job playbackJob;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile kotlinx.coroutines.Job ringTimeoutJob;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile kotlinx.coroutines.Job watchdogJob;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicInteger sendSequenceAtomic = null;
    @kotlin.jvm.Volatile()
    private volatile long lastAudioRxMs = 0L;
    @kotlin.jvm.Volatile()
    private volatile boolean isMuted = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Object cleanupLock = null;
    @org.jetbrains.annotations.NotNull()
    private kotlinx.coroutines.channels.Channel<byte[]> audioPlaybackChannel;
    @org.jetbrains.annotations.NotNull()
    private final android.media.AudioManager audioManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isSpeakerOn = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSpeakerOn = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isMutedState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isMutedState = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.remote.CallManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public CallManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.SessionKeyManager sessionKeyManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.CallState> getCallState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.IncomingCallData> getIncomingCallData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSpeakerOn() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isMutedState() {
        return null;
    }
    
    /**
     * Initiates an outgoing call to the contact owning [convId].
     * Waits up to 5 s for the relay WebSocket to connect before sending OFFER.
     * Sends a signed CALL_OFFER packet over the relay; starts a 60 s ring timeout.
     */
    public final void startCall(@org.jetbrains.annotations.NotNull()
    java.lang.String convId, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    /**
     * Accepts the pending incoming call.
     * Performs the callee side of the ephemeral X25519 DH and starts audio I/O.
     * Must only be called when [callState] == [CallState.INCOMING_RINGING].
     */
    public final void answerCall() {
    }
    
    /**
     * Rejects the pending incoming call.
     * Sends REJECT and returns to IDLE immediately.
     */
    public final void rejectCall() {
    }
    
    /**
     * Ends the active call or cancels an outgoing/incoming ringing call.
     */
    public final void hangup() {
    }
    
    public final void toggleMute() {
    }
    
    public final void toggleSpeaker() {
    }
    
    /**
     * Entry point for all call-related relay packets.
     * Called by [BackgroundConnectionManager] whenever a packet with magic byte 0x02 or 0x03 arrives.
     *
     * @param contact the Contact associated with this conversation (for signature verification)
     */
    public final void onRelayPacket(@org.jetbrains.annotations.NotNull()
    java.lang.String convId, @org.jetbrains.annotations.NotNull()
    java.lang.String msgId, @org.jetbrains.annotations.NotNull()
    byte[] packet, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    private final void onSignalReceived(java.lang.String convId, byte[] packet, com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    private final void handleIncomingOffer(java.lang.String convId, java.lang.String callId, byte[] callerEphPub, com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    private final void handleAnswer(java.lang.String callId, byte[] calleeEphPub) {
    }
    
    private final void handleRejectOrHangup(java.lang.String incomingCallId) {
    }
    
    private final void startAudio() {
    }
    
    /**
     * Watchdog coroutine: ends the call if no audio frames arrive from the peer within
     * [AUDIO_WATCHDOG_MS] ms. Handles the case where the peer's device disconnects
     * silently (network loss, Doze mode, app killed) without sending a HANGUP signal.
     */
    private final void startAudioWatchdog() {
    }
    
    private final void onAudioFrameReceived(byte[] packet) {
    }
    
    private final byte[] buildSignalPacket(com.cryptika.messenger.domain.model.CallSignalType type, java.lang.String callId, byte[] ephemeralPub) {
        return null;
    }
    
    private final byte[] buildAudioFrame(byte[] key, byte[] pcm, int seq) {
        return null;
    }
    
    /**
     * Derives the two directional call keys.
     * Both sides compute the same [callerEncKey] / [calleeEncKey] since sharedSecret is symmetric.
     *
     * @return Pair(encryptKey, decryptKey) from THIS device's perspective
     */
    private final kotlin.Pair<byte[], byte[]> deriveCallKeys(byte[] sharedSecret, java.lang.String callId, boolean isCaller) {
        return null;
    }
    
    private final java.lang.String generateCallId() {
        return null;
    }
    
    private final void cleanup() {
    }
    
    private final java.lang.String toHexString(byte[] $this$toHexString) {
        return null;
    }
    
    private final byte[] hexToBytes(java.lang.String $this$hexToBytes) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/cryptika/messenger/data/remote/CallManager$Companion;", "", "()V", "AUDIO_FRAME_MAGIC", "", "AUDIO_WATCHDOG_MS", "", "CALL_SIGNAL_MAGIC", "FRAME_BYTES", "", "FRAME_SAMPLES", "RING_TIMEOUT_MS", "SAMPLE_RATE", "SIGNAL_SIZE", "TAG", "", "Cryptika_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}