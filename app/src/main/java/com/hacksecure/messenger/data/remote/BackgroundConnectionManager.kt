// data/remote/BackgroundConnectionManager.kt
// Maintains relay WebSocket connections for ALL conversations while the app is alive.
// Allows messages to be received even when the chat screen is not open.
package com.cryptika.messenger.data.remote

import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cryptika.messenger.MainActivity
import com.cryptika.messenger.R
import com.cryptika.messenger.data.local.AuthStore
import com.cryptika.messenger.data.local.db.ConversationDao
import com.cryptika.messenger.data.local.db.ConversationEntity
import com.cryptika.messenger.data.remote.api.PresenceRequest
import com.cryptika.messenger.data.remote.api.RelayApi
import com.cryptika.messenger.data.remote.api.TicketRequest
import com.cryptika.messenger.data.remote.websocket.RelayEvent
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient
import com.cryptika.messenger.domain.crypto.*
import com.cryptika.messenger.domain.model.*
import com.cryptika.messenger.domain.repository.ContactRepository
import com.cryptika.messenger.domain.repository.IdentityRepository
import com.cryptika.messenger.domain.repository.MessageRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import java.util.Base64
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that keeps a relay WebSocket alive for every known conversation.
 *
 * When the user opens a chat screen, [ChatViewModel] registers itself as the
 * active handler via [registerChatHandler]. All incoming MESSAGE packets are
 * then forwarded to ChatViewModel for decryption and UI display. When the user
 * leaves the chat, [unregisterChatHandler] is called and this manager takes over
 * background decryption + DB storage so messages are never lost.
 *
 * The DH handshake is always performed here, keeping crypto state in one place.
 * ChatViewModel receives the established [MessageProcessor] via [onSessionReady].
 */
@Singleton
class BackgroundConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository,
    private val messageRepository: MessageRepository,
    private val conversationDao: ConversationDao,
    private val handshakeManager: HandshakeManager,
    private val identityKeyManager: IdentityKeyManager,
    private val ticketManager: TicketManager,
    private val relayApi: RelayApi,
    private val authStore: AuthStore,
    private val serverConfig: ServerConfig,
    private val okHttpClient: OkHttpClient,
    // Lazy<CallManager> breaks the circular dependency:
    // CallManager → BackgroundConnectionManager (for sendPacket)
    // BackgroundConnectionManager → Lazy<CallManager> (for routing call packets)
    private val callManager: Lazy<CallManager>,
) {
    /** Long-lived supervisor scope that outlives any single ViewModel. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Network connectivity ───────────────────────────────────────────────────
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Reconnects all dead conversations when the device regains internet access.
     * Registered in [startAllConnections] and unregistered in [stopAll].
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available — reconnecting dead conversations")
            scope.launch { reconnectAll() }
        }
    }

    companion object {
        private const val TAG = "BackgroundConnMgr"
        private const val MSG_CHANNEL_ID = "cryptika_messages"
        private const val MSG_NOTIFICATION_ID = 2001
    }

    // ── Per-conversation state ────────────────────────────────────────────────

    class ConvState(
        val conversationId: String,
        val contact: Contact,
        val myIdentity: LocalIdentity,
        val wsClient: RelayWebSocketClient
    ) {
        @Volatile var messageProcessor: MessageProcessor? = null
        @Volatile var ourEphemeralPair: SessionKeyManager.EphemeralKeyPair? = null
        @Volatile var ourOfferPacket: ByteArray? = null
        @Volatile var verifiedTicket: VerifiedTicket? = null
        val seenMessageIds: MutableSet<String> = Collections.synchronizedSet(HashSet())
        var collectionJob: Job? = null

        private val _connState = MutableStateFlow(ConnectionState.DISCONNECTED)
        val connectionState: StateFlow<ConnectionState> = _connState
        fun updateConnectionState(s: ConnectionState) { _connState.value = s }
    }

    private val convStates = ConcurrentHashMap<String, ConvState>()

    // ── ChatViewModel callbacks ───────────────────────────────────────────────

    /** Raw MESSAGE packet handler set by the active ChatViewModel. */
    private val chatPacketHandlers =
        ConcurrentHashMap<String, suspend (msgId: String, packet: ByteArray) -> Unit>()

    /** Notified when the DH handshake completes for a conversation. */
    private val sessionReadyCallbacks =
        ConcurrentHashMap<String, suspend (processor: MessageProcessor) -> Unit>()

    /** Notified on connection state changes. */
    private val connStateCallbacks =
        ConcurrentHashMap<String, suspend (ConnectionState) -> Unit>()

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Called by ChatViewModel when the chat screen opens.
     * - Routes incoming MESSAGE packets to [packetHandler].
     * - Calls [onSessionReady] immediately if a session is already established,
     *   or later when the handshake completes.
     */
    fun registerChatHandler(
        convId: String,
        packetHandler: suspend (msgId: String, packet: ByteArray) -> Unit,
        onSessionReady: suspend (MessageProcessor) -> Unit,
        onConnectionStateChange: suspend (ConnectionState) -> Unit,
    ) {
        chatPacketHandlers[convId] = packetHandler
        sessionReadyCallbacks[convId] = onSessionReady
        connStateCallbacks[convId] = onConnectionStateChange

        // Notify immediately if state is already available
        val state = convStates[convId] ?: return
        scope.launch {
            onConnectionStateChange(state.connectionState.value)
            state.messageProcessor?.let { onSessionReady(it) }
        }
    }

    fun unregisterChatHandler(convId: String) {
        chatPacketHandlers.remove(convId)
        sessionReadyCallbacks.remove(convId)
        connStateCallbacks.remove(convId)
    }

    /** Returns the established [MessageProcessor] for [convId], or null. */
    fun getMessageProcessor(convId: String): MessageProcessor? =
        convStates[convId]?.messageProcessor

    /** Transmits a wire-packet for [convId] via this manager's WebSocket. */
    suspend fun sendPacket(convId: String, messageId: String, packet: ByteArray): Boolean =
        convStates[convId]?.wsClient?.send(convId, messageId, packet) ?: false

    fun isConnected(convId: String): Boolean =
        convStates[convId]?.wsClient?.isConnected() == true

    /**
     * Starts background connections for all known contacts.
     * Idempotent — safe to call multiple times.
     * Called from [CryptikaApp.onCreate].
     */
    fun startAllConnections() {
        registerNetworkCallback()
        scope.launch {
            // Wait for identity (may not exist yet on first launch)
            var identity = identityRepository.getLocalIdentity()
            if (identity == null) {
                repeat(20) {
                    delay(500)
                    identity = identityRepository.getLocalIdentity()
                    if (identity != null) return@repeat
                }
                if (identity == null) return@launch
            }
            val myIdentity = identity!!

            // Connect to existing contacts AND watch for new ones added during the session
            contactRepository.getContacts().collect { contacts ->
                contacts.forEach { contact ->
                    val convId = buildConvId(myIdentity.identityHex, contact.identityHex)
                    if (!convStates.containsKey(convId)) {
                        ensureConnected(myIdentity, contact)
                    }
                }
            }
        }
    }

    /**
     * Ensures a background connection exists for the given conversation.
     * Idempotent — no-op if already connected.
     */
    fun ensureConnected(identity: LocalIdentity, contact: Contact) {
        val convId = buildConvId(identity.identityHex, contact.identityHex)
        if (convStates.containsKey(convId)) return

        val wsClient = RelayWebSocketClient(okHttpClient, serverConfig)
        val state = ConvState(convId, contact, identity, wsClient)
        convStates[convId] = state

        scope.launch { connectConversation(state) }
    }

    /**
     * Reconnects all dead/idle conversations. Called when:
     * - The device regains network access (via [networkCallback])
     * - Any other trigger that knows connectivity has been restored
     */
    private fun reconnectAll() {
        for ((_, state) in convStates) {
            val ws = state.wsClient
            if (!ws.isConnected() && !ws.isReconnecting()) {
                Log.d(TAG, "reconnectAll → reconnecting ${state.conversationId.take(16)}...")
                scope.launch { connectConversation(state) }
            }
        }
    }

    /**
     * Tears down and re-establishes the connection for a conversation.
     * Called by ChatViewModel's "Retry Connection" button.
     */
    fun forceReconnect(identity: LocalIdentity, contact: Contact) {
        val convId = buildConvId(identity.identityHex, contact.identityHex)
        convStates.remove(convId)?.let { old ->
            old.collectionJob?.cancel()
            old.ourEphemeralPair?.zeroizePrivate()
            old.wsClient.disconnect()
        }
        ensureConnected(identity, contact)
    }

    // ── Internal connection lifecycle ─────────────────────────────────────────

    private suspend fun connectConversation(state: ConvState) {
        try {
            // JWT is required for all server interactions: presence, ticket, and WS auth.
            val jwtToken = authStore.jwtToken
            if (jwtToken.isNullOrBlank()) {
                Log.w(TAG, "Skipping connection for ${state.conversationId.take(16)}: no JWT available")
                return
            }

            // Register presence (non-blocking on failure)
            try {
                relayApi.registerPresence(
                    url = "${serverConfig.apiBaseUrl}/api/v1/presence",
                    auth = "Bearer $jwtToken",
                    request = PresenceRequest(
                        identity_hash = state.myIdentity.identityHex,
                        connection_token = UUID.randomUUID().toString()
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Presence registration failed for ${state.conversationId.take(16)}: ${e.message}")
            }

            // Request and verify session ticket (non-blocking on failure)
            try {
                val sorted = listOf(state.myIdentity.identityHex, state.contact.identityHex).sorted()
                val ticketResponse = relayApi.requestTicket(
                    url = "${serverConfig.apiBaseUrl}/api/v1/ticket",
                    auth = "Bearer $jwtToken",
                    request = TicketRequest(a_id = sorted[0], b_id = sorted[1])
                )
                val ticketBytes = Base64.getDecoder().decode(ticketResponse.ticket_b64)
                val verified = ticketManager.verifyTicket(ticketBytes)
                // Validate ticket participants match this conversation
                ticketManager.validateParticipants(
                    verified,
                    state.myIdentity.identityHash,
                    state.contact.identityHash
                )
                state.verifiedTicket = verified
            } catch (e: Exception) {
                Log.w(TAG, "Ticket fetch/verify failed for ${state.conversationId.take(16)}: ${e.message}. Proceeding without ticket binding.")
            }

            // Connect WebSocket with JWT auth in the Authorization header
            state.wsClient.connect(state.conversationId, jwtToken, state.myIdentity.identityHex)

            // Collect relay events for this conversation
            state.collectionJob = scope.launch {
                state.wsClient.events.collect { event ->
                    when (event) {
                        is RelayEvent.Connected -> {
                            state.updateConnectionState(ConnectionState.CONNECTED_RELAY)
                            connStateCallbacks[state.conversationId]
                                ?.invoke(ConnectionState.CONNECTED_RELAY)

                            // Send our DH handshake offer
                            val (offerPacket, ephemeralPair) = withContext(Dispatchers.Default) {
                                handshakeManager.createOffer()
                            }
                            state.ourEphemeralPair = ephemeralPair
                            state.ourOfferPacket = offerPacket
                            state.wsClient.send(
                                state.conversationId,
                                "hs_bg_${UUID.randomUUID()}",
                                offerPacket
                            )
                        }

                        is RelayEvent.MessageReceived -> {
                            if (event.message.conversationId == state.conversationId) {
                                val packetBytes = event.message.packetBytes
                                when {
                                    handshakeManager.isHandshakeOffer(packetBytes) -> {
                                        // DH offer — handled here, never forwarded to ChatViewModel
                                        completeHandshake(state, packetBytes)
                                    }
                                    packetBytes.isNotEmpty() &&
                                    (packetBytes[0] == CallManager.CALL_SIGNAL_MAGIC ||
                                     packetBytes[0] == CallManager.AUDIO_FRAME_MAGIC) -> {
                                        // Call signal or audio frame — route to CallManager
                                        callManager.get().onRelayPacket(
                                            state.conversationId,
                                            event.message.messageId,
                                            packetBytes,
                                            state.contact
                                        )
                                    }
                                    else -> {
                                        // Encrypted message packet
                                        val handler = chatPacketHandlers[state.conversationId]
                                        if (handler != null) {
                                            handler(event.message.messageId, packetBytes)
                                        } else {
                                            receiveInBackground(state, event.message.messageId, packetBytes)
                                        }
                                    }
                                }
                            }
                        }

                        is RelayEvent.Disconnected -> {
                            state.updateConnectionState(ConnectionState.DISCONNECTED)
                            connStateCallbacks[state.conversationId]
                                ?.invoke(ConnectionState.DISCONNECTED)
                        }

                        is RelayEvent.Error -> {
                            state.updateConnectionState(ConnectionState.ERROR)
                            connStateCallbacks[state.conversationId]?.invoke(ConnectionState.ERROR)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // RelayWebSocketClient handles reconnect; we just ensure state is correct
        }
    }

    // ── Handshake ─────────────────────────────────────────────────────────────

    private suspend fun completeHandshake(state: ConvState, offerBytes: ByteArray) {
        if (state.messageProcessor != null) return // already established — ignore duplicate

        // Echo our offer so a late-joining peer can also complete the handshake
        state.ourOfferPacket?.let { stored ->
            state.wsClient.send(
                state.conversationId,
                "hs_echo_bg_${UUID.randomUUID()}",
                stored
            )
        }

        val ephemeralPair = state.ourEphemeralPair ?: return

        try {
            val (_, sendRoot, recvRoot) = withContext(Dispatchers.Default) {
                handshakeManager.deriveSessionKey(
                    offerBytes = offerBytes,
                    peerIdentityPublicKey = state.contact.publicKeyBytes,
                    ourEphemeralPair = ephemeralPair,   // private key zeroized inside
                    myIdentityHash = state.myIdentity.identityHash,
                    peerIdentityHash = state.contact.identityHash,
                    verifiedTicket = state.verifiedTicket
                )
            }
            state.ourEphemeralPair = null
            state.verifiedTicket = null  // clear after use — ticketHash no longer needed

            val sendRatchet = HashRatchet(sendRoot)
            val recvRatchet = HashRatchet(recvRoot)

            val processor = MessageProcessor(
                sendRatchet = sendRatchet,
                recvRatchet = recvRatchet,
                identityKeyManager = identityKeyManager,
                peerPublicKeyBytes = state.contact.publicKeyBytes,
                myIdentityHash = state.myIdentity.identityHash
            )
            state.messageProcessor = processor
            state.updateConnectionState(ConnectionState.CONNECTED_RELAY)

            // Notify active ChatViewModel that the session is ready
            sessionReadyCallbacks[state.conversationId]?.invoke(processor)
            connStateCallbacks[state.conversationId]?.invoke(ConnectionState.CONNECTED_RELAY)

        } catch (_: CryptoError.SignatureInvalid) {
            // Peer signature invalid — ignore offer
        } catch (_: Exception) { }
    }

    // ── Background message processing ─────────────────────────────────────────

    private suspend fun receiveInBackground(
        state: ConvState,
        messageId: String,
        packetBytes: ByteArray
    ) {
        if (!state.seenMessageIds.add(messageId)) return // deduplicate
        val processor = state.messageProcessor ?: return // can't decrypt without session

        try {
            val (plaintextBytes, header) = withContext(Dispatchers.Default) {
                processor.receive(packetBytes)
            }
            val text = plaintextBytes.toString(Charsets.UTF_8)
            val now = System.currentTimeMillis()

            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = state.conversationId,
                senderId = header.senderId.toHexString(),
                content = text,
                timestampMs = header.timestampMs,
                counter = header.counter,
                expirySeconds = header.expirySeconds,
                expiryDeadlineMs = if (header.expirySeconds > 0)
                    header.timestampMs + (header.expirySeconds * 1000L) else null,
                isOutgoing = false,
                state = MessageState.DELIVERED,
                messageType = header.messageType
            )

            withContext(Dispatchers.IO) {
                messageRepository.saveMessage(message, plaintextBytes)

                // Ensure conversation row exists, then increment unread count
                val existing = conversationDao.getConversation(state.conversationId)
                if (existing == null) {
                    conversationDao.insertOrUpdate(
                        ConversationEntity(
                            id = state.conversationId,
                            contactId = state.contact.id,
                            lastMessageAt = now,
                            unreadCount = 1
                        )
                    )
                } else {
                    conversationDao.incrementUnreadCount(state.conversationId, now)
                }

                // Show notification with unread count
                showUnreadNotification(state.contact.displayName, state.conversationId)
            }
        } catch (e: CryptoError) {
            Log.w("BGConn", "Crypto check failed for msg $messageId: ${e::class.simpleName}")
        } catch (e: Exception) {
            Log.w("BGConn", "Background receive failed for msg $messageId", e)
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    fun stopAll() {
        try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (_: Exception) {}
        convStates.values.forEach { state ->
            state.collectionJob?.cancel()
            state.ourEphemeralPair?.zeroizePrivate()
            state.wsClient.disconnect()
        }
        convStates.clear()
        scope.cancel()
    }
    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register network callback: ${e.message}")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun buildConvId(a: String, b: String): String {
        val sorted = listOf(a, b).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    // ── Unread message notifications ──────────────────────────────────────────

    private fun ensureMessageChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(MSG_CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                MSG_CHANNEL_ID,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    private suspend fun showUnreadNotification(senderName: String, conversationId: String) {
        ensureMessageChannel()
        val nm = context.getSystemService(NotificationManager::class.java)

        // Sum all unread counts across conversations
        val conv = conversationDao.getConversation(conversationId)
        val unread = conv?.unreadCount ?: 1

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MSG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(
                if (unread == 1) "New message"
                else "$unread new messages"
            )
            .setNumber(unread)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        nm.notify(MSG_NOTIFICATION_ID, notification)
    }

    fun clearMessageNotification() {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.cancel(MSG_NOTIFICATION_ID)
    }
}
