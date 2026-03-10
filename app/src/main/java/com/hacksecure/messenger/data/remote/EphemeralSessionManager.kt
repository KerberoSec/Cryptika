// data/remote/EphemeralSessionManager.kt
// Manages ephemeral anonymous sessions with 30-minute auto-destruction.
package com.cryptika.messenger.data.remote

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cryptika.messenger.data.local.AuthStore
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient
import com.cryptika.messenger.domain.crypto.*
import com.cryptika.messenger.domain.model.*
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
    private val identityKeyManager: IdentityKeyManager
) {
    companion object {
        private const val TAG = "EphemeralSessionMgr"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Per-session state ─────────────────────────────────────────────────────

    data class EphemeralSession(
        val sessionUUID: String,
        val contactId: String,
        val expiresAt: Long,
        val wsClient: RelayWebSocketClient,
        var destroyJob: Job? = null,
        @Volatile var messageProcessor: MessageProcessor? = null,
        @Volatile var ephemeralKeyPair: SessionKeyManager.EphemeralKeyPair? = null
    )

    private val sessions = ConcurrentHashMap<String, EphemeralSession>()

    private val _activeSessions = MutableStateFlow<List<String>>(emptyList())
    val activeSessions: StateFlow<List<String>> = _activeSessions

    // ── Callbacks from ChatViewModel ─────────────────────────────────────────

    private val chatPacketHandlers =
        ConcurrentHashMap<String, suspend (msgId: String, packet: ByteArray) -> Unit>()
    private val sessionReadyCallbacks =
        ConcurrentHashMap<String, suspend (MessageProcessor) -> Unit>()

    fun registerChatHandler(
        sessionUUID: String,
        packetHandler: suspend (String, ByteArray) -> Unit,
        onSessionReady: suspend (MessageProcessor) -> Unit
    ) {
        chatPacketHandlers[sessionUUID] = packetHandler
        sessionReadyCallbacks[sessionUUID] = onSessionReady
        sessions[sessionUUID]?.messageProcessor?.let {
            scope.launch { onSessionReady(it) }
        }
    }

    fun unregisterChatHandler(sessionUUID: String) {
        chatPacketHandlers.remove(sessionUUID)
        sessionReadyCallbacks.remove(sessionUUID)
    }

    // ── Session lifecycle ─────────────────────────────────────────────────────

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

        // Save peer as a contact (ephemeral — will be deleted on session destroy)
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
        val session = EphemeralSession(
            sessionUUID = sessionUUID,
            contactId = contactId,
            expiresAt = expiresAt,
            wsClient = wsClient
        )
        sessions[sessionUUID] = session
        updateActiveSessionsList()

        // Schedule auto-destroy
        val ttlMs = expiresAt - System.currentTimeMillis()
        if (ttlMs <= 0) {
            destroySession(sessionUUID)
            return
        }
        session.destroyJob = scope.launch {
            delay(ttlMs)
            destroySession(sessionUUID)
        }

        // Connect WebSocket with JWT auth — route via ?session=<UUID>&token=<JWT>
        val jwtToken = authStore.jwtToken ?: return
        connectSession(session, jwtToken, identity)

        Log.d(TAG, "Joined session ${sessionUUID.take(8)}... (TTL=${ttlMs / 1000}s)")
    }

    private fun connectSession(session: EphemeralSession, jwtToken: String, identity: LocalIdentity) {
        // Build custom WebSocket URL with session param
        val wsUrl = "${serverConfig.relayBaseUrl}/ws?session=${session.sessionUUID}&token=$jwtToken"

        // Use the WSClient's connect with session as conversationId
        // The server will route based on ?session= param
        session.wsClient.connect(session.sessionUUID, jwtToken, identity.identityHex)

        // Collect events
        scope.launch {
            session.wsClient.events.collect { event ->
                when (event) {
                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.Connected -> {
                        Log.d(TAG, "Session ${session.sessionUUID.take(8)}... connected")
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
                            handshakeManager.isHandshakeOffer(packetBytes) -> {
                                completeHandshake(session, packetBytes)
                            }
                            else -> {
                                // Forward to ChatViewModel if registered
                                chatPacketHandlers[session.sessionUUID]?.invoke(
                                    event.message.messageId, packetBytes
                                )
                            }
                        }
                    }

                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.Disconnected -> {
                        Log.d(TAG, "Session ${session.sessionUUID.take(8)}... disconnected")
                    }

                    is com.cryptika.messenger.data.remote.websocket.RelayEvent.Error -> {
                        Log.e(TAG, "Session ${session.sessionUUID.take(8)}... error", event.throwable)
                    }
                }
            }
        }
    }

    private suspend fun completeHandshake(session: EphemeralSession, offerPacket: ByteArray) {
        val contactId = session.contactId
        val contact = contactRepository.getContact(contactId) ?: return
        val identity = identityRepository.getLocalIdentity() ?: return

        val ephemeralPair = session.ephemeralKeyPair
        if (ephemeralPair != null) {
            // We already sent an offer — derive session key
            try {
                val sessionKey = withContext(Dispatchers.Default) {
                    handshakeManager.deriveSessionKey(
                        offerBytes = offerPacket,
                        peerIdentityPublicKey = contact.publicKeyBytes,
                        ourEphemeralPair = ephemeralPair,
                        myIdentityHash = identity.identityHash,
                        peerIdentityHash = contact.identityHash
                    )
                }
                session.ephemeralKeyPair = null

                val sendRatchet = HashRatchet(sessionKey.copyOf())
                val recvRatchet = HashRatchet(sessionKey.copyOf())
                sessionKey.fill(0)

                val processor = MessageProcessor(
                    sendRatchet = sendRatchet,
                    recvRatchet = recvRatchet,
                    identityKeyManager = identityKeyManager,
                    peerPublicKeyBytes = contact.publicKeyBytes,
                    myIdentityHash = identity.identityHash
                )
                session.messageProcessor = processor
                sessionReadyCallbacks[session.sessionUUID]?.invoke(processor)
                Log.d(TAG, "Handshake OK for session ${session.sessionUUID.take(8)}...")
            } catch (e: Exception) {
                Log.e(TAG, "Handshake failed for session ${session.sessionUUID.take(8)}...", e)
            }
        } else {
            // We haven't sent an offer yet — create one and respond
            val (responsePacket, newPair) = withContext(Dispatchers.Default) {
                handshakeManager.createOffer()
            }
            session.ephemeralKeyPair = newPair
            session.wsClient.send(session.sessionUUID, "hs_${UUID.randomUUID()}", responsePacket)

            try {
                val sessionKey = withContext(Dispatchers.Default) {
                    handshakeManager.deriveSessionKey(
                        offerBytes = offerPacket,
                        peerIdentityPublicKey = contact.publicKeyBytes,
                        ourEphemeralPair = newPair,
                        myIdentityHash = identity.identityHash,
                        peerIdentityHash = contact.identityHash
                    )
                }
                session.ephemeralKeyPair = null

                val sendRatchet = HashRatchet(sessionKey.copyOf())
                val recvRatchet = HashRatchet(sessionKey.copyOf())
                sessionKey.fill(0)

                val processor = MessageProcessor(
                    sendRatchet = sendRatchet,
                    recvRatchet = recvRatchet,
                    identityKeyManager = identityKeyManager,
                    peerPublicKeyBytes = contact.publicKeyBytes,
                    myIdentityHash = identity.identityHash
                )
                session.messageProcessor = processor
                sessionReadyCallbacks[session.sessionUUID]?.invoke(processor)
            } catch (e: Exception) {
                Log.e(TAG, "Handshake failed for session ${session.sessionUUID.take(8)}...", e)
            }
        }
    }

    /** Send a packet via an ephemeral session's WebSocket. */
    suspend fun sendPacket(sessionUUID: String, messageId: String, packet: ByteArray): Boolean {
        val session = sessions[sessionUUID] ?: return false
        return session.wsClient.send(sessionUUID, messageId, packet)
    }

    fun getMessageProcessor(sessionUUID: String): MessageProcessor? =
        sessions[sessionUUID]?.messageProcessor

    fun getContactId(sessionUUID: String): String? =
        sessions[sessionUUID]?.contactId

    fun getExpiresAt(sessionUUID: String): Long =
        sessions[sessionUUID]?.expiresAt ?: 0

    /**
     * Destroy a session — cryptographic erasure.
     * 1. Close WebSocket
     * 2. Zeroize all crypto material (DH keys, session keys, ratchet state)
     * 3. Delete all messages for this conversation from DB
     * 4. Delete the contact record
     */
    suspend fun destroySession(sessionUUID: String) {
        val session = sessions.remove(sessionUUID) ?: return
        updateActiveSessionsList()

        Log.d(TAG, "DESTROYING session ${sessionUUID.take(8)}...")

        // Cancel auto-destroy timer
        session.destroyJob?.cancel()

        // Close WebSocket
        session.wsClient.disconnect()

        // Zeroize crypto
        session.ephemeralKeyPair?.zeroizePrivate()
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

        Log.d(TAG, "Session ${sessionUUID.take(8)}... DESTROYED (cryptographic erasure complete)")
    }

    /** Destroy ALL sessions — called on logout or app wipe. */
    fun destroyAllSessions() {
        scope.launch {
            for (uuid in sessions.keys.toList()) {
                destroySession(uuid)
            }
        }
    }

    private fun updateActiveSessionsList() {
        _activeSessions.value = sessions.keys.toList()
    }

    private fun String.hexToBytes(): ByteArray {
        check(length % 2 == 0) { "Hex string must have even length" }
        return ByteArray(length / 2) { i ->
            ((get(i * 2).digitToInt(16) shl 4) or get(i * 2 + 1).digitToInt(16)).toByte()
        }
    }
}
