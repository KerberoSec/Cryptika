package com.cryptika.messenger.presentation.viewmodel;

import android.util.Base64;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.websocket.RelayEvent;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.*;
import dagger.hilt.android.lifecycle.HiltViewModel;
import com.cryptika.messenger.data.remote.CallManager;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.*;
import java.util.UUID;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00d2\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010#\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0006\b\u0007\u0018\u0000 c2\u00020\u00012\u00020\u0002:\u0001cBY\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0012\u0012\u0006\u0010\u0013\u001a\u00020\u0014\u0012\b\b\u0001\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\u0002\u0010\u0017J\u0018\u00106\u001a\u00020!2\u0006\u00107\u001a\u00020!2\u0006\u00108\u001a\u00020!H\u0002J \u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020!2\u0006\u0010<\u001a\u00020.2\u0006\u0010=\u001a\u00020\u001fH\u0002J\u0016\u0010>\u001a\u00020:2\u0006\u0010?\u001a\u00020!2\u0006\u0010@\u001a\u00020*J\u0006\u0010A\u001a\u00020:J\u000e\u0010B\u001a\u00020:2\u0006\u0010C\u001a\u00020!J\u000e\u0010D\u001a\u00020:2\u0006\u0010E\u001a\u00020!J\b\u0010F\u001a\u00020:H\u0014J\u0010\u0010G\u001a\u00020:2\u0006\u0010H\u001a\u00020IH\u0016J\u001e\u0010J\u001a\u00020:2\u0006\u0010?\u001a\u00020!2\u0006\u0010K\u001a\u00020LH\u0082@\u00a2\u0006\u0002\u0010MJ\u0006\u0010N\u001a\u00020:J\u0006\u0010O\u001a\u00020:J\u0016\u0010P\u001a\u00020:2\f\u0010Q\u001a\b\u0012\u0004\u0012\u00020S0RH\u0002J\u0006\u0010T\u001a\u00020:J\u0016\u0010U\u001a\u00020:2\u0006\u0010V\u001a\u00020*H\u0082@\u00a2\u0006\u0002\u0010WJ\u000e\u0010X\u001a\u00020:2\u0006\u0010V\u001a\u00020*J\u000e\u0010Y\u001a\u00020:2\u0006\u0010Z\u001a\u00020[J\u0010\u0010\\\u001a\u00020:2\u0006\u0010]\u001a\u00020^H\u0002J\u0006\u0010_\u001a\u00020:J\u000e\u0010`\u001a\u00020:2\u0006\u0010a\u001a\u00020!J\f\u0010b\u001a\u00020!*\u00020LH\u0002R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010 \u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010$\u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010%\u001a\b\u0012\u0004\u0012\u00020\u001a0&\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020*X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010+\u001a\u0004\u0018\u00010,X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010-\u001a\u0004\u0018\u00010.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010/\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u00100\u001a\b\u0012\u0004\u0012\u00020!01X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00102\u001a\b\u0012\u0004\u0012\u00020\u001d03\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105\u00a8\u0006d"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ChatViewModel;", "Landroidx/lifecycle/ViewModel;", "Landroidx/lifecycle/DefaultLifecycleObserver;", "messageRepository", "Lcom/cryptika/messenger/domain/repository/MessageRepository;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "relayApi", "Lcom/cryptika/messenger/data/remote/api/RelayApi;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "backgroundConnectionManager", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "ephemeralSessionManager", "Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "conversationDao", "Lcom/cryptika/messenger/data/local/db/ConversationDao;", "appContext", "Landroid/content/Context;", "(Lcom/cryptika/messenger/domain/repository/MessageRepository;Lcom/cryptika/messenger/domain/repository/ContactRepository;Lcom/cryptika/messenger/domain/repository/IdentityRepository;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/data/remote/api/RelayApi;Lcom/cryptika/messenger/data/remote/ServerConfig;Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;Lcom/cryptika/messenger/data/local/db/ConversationDao;Landroid/content/Context;)V", "_events", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ChatEvent;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ChatUiState;", "activeContact", "Lcom/cryptika/messenger/domain/model/Contact;", "conversationId", "", "ephemeralCountdownJob", "Lkotlinx/coroutines/Job;", "ephemeralSessionUUID", "events", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "isEphemeralMode", "", "messageProcessor", "Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "myIdentity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "nextExpiryJob", "seenMessageIds", "", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "buildConversationId", "a", "b", "connectRelay", "", "convId", "identity", "contact", "deleteMessage", "messageId", "forBoth", "exitCurrentChat", "initConversation", "contactId", "initEphemeralSession", "sessionUUID", "onCleared", "onStart", "owner", "Landroidx/lifecycle/LifecycleOwner;", "receiveMessage", "packetBytes", "", "(Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "retryConnection", "retryFailedMessages", "scheduleNextExpiry", "messages", "", "Lcom/cryptika/messenger/domain/model/Message;", "sendMessage", "sendScreenshotPolicyControl", "allow", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setAllowPeerScreenshots", "setExpirySeconds", "seconds", "", "startEphemeralCountdown", "expiresAt", "", "triggerLocalExpiry", "updateInputText", "text", "toHexString", "Companion", "Cryptika_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChatViewModel extends androidx.lifecycle.ViewModel implements androidx.lifecycle.DefaultLifecycleObserver {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.MessageRepository messageRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.ContactRepository contactRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.IdentityRepository identityRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.api.RelayApi relayApi = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.ServerConfig serverConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.db.ConversationDao conversationDao = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context appContext = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SCREENSHOT_CONTROL_PREFIX = "__SSCTRL__:";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SCREENSHOT_ALLOW = "ALLOW";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SCREENSHOT_DISALLOW = "DISALLOW";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.presentation.viewmodel.ChatUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ChatUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.cryptika.messenger.presentation.viewmodel.ChatEvent> _events = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.cryptika.messenger.presentation.viewmodel.ChatEvent> events = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.cryptika.messenger.domain.crypto.MessageProcessor messageProcessor;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile java.lang.String conversationId;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.cryptika.messenger.domain.model.Contact activeContact;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.cryptika.messenger.domain.model.LocalIdentity myIdentity;
    
    /**
     * In-memory de-duplication for relay messages processed this session.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> seenMessageIds = null;
    
    /**
     * True when this ChatViewModel is driving an ephemeral (contact-discovery) session.
     */
    @kotlin.jvm.Volatile()
    private volatile boolean isEphemeralMode = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile java.lang.String ephemeralSessionUUID;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job ephemeralCountdownJob;
    
    /**
     * Tracks the single scheduled-expiry coroutine.  We only ever need one: it fires at the
     * nearest upcoming deadline, calls deleteExpiredMessages() (which cleans ALL due messages),
     * and the Room flow re-emits triggering a reschedule for the next nearest deadline.
     */
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job nextExpiryJob;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.presentation.viewmodel.ChatViewModel.Companion Companion = null;
    
    @javax.inject.Inject()
    public ChatViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.MessageRepository messageRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.RelayApi relayApi, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.ConversationDao conversationDao, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context appContext) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.presentation.viewmodel.ChatUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.cryptika.messenger.presentation.viewmodel.ChatEvent> getEvents() {
        return null;
    }
    
    public final void initConversation(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId) {
    }
    
    /**
     * Initialises the chat in ephemeral mode, backed by [EphemeralSessionManager].
     * Messages use in-memory storage; the session auto-destroys after its TTL.
     */
    public final void initEphemeralSession(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID) {
    }
    
    private final void startEphemeralCountdown(long expiresAt) {
    }
    
    /**
     * Starts (or restarts) the WebSocket relay connection and the event-collection
     * coroutine.  Any previous relay session is cancelled first so we never accumulate
     * stale coroutines.
     */
    private final void connectRelay(java.lang.String convId, com.cryptika.messenger.domain.model.LocalIdentity identity, com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    @java.lang.Override()
    public void onStart(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.LifecycleOwner owner) {
    }
    
    public final void updateInputText(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    public final void setExpirySeconds(int seconds) {
    }
    
    /**
     * Encrypts and sends the current input text through the full cryptographic pipeline.
     *
     * Ordering (correct):
     *  1. Encrypt plaintext → wirePacketBytes
     *  2. Save message to DB as SENDING
     *  3. Send wirePacketBytes over relay
     *  4. Update message state to SENT or FAILED based on relay result
     */
    public final void sendMessage() {
    }
    
    /**
     * Decrypts and persists an incoming wire-packet received from the relay.
     * De-duplicates on [messageId] so relay retransmits are silently dropped.
     * Called from BGM's packet handler (already on a background thread).
     */
    private final java.lang.Object receiveMessage(java.lang.String messageId, byte[] packetBytes, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Triggers a local expiry sweep: deletes expired messages and their keys from DB.
     */
    public final void triggerLocalExpiry() {
    }
    
    public final void setAllowPeerScreenshots(boolean allow) {
    }
    
    private final java.lang.Object sendScreenshotPolicyControl(boolean allow, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void exitCurrentChat() {
    }
    
    public final void retryFailedMessages() {
    }
    
    /**
     * Cancels any pending expiry job, then schedules a new one to fire at the nearest
     * upcoming [Message.expiryDeadlineMs].  When it fires it calls [deleteExpiredMessages]
     * which cleans up every message whose deadline has passed (including any that expired
     * while the coroutine was sleeping).  The resulting Room emission re-triggers this
     * function so the next nearest deadline is always scheduled.
     */
    private final void scheduleNextExpiry(java.util.List<com.cryptika.messenger.domain.model.Message> messages) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    public final void deleteMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId, boolean forBoth) {
    }
    
    /**
     * Re-establish relay connection after a disconnection or URL change.
     */
    public final void retryConnection() {
    }
    
    private final java.lang.String buildConversationId(java.lang.String a, java.lang.String b) {
        return null;
    }
    
    private final java.lang.String toHexString(byte[] $this$toHexString) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ChatViewModel$Companion;", "", "()V", "SCREENSHOT_ALLOW", "", "SCREENSHOT_CONTROL_PREFIX", "SCREENSHOT_DISALLOW", "Cryptika_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}