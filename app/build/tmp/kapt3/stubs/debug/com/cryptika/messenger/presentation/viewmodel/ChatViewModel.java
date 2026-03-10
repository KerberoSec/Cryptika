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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00bc\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010#\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002BI\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0001\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\u0002\u0010\u0013J\u0018\u0010.\u001a\u00020\u001d2\u0006\u0010/\u001a\u00020\u001d2\u0006\u00100\u001a\u00020\u001dH\u0002J \u00101\u001a\u0002022\u0006\u00103\u001a\u00020\u001d2\u0006\u00104\u001a\u00020%2\u0006\u00105\u001a\u00020\u001bH\u0002J\u0016\u00106\u001a\u0002022\u0006\u00107\u001a\u00020\u001d2\u0006\u00108\u001a\u000209J\u000e\u0010:\u001a\u0002022\u0006\u0010;\u001a\u00020\u001dJ\b\u0010<\u001a\u000202H\u0014J\u0010\u0010=\u001a\u0002022\u0006\u0010>\u001a\u00020?H\u0016J\u001e\u0010@\u001a\u0002022\u0006\u00107\u001a\u00020\u001d2\u0006\u0010A\u001a\u00020BH\u0082@\u00a2\u0006\u0002\u0010CJ\u0006\u0010D\u001a\u000202J\u0006\u0010E\u001a\u000202J\u0016\u0010F\u001a\u0002022\f\u0010G\u001a\b\u0012\u0004\u0012\u00020I0HH\u0002J\u0006\u0010J\u001a\u000202J\u000e\u0010K\u001a\u0002022\u0006\u0010L\u001a\u00020MJ\u0006\u0010N\u001a\u000202J\u000e\u0010O\u001a\u0002022\u0006\u0010P\u001a\u00020\u001dJ\f\u0010Q\u001a\u00020\u001d*\u00020BH\u0002R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00160\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00190\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00160\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010$\u001a\u0004\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010&\u001a\u0004\u0018\u00010\'X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010(\u001a\b\u0012\u0004\u0012\u00020\u001d0)X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00190+\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-\u00a8\u0006R"}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/ChatViewModel;", "Landroidx/lifecycle/ViewModel;", "Landroidx/lifecycle/DefaultLifecycleObserver;", "messageRepository", "Lcom/cryptika/messenger/domain/repository/MessageRepository;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "relayApi", "Lcom/cryptika/messenger/data/remote/api/RelayApi;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "backgroundConnectionManager", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "appContext", "Landroid/content/Context;", "(Lcom/cryptika/messenger/domain/repository/MessageRepository;Lcom/cryptika/messenger/domain/repository/ContactRepository;Lcom/cryptika/messenger/domain/repository/IdentityRepository;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/data/remote/api/RelayApi;Lcom/cryptika/messenger/data/remote/ServerConfig;Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;Landroid/content/Context;)V", "_events", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ChatEvent;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/presentation/viewmodel/ChatUiState;", "activeContact", "Lcom/cryptika/messenger/domain/model/Contact;", "conversationId", "", "events", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "messageProcessor", "Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "myIdentity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "nextExpiryJob", "Lkotlinx/coroutines/Job;", "seenMessageIds", "", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "buildConversationId", "a", "b", "connectRelay", "", "convId", "identity", "contact", "deleteMessage", "messageId", "forBoth", "", "initConversation", "contactId", "onCleared", "onStart", "owner", "Landroidx/lifecycle/LifecycleOwner;", "receiveMessage", "packetBytes", "", "(Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "retryConnection", "retryFailedMessages", "scheduleNextExpiry", "messages", "", "Lcom/cryptika/messenger/domain/model/Message;", "sendMessage", "setExpirySeconds", "seconds", "", "triggerLocalExpiry", "updateInputText", "text", "toHexString", "Cryptika_debug"})
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
    private final android.content.Context appContext = null;
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
     * Tracks the single scheduled-expiry coroutine.  We only ever need one: it fires at the
     * nearest upcoming deadline, calls deleteExpiredMessages() (which cleans ALL due messages),
     * and the Room flow re-emits triggering a reschedule for the next nearest deadline.
     */
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job nextExpiryJob;
    
    @javax.inject.Inject()
    public ChatViewModel(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.MessageRepository messageRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.RelayApi relayApi, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager, @dagger.hilt.android.qualifiers.ApplicationContext()
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
}