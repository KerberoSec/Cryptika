// data/remote/EphemeralSessionManager.kt
// Manages ephemeral anonymous sessions with 30-minute auto-destruction.
package com.cryptika.messenger.data.remote

import android.os.Handler
import android.os.Looper
import com.cryptika.messenger.data.local.AuthStore
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient
import com.cryptika.messenger.domain.crypto.*
import com.cryptika.messenger.domain.model.*
import com.cryptika.messenger.domain.repository.AuthRepository
import com.cryptika.messenger.domain.repository.ContactRepository
import com.cryptika.messenger.domain.repository.IdentityRepository
import com.cryptika.messenger.domain.repository.MessageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks ephemeral anonymous sessions created via contact accept flow.
 *
 * Each session:
 * - Has a UUID for WebSocket routing (replaces conversation ID)
 * - Lives for exactly 30 minutes (server-enforced, client-enforced)
 * - On expiry: all crypto material, message history, and contact data are wiped
 */
@Singleton
class EphemeralSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authStore: AuthStore,
    private val okHttpClient: OkHttpClient,
    private val serverConfig: ServerConfig,
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository,
    private val messageRepository: MessageRepository,
    private val handshakeManager: HandshakeManager,
    private val identityKeyManager: IdentityKeyManager,
    private val authRepository: AuthRepository,
    // Lazy<CallManager> breaks the circular dependency
    private val callManager: dagger.Lazy<CallManager>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Per-session state

    data class EphemeralSession(
        val sessionUUID: String,
        val contactId: String,
        val expiresAt: Long,
        val wsClient: RelayWebSocketClient,
        var destroyJob: Job? = null,
        var collectionJob: Job? = null,
        @Volatile var messageProcessor: MessageProcessor? = null,
        @Volatile var ephemeralKeyPair: SessionKeyManager.EphemeralKeyPair? = null
    )

    private val sessions = ConcurrentHashMap<String, EphemeralSession>()

    private val _activeSessions = MutableStateFlow<List<String>>(emptyList())
    val activeSessions: StateFlow<List<String>> = _activeSessions

    // Special control frame: 0xFF 0xFE + "PEER_DISCONNECTED"
    // Sent by the server when the peer's WebSocket closes.
    private val PEER_DISCONNECTED_PREFIX = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    private val PEER_DISCONNECTED_TAG = "PEER_DISCONNECTED"

    // Callbacks from ChatViewModel

    private val chatPacketHandlers =
        ConcurrentHashMap<String, suspend (msgId: String, packet: ByteArray) -> Unit>()
    private val sessionReadyCallbacks =
        ConcurrentHashMap<String, suspend (MessageProcessor) -> Unit>()
    private val peerDisconnectedCallbacks =
        ConcurrentHashMap<String, suspend () -> Unit>()

    fun registerChatHandler(
        sessionUUID: String,
        packetHandler: suspend (String, ByteArray) -> Unit,
        onSessionReady: suspend (MessageProcessor) -> Unit,
        onPeerDisconnected: suspend () -> Unit = {}
    ) {
        chatPacketHandlers[sessionUUID] = packetHandler
        sessionReadyCallbacks[sessionUUID] = onSessionReady
        peerDisconnectedCallbacks[sessionUUID] = onPeerDisconnected
        sessions[sessionUUID]?.messageProcessor?.let {
            scope.launch { onSessionReady(it) }
        }
    }

    fun unregisterChatHandler(sessionUUID: String) {
        chatPacketHandlers.remove(sessionUUID)
        sessionReadyCallbacks.remove(sessionUUID)
        peerDisconnectedCallbacks.remove(sessionUUID)
    }

    // Session lifecycle

    companion object {
        /** Hard client-side cap: no ephemeral session may outlive 30 minutes regardless
         *  of what the server declares in [expiresAt]. */
        private const val MAX_SESSION_TTL_MS = 30 * 60 * 1_000L  // 30 minutes
    }

    /**
     * Create and connect to an ephemeral session.
     * Called after a contact request is accepted (by either party).
     *
     * @param sessionUUID Server-issued session UUID
     * @param expiresAt Server-issued expiry timestamp (ms)
     * @param peerIdentityHash Peer's Ed25519 identity hash hex
     * @param peerPublicKeyB64 Peer's Ed25519 public key (Base64)
     * @param peerNickname Display name of peer
     */
    suspend fun joinSession(
        sessionUUID: String,
        expiresAt: Long,
        peerIdentityHash: String,
        peerPublicKeyB64: String,
        peerNickname: String
    ) {
        if (sessions.containsKey(sessionUUID)) return // already joined

        val identity = identityRepository.getLocalIdentity() ?: return
        val peerPubKeyBytes = android.util.Base64.decode(peerPublicKeyB64, android.util.Base64.NO_WRAP)

        // JWT required for WS auth: check before creating any session state to avoid orphaned contact entries
        val jwtToken = authStore.jwtToken ?: return

        // Save peer as a contact (ephemeral, will be deleted on session destroy)
        val contactId = UUID.randomUUID().toString()
        val contact = Contact(
            id = contactId,
            identityHash = peerIdentityHash.hexToBytes(),
            publicKeyBytes = peerPubKeyBytes,
            displayName = peerNickname,
            verifiedAt = System.currentTimeMillis()
        )
        contactRepository.saveContact(contact)

        // Create WebSocket client for this session
        val wsClient = RelayWebSocketClient(okHttpClient, serverConfig)

        // Enforce the 30-minute client-side cap regardless of server-declared expiresAt.
        val now = System.currentTimeMillis()
        val cappedExpiresAt = minOf(expiresAt, now + MAX_SESSION_TTL_MS)

        val session = EphemeralSession(
            sessionUUID = sessionUUID,
            contactId = contactId,
            expiresAt = cappedExpiresAt,
            wsClient = wsClient
        )
        sessions[sessionUUID] = session
        updateActiveSessionsList()

        // Schedule auto-destroy
        val ttlMs = cappedExpiresAt - now
        if (ttlMs <= 0) {
            destroySession(sessionUUID)
            return
        }
        session.destroyJob = scope.launch {
            delay(ttlMs)
            destroySession(sessionUUID)
        }

        // Connect WebSocket with JWT auth in the Authorization header.
        connectSession(session, jwtToken, identity)
    }

    private fun connectSession(session: EphemeralSession, jwtToken: String, identity: LocalIdentity) {
        // Use the WS client with session-based routing; the JWT stays in the Authorization header.
        session.wsClient.connect(session.sessionUUID, jwtToken, identity.identityHex)

        // Collect events
        session.collectionJob?.cancel()
        session.collectionJob = scope.launch {
            session.wsClient.events.collect { event ->
                when (event) {
                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.Connected -> {
                        // Initiate DH handshake
                        val (offerPacket, ephemeralPair) = withContext(Dispatchers.Default) {
                            handshakeManager.createOffer()
                        }
                        session.ephemeralKeyPair = ephemeralPair
                        session.wsClient.send(session.sessionUUID, "hs_${UUID.randomUUID()}", offerPacket)
                    }

                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.MessageReceived -> {
                        val packetBytes = event.message.packetBytes
                        when {
                            isPeerDisconnectedSignal(packetBytes) -> {
                                peerDisconnectedCallbacks[session.sessionUUID]?.invoke()
                                destroySession(session.sessionUUID)
                            }
                            packetBytes.size == 1 &&
                            packetBytes[0] == BackgroundConnectionManager.FORCE_LOGOUT_MAGIC &&
                            session.messageProcessor != null -> {
                                peerDisconnectedCallbacks[session.sessionUUID]?.invoke()
                                destroySession(session.sessionUUID)
                            }
                            handshakeManager.isHandshakeOffer(packetBytes) -> {
                                completeHandshake(session, packetBytes)
                            }
                            // Audio frame: route directly to CallManager without querying Room DB (100 fps performance optimization)
                            packetBytes.isNotEmpty() && packetBytes[0] == CallManager.AUDIO_FRAME_MAGIC -> {
                                callManager.get().onRelayPacket(
                                    session.sessionUUID,
                                    event.message.messageId,
                                    packetBytes,
                                    null
                                )
                            }
                            // Call signal: requires Contact for Ed25519 signature verification
                            packetBytes.isNotEmpty() && packetBytes[0] == CallManager.CALL_SIGNAL_MAGIC -> {
                                val contact = contactRepository.getContact(session.contactId)
                                if (contact != null) {
                                    callManager.get().onRelayPacket(
                                        session.sessionUUID,
                                        event.message.messageId,
                                        packetBytes,
                                        contact
                                    )
                                }
                            }
                            else -> {
                                val handler = chatPacketHandlers[session.sessionUUID]
                                if (handler != null) {
                                    handler(event.message.messageId, packetBytes)
                                } else {
                                    receiveInBackground(session, event.message.messageId, packetBytes)
                                }
                            }
                        }
                    }

                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.Disconnected -> {
                        // Transport disconnect: RelayWebSocketClient will automatically reconnect with backoff.
                        // Do NOT destroy the ephemeral session or delete messages on transient network drops.
                    }

                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.Error -> {
                        // Transport error: RelayWebSocketClient will automatically reconnect with backoff.
                    }
                }
            }
        }
    }

    private suspend fun completeHandshake(session: EphemeralSession, offerPacket: ByteArray) {
        if (session.messageProcessor != null) return
        val contactId = session.contactId
        val contact = contactRepository.getContact(contactId) ?: return
        val identity = identityRepository.getLocalIdentity() ?: return

        val ephemeralPair = session.ephemeralKeyPair
        if (ephemeralPair != null) {
            // We already sent an offer: derive session key
            try {
                val (_, sendRoot, recvRoot) = withContext(Dispatchers.Default) {
                    handshakeManager.deriveSessionKey(
                        offerBytes = offerPacket,
                        peerIdentityPublicKey = contact.publicKeyBytes,
                        ourEphemeralPair = ephemeralPair,
                        myIdentityHash = identity.identityHash,
                        peerIdentityHash = contact.identityHash
                    )
                }
                session.ephemeralKeyPair = null

                val sendRatchet = HashRatchet(sendRoot)
                val recvRatchet = HashRatchet(recvRoot)
                sendRoot.fill(0)   // HashRatchet copied the seed; zeroize the original
                recvRoot.fill(0)

                val processor = MessageProcessor(
                    sendRatchet = sendRatchet,
                    recvRatchet = recvRatchet,
                    identityKeyManager = identityKeyManager,
                    peerPublicKeyBytes = contact.publicKeyBytes,
                    myIdentityHash = identity.identityHash
                )
                session.messageProcessor = processor
                sessionReadyCallbacks[session.sessionUUID]?.invoke(processor)
            } catch (_: Exception) {
                session.ephemeralKeyPair = null
            }
        } else {
            // We haven't sent an offer yet: create one and respond
            val (responsePacket, newPair) = withContext(Dispatchers.Default) {
                handshakeManager.createOffer()
            }
            session.ephemeralKeyPair = newPair
            session.wsClient.send(session.sessionUUID, "hs_${UUID.randomUUID()}", responsePacket)

            try {
                val (_, sendRoot2, recvRoot2) = withContext(Dispatchers.Default) {
                    handshakeManager.deriveSessionKey(
                        offerBytes = offerPacket,
                        peerIdentityPublicKey = contact.publicKeyBytes,
                        ourEphemeralPair = newPair,
                        myIdentityHash = identity.identityHash,
                        peerIdentityHash = contact.identityHash
                    )
                }
                session.ephemeralKeyPair = null

                val sendRatchet = HashRatchet(sendRoot2)
                val recvRatchet = HashRatchet(recvRoot2)
                sendRoot2.fill(0)  // HashRatchet copied the seed; zeroize the original
                recvRoot2.fill(0)

                val processor = MessageProcessor(
                    sendRatchet = sendRatchet,
                    recvRatchet = recvRatchet,
                    identityKeyManager = identityKeyManager,
                    peerPublicKeyBytes = contact.publicKeyBytes,
                    myIdentityHash = identity.identityHash
                )
                session.messageProcessor = processor
                sessionReadyCallbacks[session.sessionUUID]?.invoke(processor)
            } catch (_: Exception) {
                session.ephemeralKeyPair = null
            }
        }
    }

    /** Send a packet via an ephemeral session's WebSocket. Triggers credential burn on first message. */
    suspend fun sendPacket(sessionUUID: String, messageId: String, packet: ByteArray): Boolean {
        val session = sessions[sessionUUID] ?: return false
        val result = session.wsClient.send(sessionUUID, messageId, packet)
        if (result) triggerBurnOnFirstMessage()
        return result
    }

    /** Burns server credentials once on the first successfully sent message. */
    @Volatile private var burnTriggered = false
    private suspend fun triggerBurnOnFirstMessage() {
        if (burnTriggered || authRepository.isCredentialsBurned()) return
        burnTriggered = true
        scope.launch {
            authRepository.burnCredentials()
        }
    }

    private val seenMessageIds = ConcurrentHashMap.newKeySet<String>()

    private suspend fun receiveInBackground(
        session: EphemeralSession,
        messageId: String,
        packetBytes: ByteArray
    ) {
        if (!seenMessageIds.add(messageId)) return
        val processor = session.messageProcessor ?: return

        try {
            val (plaintextBytes, header) = withContext(Dispatchers.Default) {
                processor.receive(packetBytes)
            }
            val text = plaintextBytes.toString(Charsets.UTF_8)
            val now = System.currentTimeMillis()

            if (text.startsWith("__DEL__:")) {
                val counterStr = text.removePrefix("__DEL__:")
                val targetCounter = counterStr.toLongOrNull()
                if (targetCounter != null) {
                    val contact = contactRepository.getContact(session.contactId)
                    if (contact != null) {
                        withContext(Dispatchers.IO) {
                            messageRepository.deletePeerMessageByCounter(
                                session.sessionUUID,
                                contact.identityHex,
                                targetCounter
                            )
                        }
                    }
                }
                return
            }

            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = session.sessionUUID,
                senderId = header.senderId.toHexString(),
                content = text,
                timestampMs = header.timestampMs,
                counter = header.counter,
                expirySeconds = header.expirySeconds,
                expiryDeadlineMs = if (header.expirySeconds > 0) {
                    val senderDeadline = header.timestampMs + (header.expirySeconds * 1000L)
                    maxOf(senderDeadline, now + 5_000L)
                } else null,
                isOutgoing = false,
                state = MessageState.DELIVERED,
                messageType = header.messageType
            )

            withContext(Dispatchers.IO) {
                messageRepository.saveMessage(message, plaintextBytes)
            }
        } catch (_: Exception) {}
    }

    fun getMessageProcessor(sessionUUID: String): MessageProcessor? =
        sessions[sessionUUID]?.messageProcessor

    fun getContactId(sessionUUID: String): String? =
        sessions[sessionUUID]?.contactId

    fun getExpiresAt(sessionUUID: String): Long =
        sessions[sessionUUID]?.expiresAt ?: 0

    /**
     * Destroy a session: cryptographic erasure.
     * 1. Close WebSocket
     * 2. Zeroize all crypto material (DH keys, session keys, ratchet state)
     * 3. Delete all messages for this conversation from DB
     * 4. Delete the contact record
     */
    suspend fun destroySession(sessionUUID: String) {
        val session = sessions.remove(sessionUUID) ?: return
        updateActiveSessionsList()

        // Cancel auto-destroy timer and collection job
        session.destroyJob?.cancel()
        session.collectionJob?.cancel()

        // Close WebSocket
        session.wsClient.disconnect()

        // Zeroize all crypto material (ratchet keys, DH keys, identity buffers)
        session.ephemeralKeyPair?.zeroizePrivate()
        session.messageProcessor?.zeroize()
        session.messageProcessor = null

        // Delete all messages for this conversation
        try {
            messageRepository.deleteConversationMessages(sessionUUID)
        } catch (_: Exception) {}

        // Delete the ephemeral contact
        try {
            contactRepository.deleteContact(session.contactId)
        } catch (_: Exception) {}

        // Unregister handlers
        chatPacketHandlers.remove(sessionUUID)
        sessionReadyCallbacks.remove(sessionUUID)
    }

    /** Destroy ALL sessions: called on logout or app wipe. */
    fun destroyAllSessions() {
        burnTriggered = false
        scope.launch {
            for (uuid in sessions.keys.toList()) {
                destroySession(uuid)
            }
        }
    }

    private fun updateActiveSessionsList() {
        _activeSessions.value = sessions.keys.toList()
    }

    /** Check if a received packet is the PEER_DISCONNECTED control frame. */
    private fun isPeerDisconnectedSignal(data: ByteArray): Boolean {
        if (data.size < PEER_DISCONNECTED_PREFIX.size + PEER_DISCONNECTED_TAG.length) return false
        if (data[0] != PEER_DISCONNECTED_PREFIX[0] || data[1] != PEER_DISCONNECTED_PREFIX[1]) return false
        val tag = String(data, PEER_DISCONNECTED_PREFIX.size, data.size - PEER_DISCONNECTED_PREFIX.size, Charsets.UTF_8)
        return tag == PEER_DISCONNECTED_TAG
    }

    private fun String.hexToBytes(): ByteArray {
        check(length % 2 == 0) { "Hex string must have even length" }
        return ByteArray(length / 2) { i ->
            ((get(i * 2).digitToInt(16) shl 4) or get(i * 2 + 1).digitToInt(16)).toByte()
        }
    }
}
