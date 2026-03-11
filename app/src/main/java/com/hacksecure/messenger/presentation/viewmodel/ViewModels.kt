// presentation/viewmodel/ViewModels.kt
package com.cryptika.messenger.presentation.viewmodel

import android.util.Base64
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptika.messenger.data.remote.api.RelayApi
import com.cryptika.messenger.data.remote.ServerConfig
import com.cryptika.messenger.data.remote.websocket.RelayEvent
import com.cryptika.messenger.domain.crypto.*
import com.cryptika.messenger.domain.model.*
import com.cryptika.messenger.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import com.cryptika.messenger.data.remote.CallManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════════════════════
// SPLASH / ONBOARDING VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
data class SplashUiState(
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init { initializeIdentity() }

    private fun initializeIdentity() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                var identity = identityRepository.getLocalIdentity()
                if (identity == null) {
                    identity = identityRepository.generateIdentity()
                }
                _uiState.update { it.copy(isLoading = false, isReady = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to initialize identity: ${e.message}") }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// HOME VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
data class HomeUiState(
    val conversations: List<ConversationUiItem> = emptyList(),
    val isLoading: Boolean = true
)

data class ConversationUiItem(
    val conversationId: String,
    val contact: Contact,
    val lastMessageAt: Long,
    val unreadCount: Int,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository,
    private val messageRepository: MessageRepository,
    private val conversationDao: com.cryptika.messenger.data.local.db.ConversationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val myHex = withContext(Dispatchers.IO) {
                identityRepository.getLocalIdentity()?.identityHex ?: ""
            }
            contactRepository.getContacts().collect { contacts ->
                val items = contacts.map { contact ->
                    val convId = buildConversationId(myHex, contact.identityHex)
                    val conv = withContext(Dispatchers.IO) {
                        conversationDao.getConversation(convId)
                    }
                    ConversationUiItem(
                        conversationId = convId,
                        contact = contact,
                        lastMessageAt = conv?.lastMessageAt ?: contact.verifiedAt,
                        unreadCount = conv?.unreadCount ?: 0
                    )
                }
                _uiState.update { it.copy(conversations = items, isLoading = false) }
            }
        }
    }

    fun deleteConversation(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contact = contactRepository.getContact(contactId) ?: return@launch
                val myHex = identityRepository.getLocalIdentity()?.identityHex ?: return@launch
                val convId = buildConversationId(myHex, contact.identityHex)
                messageRepository.deleteConversationMessages(convId)
                contactRepository.deleteContact(contactId)
            } catch (_: Exception) {}
        }
    }

    private fun buildConversationId(a: String, b: String): String {
        val sorted = listOf(a, b).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// QR DISPLAY VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
data class QrDisplayUiState(
    val qrPayload: String = "",
    val fingerprintHex: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class QrDisplayViewModel @Inject constructor(
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrDisplayUiState())
    val uiState: StateFlow<QrDisplayUiState> = _uiState.asStateFlow()

    init { loadIdentity() }

    private fun loadIdentity() {
        viewModelScope.launch(Dispatchers.Default) {
            val identity = identityRepository.getLocalIdentity() ?: return@launch
            val qrPayload = buildQrPayload(identity.publicKeyBytes)
            val fingerprint = formatFingerprint(identity.identityHex)
            _uiState.update {
                it.copy(qrPayload = qrPayload, fingerprintHex = fingerprint, isLoading = false)
            }
        }
    }

    /**
     * QR payload: "cryptika://id/v1/<BASE64URL_NO_PADDING>"
     * Binary: [1 byte version=0x01][32 bytes public key]
     */
    private fun buildQrPayload(publicKeyBytes: ByteArray): String {
        val payload = ByteArray(33)
        payload[0] = 0x01  // version
        publicKeyBytes.copyInto(payload, 1)
        val b64 = Base64.encodeToString(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        return "cryptika://id/v1/$b64"
    }

    /** Groups fingerprint hex into blocks of 8 for readability */
    private fun formatFingerprint(hex: String): String =
        hex.chunked(8).joinToString(" ")
}

// ══════════════════════════════════════════════════════════════════════════════
// QR SCAN VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
data class QrScanResult(
    val publicKeyBytes: ByteArray,
    val identityHash: ByteArray,
    val fingerprintHex: String
)

sealed class ScanState {
    object Idle : ScanState()
    data class Success(val result: QrScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}

@HiltViewModel
class QrScanViewModel @Inject constructor() : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun processQrResult(rawValue: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (!rawValue.startsWith("cryptika://id/v1/")) {
                    _scanState.update { ScanState.Error("Invalid QR code format") }
                    return@launch
                }
                val b64Part = rawValue.removePrefix("cryptika://id/v1/")
                val decoded = Base64.decode(b64Part, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)

                if (decoded.size < 33) {
                    _scanState.update { ScanState.Error("QR payload too short") }
                    return@launch
                }
                val version = decoded[0]
                if (version != 0x01.toByte()) {
                    _scanState.update { ScanState.Error("Unsupported QR version") }
                    return@launch
                }
                val publicKeyBytes = decoded.copyOfRange(1, 33)
                val identityHash = IdentityHash.compute(publicKeyBytes)
                val fingerprintHex = identityHash.joinToString("") { "%02x".format(it) }
                    .chunked(8).joinToString(" ")

                _scanState.update {
                    ScanState.Success(QrScanResult(publicKeyBytes, identityHash, fingerprintHex))
                }
            } catch (e: Exception) {
                _scanState.update { ScanState.Error("Failed to parse QR code") }
            }
        }
    }

    fun reset() = _scanState.update { ScanState.Idle }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTACT CONFIRMATION VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
sealed class ConfirmState {
    object Idle : ConfirmState()
    object Saving : ConfirmState()
    object Saved : ConfirmState()
    data class KeyChangeWarning(val existingContact: Contact) : ConfirmState()
    data class Error(val message: String) : ConfirmState()
}

@HiltViewModel
class ContactConfirmViewModel @Inject constructor(
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ConfirmState>(ConfirmState.Idle)
    val state: StateFlow<ConfirmState> = _state.asStateFlow()

    fun saveContact(
        publicKeyBytes: ByteArray,
        identityHash: ByteArray,
        displayName: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { ConfirmState.Saving }

            val identityHex = identityHash.joinToString("") { "%02x".format(it) }
            val existing = contactRepository.getContactByIdentityHash(identityHex)

            if (existing != null && !existing.publicKeyBytes.contentEquals(publicKeyBytes)) {
                // Key changed — warn user
                _state.update { ConfirmState.KeyChangeWarning(existing) }
                return@launch
            }

            val contact = Contact(
                id = existing?.id ?: UUID.randomUUID().toString(),
                identityHash = identityHash,
                publicKeyBytes = publicKeyBytes,
                displayName = displayName.ifBlank { "Unknown Contact" },
                verifiedAt = System.currentTimeMillis(),
                keyChangedAt = if (existing != null &&
                    !existing.publicKeyBytes.contentEquals(publicKeyBytes))
                    System.currentTimeMillis() else null
            )

            if (existing == null) contactRepository.saveContact(contact)
            else contactRepository.updateContact(contact)

            _state.update { ConfirmState.Saved }
        }
    }

    fun forceUpdateKey(
        existing: Contact,
        newPublicKeyBytes: ByteArray,
        newIdentityHash: ByteArray
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = existing.copy(
                publicKeyBytes = newPublicKeyBytes,
                identityHash = newIdentityHash,
                keyChangedAt = System.currentTimeMillis()
            )
            contactRepository.updateContact(updated)
            _state.update { ConfirmState.Saved }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CHAT VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val contact: Contact? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val sessionEstablished: Boolean = false,
    val defaultExpirySeconds: Int = 1800,
    val isLoading: Boolean = true,
    val inputText: String = "",
    val selectedExpirySeconds: Int = 1800,
    val serverRelayUrl: String = "",
    val isEphemeral: Boolean = false,
    val ephemeralState: EphemeralSessionState = EphemeralSessionState.None
)

sealed class ChatEvent {
    data class ShowError(val message: String) : ChatEvent()
    data class MessageRejected(val reason: String) : ChatEvent()
    object SessionSecured : ChatEvent()
    object Snackbar : ChatEvent()
    /** Emitted when re-queuing previously-FAILED messages on reconnect. */
    object RetrySucceeded : ChatEvent()
    /** Emitted when the ephemeral session expires or disconnects — triggers full logout. */
    object ForceLogout : ChatEvent()
    /** Emitted when the peer disconnects (e.g. screen off). */
    object PeerDisconnected : ChatEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository,
    private val identityKeyManager: IdentityKeyManager,
    private val relayApi: RelayApi,
    private val serverConfig: ServerConfig,
    private val backgroundConnectionManager: com.cryptika.messenger.data.remote.BackgroundConnectionManager,
    private val ephemeralSessionManager: com.cryptika.messenger.data.remote.EphemeralSessionManager,
    private val conversationDao: com.cryptika.messenger.data.local.db.ConversationDao,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context
) : ViewModel(), DefaultLifecycleObserver {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>()
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    // ── Session state ────────────────────────────────────────────────────────

    @Volatile private var messageProcessor: MessageProcessor? = null
    @Volatile private var conversationId: String? = null
    @Volatile private var activeContact: Contact? = null
    @Volatile private var myIdentity: LocalIdentity? = null

    /** In-memory de-duplication for relay messages processed this session. */
    private val seenMessageIds = mutableSetOf<String>()

    /** True when this ChatViewModel is driving an ephemeral (contact-discovery) session. */
    @Volatile private var isEphemeralMode = false
    @Volatile private var ephemeralSessionUUID: String? = null
    private var ephemeralCountdownJob: kotlinx.coroutines.Job? = null

    /**
     * Tracks the single scheduled-expiry coroutine.  We only ever need one: it fires at the
     * nearest upcoming deadline, calls deleteExpiredMessages() (which cleans ALL due messages),
     * and the Room flow re-emits triggering a reschedule for the next nearest deadline.
     */
    private var nextExpiryJob: kotlinx.coroutines.Job? = null

    // ── Initialisation ───────────────────────────────────────────────────────

    fun initConversation(contactId: String) {
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) {
                contactRepository.getContact(contactId)
            } ?: return@launch

            val identity = withContext(Dispatchers.IO) {
                identityRepository.getLocalIdentity()
            } ?: return@launch

            conversationId = buildConversationId(identity.identityHex, contact.identityHex)
            activeContact = contact
            myIdentity = identity

            // Read persisted default expiry (set in Settings, defaults to 30 minutes)
            val defaultExpiry = appContext
                .getSharedPreferences("cryptika_settings", android.content.Context.MODE_PRIVATE)
                .getInt("default_expiry_seconds", 1800)

            _uiState.update { it.copy(contact = contact, isLoading = false, selectedExpirySeconds = defaultExpiry) }

            // Establish or reuse background connection via BackgroundConnectionManager
            connectRelay(conversationId!!, identity, contact)

            // Mark conversation as read + clear notification
            withContext(Dispatchers.IO) {
                conversationDao.markAsRead(conversationId!!)
            }
            backgroundConnectionManager.clearMessageNotification()

            // Immediately delete any messages that already expired while the app was closed.
            withContext(Dispatchers.IO) {
                try { messageRepository.deleteExpiredMessages() } catch (_: Exception) {}
            }

            // Room Flow suspends here indefinitely, updating UI whenever DB changes
            messageRepository.getMessages(conversationId!!).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
                scheduleNextExpiry(messages)
            }
        }
    }

    // ── Ephemeral session init ───────────────────────────────────────────────

    /**
     * Initialises the chat in ephemeral mode, backed by [EphemeralSessionManager].
     * Messages use in-memory storage; the session auto-destroys after its TTL.
     */
    fun initEphemeralSession(sessionUUID: String) {
        isEphemeralMode = true
        ephemeralSessionUUID = sessionUUID

        viewModelScope.launch {
            val contactId = ephemeralSessionManager.getContactId(sessionUUID) ?: return@launch
            val contact = withContext(Dispatchers.IO) {
                contactRepository.getContact(contactId)
            } ?: return@launch

            val identity = withContext(Dispatchers.IO) {
                identityRepository.getLocalIdentity()
            } ?: return@launch

            conversationId = sessionUUID  // use sessionUUID as conversationId
            activeContact = contact
            myIdentity = identity

            val expiresAt = ephemeralSessionManager.getExpiresAt(sessionUUID)

            _uiState.update {
                it.copy(
                    contact = contact,
                    isLoading = false,
                    isEphemeral = true,
                    selectedExpirySeconds = 3,
                    serverRelayUrl = serverConfig.relayBaseUrl,
                    ephemeralState = EphemeralSessionState.Active(
                        remainingMs = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0),
                        expiresAt = expiresAt
                    )
                )
            }

            // Register with EphemeralSessionManager to receive packets
            ephemeralSessionManager.registerChatHandler(
                sessionUUID = sessionUUID,
                packetHandler = { msgId, packetBytes -> receiveMessage(msgId, packetBytes) },
                onSessionReady = { processor ->
                    messageProcessor = processor
                    _uiState.update {
                        it.copy(
                            sessionEstablished = true,
                            connectionState = ConnectionState.CONNECTED_RELAY
                        )
                    }
                    viewModelScope.launch { _events.emit(ChatEvent.SessionSecured) }
                },
                onPeerDisconnected = {
                    viewModelScope.launch {
                        _events.emit(ChatEvent.PeerDisconnected)
                        // Small delay so the user can see the notification
                        kotlinx.coroutines.delay(1500)
                        _events.emit(ChatEvent.ForceLogout)
                    }
                }
            )

            // If session already has a processor, adopt it
            ephemeralSessionManager.getMessageProcessor(sessionUUID)?.let { existing ->
                messageProcessor = existing
                _uiState.update {
                    it.copy(sessionEstablished = true, connectionState = ConnectionState.CONNECTED_RELAY)
                }
            }

            // Countdown timer
            startEphemeralCountdown(expiresAt)

            // Room flow for messages stored under the sessionUUID conversation
            withContext(Dispatchers.IO) {
                try { messageRepository.deleteExpiredMessages() } catch (_: Exception) {}
            }
            messageRepository.getMessages(sessionUUID).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
                scheduleNextExpiry(messages)
            }
        }
    }

    private fun startEphemeralCountdown(expiresAt: Long) {
        ephemeralCountdownJob?.cancel()
        ephemeralCountdownJob = viewModelScope.launch {
            while (true) {
                val remaining = expiresAt - System.currentTimeMillis()
                if (remaining <= 0) {
                    _uiState.update {
                        it.copy(ephemeralState = EphemeralSessionState.Expired)
                    }
                    // Trigger force-logout when session expires
                    _events.emit(ChatEvent.ForceLogout)
                    break
                }
                _uiState.update {
                    it.copy(
                        ephemeralState = EphemeralSessionState.Active(
                            remainingMs = remaining,
                            expiresAt = expiresAt
                        )
                    )
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    // ── Relay connection ─────────────────────────────────────────────────────

    /**
     * Starts (or restarts) the WebSocket relay connection and the event-collection
     * coroutine.  Any previous relay session is cancelled first so we never accumulate
     * stale coroutines.
     */
    private fun connectRelay(convId: String, identity: LocalIdentity, contact: Contact) {
        // Clear any locally-cached processor reference; BGM owns the session now
        messageProcessor = null
        _uiState.update {
            it.copy(
                connectionState = ConnectionState.CONNECTING,
                sessionEstablished = false,
                serverRelayUrl = serverConfig.relayBaseUrl
            )
        }

        // If BGM already has an established session for this conversation, adopt it immediately
        backgroundConnectionManager.getMessageProcessor(convId)?.let { existingProcessor ->
            messageProcessor = existingProcessor
            _uiState.update { it.copy(sessionEstablished = true, connectionState = ConnectionState.CONNECTED_RELAY) }
        }

        // Register as the active chat handler so BGM routes incoming packets here
        backgroundConnectionManager.registerChatHandler(
            convId = convId,
            packetHandler = { msgId, packetBytes -> receiveMessage(msgId, packetBytes) },
            onSessionReady = { processor ->
                messageProcessor = processor
                _uiState.update { it.copy(sessionEstablished = true, connectionState = ConnectionState.CONNECTED_RELAY) }
                viewModelScope.launch { _events.emit(ChatEvent.SessionSecured) }
            },
            onConnectionStateChange = { newState ->
                _uiState.update { it.copy(connectionState = newState) }
            }
        )

        // Ensure BGM is connected for this conversation (idempotent)
        backgroundConnectionManager.ensureConnected(identity, contact)
    }

    // App Lifecycle -- reconnect when app returns from background
    override fun onStart(owner: LifecycleOwner) {
        if (isEphemeralMode) return  // ephemeral sessions are managed by EphemeralSessionManager
        val convId = conversationId ?: return
        val contact = activeContact ?: return

        viewModelScope.launch {
            val identity = myIdentity
                ?: withContext(Dispatchers.IO) { identityRepository.getLocalIdentity() }
                    ?.also { myIdentity = it }
                ?: return@launch
            connectRelay(convId, identity, contact)
        }
    }


    // ── Input helpers ────────────────────────────────────────────────────────

    fun updateInputText(text: String) = _uiState.update { it.copy(inputText = text) }

    fun setExpirySeconds(seconds: Int) = _uiState.update { it.copy(selectedExpirySeconds = seconds) }

    // ── Send ─────────────────────────────────────────────────────────────────

    /**
     * Encrypts and sends the current input text through the full cryptographic pipeline.
     *
     * Ordering (correct):
     *   1. Encrypt plaintext → wirePacketBytes
     *   2. Save message to DB as SENDING
     *   3. Send wirePacketBytes over relay
     *   4. Update message state to SENT or FAILED based on relay result
     */
    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val convId = conversationId ?: run {
            viewModelScope.launch { _events.emit(ChatEvent.ShowError("No active conversation")) }
            return
        }

        val processor = messageProcessor
        val expirySeconds = _uiState.value.selectedExpirySeconds

        viewModelScope.launch {
            try {
                // Clear input optimistically on Main thread
                _uiState.update { it.copy(inputText = "") }

                val messageId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val plaintextBytes = text.toByteArray(Charsets.UTF_8)

                val counter: Long
                val wirePacketBytes: ByteArray?

                if (processor != null) {
                    // Full encryption pipeline on Default dispatcher
                    val result = withContext(Dispatchers.Default) {
                        processor.send(plaintext = plaintextBytes, expirySeconds = expirySeconds)
                    }
                    wirePacketBytes = result.first
                    counter = result.second
                } else {
                    // Offline / session-not-yet-established mode
                    counter = withContext(Dispatchers.IO) {
                        val myPubKey = identityKeyManager.getPublicKeyBytes()
                        val myIdHex = IdentityHash.compute(myPubKey).toHexString()
                        messageRepository.getMaxCounter(convId, myIdHex) + 1
                    }
                    wirePacketBytes = null
                }

                val myPubKey = withContext(Dispatchers.Default) { identityKeyManager.getPublicKeyBytes() }
                val myIdHex = IdentityHash.compute(myPubKey).toHexString()

                // Always save as SENDING first, then update after network result
                val message = Message(
                    id = messageId,
                    conversationId = convId,
                    senderId = myIdHex,
                    content = text,
                    timestampMs = now,
                    counter = counter,
                    expirySeconds = expirySeconds,
                    expiryDeadlineMs = if (expirySeconds > 0) now + (expirySeconds * 1000L) else null,
                    isOutgoing = true,
                    state = MessageState.SENDING
                )

                // Step 2: persist to DB (row MUST exist before any state updates below)
                withContext(Dispatchers.IO) {
                    messageRepository.saveMessage(message, plaintextBytes)
                    // Update conversation lastMessageAt for home screen ordering
                    val existing = conversationDao.getConversation(convId)
                    if (existing != null) {
                        conversationDao.insertOrUpdate(existing.copy(lastMessageAt = now))
                    } else {
                        conversationDao.insertOrUpdate(
                            com.cryptika.messenger.data.local.db.ConversationEntity(
                                id = convId,
                                contactId = activeContact?.id ?: "",
                                lastMessageAt = now,
                                unreadCount = 0
                            )
                        )
                    }
                }

                // Step 3 & 4: send via appropriate WebSocket, then update state
                if (wirePacketBytes != null) {
                    val sendSuccess = withContext(Dispatchers.IO) {
                        if (isEphemeralMode && ephemeralSessionUUID != null) {
                            ephemeralSessionManager.sendPacket(
                                sessionUUID = ephemeralSessionUUID!!,
                                messageId = messageId,
                                packet = wirePacketBytes
                            )
                        } else {
                            backgroundConnectionManager.sendPacket(
                                convId = convId,
                                messageId = messageId,
                                packet = wirePacketBytes
                            )
                        }
                    }
                    val finalState = if (sendSuccess) MessageState.SENT else MessageState.FAILED
                    withContext(Dispatchers.IO) {
                        messageRepository.updateMessageState(messageId, finalState)
                    }
                    if (!sendSuccess) {
                        _events.emit(ChatEvent.ShowError("Message queued, will retry when reconnected"))
                    }
                }
                // wirePacketBytes == null: no-op, message stays as SENDING until session is established

            } catch (e: CryptoError) {
                _events.emit(ChatEvent.ShowError("Encryption failed"))
            } catch (e: Exception) {
                _events.emit(ChatEvent.ShowError("Failed to send message"))
            }
        }
    }

    // ── Receive ──────────────────────────────────────────────────────────────

    /**
     * Decrypts and persists an incoming wire-packet received from the relay.
     * De-duplicates on [messageId] so relay retransmits are silently dropped.
     * Called from BGM's packet handler (already on a background thread).
     */
    private suspend fun receiveMessage(messageId: String, packetBytes: ByteArray) {
        if (!seenMessageIds.add(messageId)) return
        val processor = messageProcessor ?: return
        val convId = conversationId ?: return
        try {
            val (plaintextBytes, header) = withContext(Dispatchers.Default) { processor.receive(packetBytes) }
            val text = plaintextBytes.toString(Charsets.UTF_8)            // Handle delete-for-both requests sent by the other party
            if (text.startsWith("__DEL__:")) {
                val targetCounter = text.removePrefix("__DEL__:").toLongOrNull()
                if (targetCounter != null) {
                    val senderHex = header.senderId.toHexString()
                    withContext(Dispatchers.IO) {
                        try { messageRepository.deleteMessageByCounterAndSender(convId, senderHex, targetCounter) }
                        catch (_: Exception) {}
                    }
                }
                return
            }
            val now = System.currentTimeMillis()
            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderId = header.senderId.toHexString(),
                content = text,
                timestampMs = header.timestampMs,
                counter = header.counter,
                expirySeconds = header.expirySeconds,
                expiryDeadlineMs = if (header.expirySeconds > 0) now + (header.expirySeconds * 1000L) else null,
                isOutgoing = false,
                state = MessageState.DELIVERED,
                messageType = header.messageType
            )
            withContext(Dispatchers.IO) { messageRepository.saveMessage(message, plaintextBytes) }
        } catch (e: CryptoError.SignatureInvalid) { _events.emit(ChatEvent.MessageRejected("signature_invalid")) }
        catch (e: CryptoError.TimestampStale) { _events.emit(ChatEvent.MessageRejected("timestamp_stale")) }
        catch (e: CryptoError.ReplayDetected) { _events.emit(ChatEvent.MessageRejected("replay_detected")) }
        catch (e: CryptoError.AEADAuthFailed) { _events.emit(ChatEvent.MessageRejected("aead_auth_failed")) }
        catch (e: CryptoError) { _events.emit(ChatEvent.MessageRejected("crypto_error")) }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { _events.emit(ChatEvent.MessageRejected("parse_error")) }
    }

    /** Triggers a local expiry sweep: deletes expired messages and their keys from DB. */
    fun triggerLocalExpiry() {
        viewModelScope.launch(Dispatchers.IO) {
            try { messageRepository.deleteExpiredMessages() } catch (_: Exception) {}
        }
    }

    // �"��"� Retry �"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"��"�

    fun retryFailedMessages() {
        val convId = conversationId ?: return
        if (!backgroundConnectionManager.isConnected(convId)) return

        viewModelScope.launch {
            val failedMessages = withContext(Dispatchers.IO) {
                messageRepository.getFailedMessages(convId)
            }
            if (failedMessages.isEmpty()) return@launch

            _events.emit(ChatEvent.ShowError("${failedMessages.size} message(s) failed, will resend when session is ready"))
            failedMessages.forEach { message ->
                withContext(Dispatchers.IO) {
                    messageRepository.updateMessageState(message.id, MessageState.SENDING)
                }
            }
            _events.emit(ChatEvent.RetrySucceeded)
        }
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────

    // -- Expiry scheduling ------------------------------------------------------

    /**
     * Cancels any pending expiry job, then schedules a new one to fire at the nearest
     * upcoming [Message.expiryDeadlineMs].  When it fires it calls [deleteExpiredMessages]
     * which cleans up every message whose deadline has passed (including any that expired
     * while the coroutine was sleeping).  The resulting Room emission re-triggers this
     * function so the next nearest deadline is always scheduled.
     */
    private fun scheduleNextExpiry(messages: List<Message>) {
        nextExpiryJob?.cancel()
        val now = System.currentTimeMillis()
        val nextDeadline = messages
            .mapNotNull { it.expiryDeadlineMs }
            .filter { it > now }
            .minOrNull() ?: return
        val delayMs = (nextDeadline - now).coerceAtLeast(0L)
        nextExpiryJob = viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(delayMs)
            try { messageRepository.deleteExpiredMessages() } catch (_: Exception) {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        nextExpiryJob?.cancel()
        ephemeralCountdownJob?.cancel()
        if (isEphemeralMode) {
            ephemeralSessionUUID?.let { ephemeralSessionManager.unregisterChatHandler(it) }
        } else {
            conversationId?.let { backgroundConnectionManager.unregisterChatHandler(it) }
        }
    }

    // -- Retry connection -------------------------------------------------------

    // Delete a message locally, optionally sending a delete-request to the other device
    fun deleteMessage(messageId: String, forBoth: Boolean) {
        val convId = conversationId ?: return
        viewModelScope.launch {
            if (forBoth) {
                val processor = messageProcessor
                if (processor != null) {
                    try {
                        val msg = withContext(Dispatchers.IO) { messageRepository.getMessage(messageId) }
                        if (msg != null) {
                            val deletePayload = "__DEL__:${msg.counter}".toByteArray(Charsets.UTF_8)
                            val result = withContext(Dispatchers.Default) {
                                processor.send(plaintext = deletePayload, expirySeconds = 0)
                            }
                            val delMsgId = UUID.randomUUID().toString()
                            withContext(Dispatchers.IO) {
                                if (isEphemeralMode && ephemeralSessionUUID != null) {
                                    ephemeralSessionManager.sendPacket(
                                        sessionUUID = ephemeralSessionUUID!!,
                                        messageId = delMsgId,
                                        packet = result.first
                                    )
                                } else {
                                    backgroundConnectionManager.sendPacket(
                                        convId = convId,
                                        messageId = delMsgId,
                                        packet = result.first
                                    )
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }
            withContext(Dispatchers.IO) {
                try { messageRepository.deleteMessage(messageId) } catch (_: Exception) {}
            }
        }
    }

    /** Re-establish relay connection after a disconnection or URL change. */
    fun retryConnection() {
        if (isEphemeralMode) return  // ephemeral sessions cannot be manually retried
        val convId = conversationId ?: return
        val contact = activeContact ?: return
        val identity = myIdentity ?: return
        backgroundConnectionManager.forceReconnect(identity, contact)
        connectRelay(convId, identity, contact)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildConversationId(a: String, b: String): String {
        val sorted = listOf(a, b).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
}

// ══════════════════════════════════════════════════════════════════════════════
// SETTINGS VIEWMODEL
// ══════════════════════════════════════════════════════════════════════════════
data class SettingsUiState(
    val fingerprintHex: String = "",
    val screenshotBlockingEnabled: Boolean = true,
    val defaultExpirySeconds: Int = 1800,
    val appVersion: String = com.cryptika.messenger.domain.model.AppVersion.NAME,
    val showRegenerateConfirm: Boolean = false,
    val serverRelayUrl: String = "",
    val pingStatus: String = "",
    val isPinging: Boolean = false,
    val forceLogout: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val identityRepository: IdentityRepository,
    private val ephemeralSessionManager: com.cryptika.messenger.data.remote.EphemeralSessionManager,
    private val authRepository: AuthRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val serverConfig: com.cryptika.messenger.data.remote.ServerConfig
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("cryptika_settings", android.content.Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            screenshotBlockingEnabled = prefs.getBoolean("screenshot_blocking", true),
            defaultExpirySeconds = prefs.getInt("default_expiry_seconds", 1800),
            serverRelayUrl = serverConfig.relayBaseUrl
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val identity = identityRepository.getLocalIdentity() ?: return@launch
            _uiState.update {
                it.copy(fingerprintHex = identity.identityHex.chunked(8).joinToString(" "))
            }
        }
    }

    fun showRegenerateConfirm() = _uiState.update { it.copy(showRegenerateConfirm = true) }
    fun dismissRegenerateConfirm() = _uiState.update { it.copy(showRegenerateConfirm = false) }

    fun regenerateIdentity() {
        viewModelScope.launch(Dispatchers.IO) {
            // Destroy all active sessions first
            ephemeralSessionManager.destroyAllSessions()
            // Delete old identity and generate new one
            identityRepository.deleteIdentity()
            identityRepository.generateIdentity()
            // Logout — force re-register
            authRepository.logout()
            _uiState.update {
                it.copy(showRegenerateConfirm = false, forceLogout = true)
            }
        }
    }

    /**
     * Persists the screenshot blocking preference.
     * MainActivity reads this on onResume() and applies/clears FLAG_SECURE accordingly.
     */
    fun setScreenshotBlocking(enabled: Boolean) {
        prefs.edit().putBoolean("screenshot_blocking", enabled).apply()
        _uiState.update { it.copy(screenshotBlockingEnabled = enabled) }
    }

    fun setDefaultExpiry(seconds: Int) {
        prefs.edit().putInt("default_expiry_seconds", seconds).apply()
        _uiState.update { it.copy(defaultExpirySeconds = seconds) }
    }

    fun updateServerUrl(url: String) {
        serverConfig.relayBaseUrl = url
        _uiState.update { it.copy(serverRelayUrl = url, pingStatus = "") }
    }

    fun pingServer() {
        val apiBase = serverConfig.apiBaseUrl
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true, pingStatus = "") }
            try {
                val result = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    val url = java.net.URL("$apiBase/health")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val start = System.currentTimeMillis()
                    val code = conn.responseCode
                    val ms = System.currentTimeMillis() - start
                    conn.disconnect()
                    if (code == 200) "Connected ${ms}ms" else "Server returned HTTP $code"
                }
                _uiState.update { it.copy(pingStatus = result, isPinging = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        pingStatus = "Cannot reach server: ${e.message?.take(80)}",
                        isPinging = false
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------
// CALL VIEWMODEL
// ------------------------------------------------------------------------------

data class CallUiState(
    val contactName: String = "",
    val callState: CallState = CallState.IDLE,
    val callDurationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false
)

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callManager: CallManager,
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository,
    private val backgroundConnectionManager: com.cryptika.messenger.data.remote.BackgroundConnectionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    /** Mirror of CallManager.incomingCallData � for global nav observer in MainActivity. */
    val incomingCallData: StateFlow<IncomingCallData?> = callManager.incomingCallData

    private var durationJob: Job? = null

    init {
        // Track call state changes to update UI and start/stop the duration counter
        viewModelScope.launch {
            callManager.callState.collect { state ->
                _uiState.update { it.copy(callState = state) }
                when (state) {
                    CallState.ACTIVE -> startDurationCounter()
                    CallState.IDLE, CallState.ENDING -> {
                        stopDurationCounter()
                        _uiState.update { it.copy(callDurationSeconds = 0) }
                    }
                    else -> {}
                }
            }
        }
        // Mirror mute/speaker state from CallManager
        viewModelScope.launch {
            callManager.isMutedState.collect { muted ->
                _uiState.update { it.copy(isMuted = muted) }
            }
        }
        viewModelScope.launch {
            callManager.isSpeakerOn.collect { on ->
                _uiState.update { it.copy(isSpeakerOn = on) }
            }
        }
    }

    // -- Called when ChatScreen phone button is tapped (outgoing call) ---------

    fun startOutgoingCall(contactId: String) {
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) { contactRepository.getContact(contactId) }
                ?: return@launch
            val myIdentity = withContext(Dispatchers.IO) { identityRepository.getLocalIdentity() }
                ?: return@launch

            _uiState.update { it.copy(contactName = contact.displayName) }

            val sorted = listOf(myIdentity.identityHex, contact.identityHex).sorted()
            val convId = "${sorted[0]}_${sorted[1]}"

            // Ensure relay connection exists before sending the call offer
            backgroundConnectionManager.ensureConnected(myIdentity, contact)
            callManager.startCall(convId, contact)
        }
    }

    // -- Called from CallScreen when an incoming call is being displayed -------

    fun initIncomingCall(contactId: String) {
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) { contactRepository.getContact(contactId) }
            _uiState.update { it.copy(contactName = contact?.displayName ?: "Unknown") }
        }
    }

    fun answerCall()  = callManager.answerCall()
    fun rejectCall()  = callManager.rejectCall()
    fun hangup()      = callManager.hangup()
    fun toggleMute()  = callManager.toggleMute()
    fun toggleSpeaker() = callManager.toggleSpeaker()

    // -- Duration counter ------------------------------------------------------

    private fun startDurationCounter() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                kotlinx.coroutines.delay(1_000)
                seconds++
                _uiState.update { it.copy(callDurationSeconds = seconds) }
            }
        }
    }

    private fun stopDurationCounter() {
        durationJob?.cancel()
        durationJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopDurationCounter()
    }
}
