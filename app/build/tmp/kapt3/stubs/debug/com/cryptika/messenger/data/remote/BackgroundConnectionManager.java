package com.cryptika.messenger.data.remote;

import android.content.Context;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.cryptika.messenger.MainActivity;
import com.cryptika.messenger.R;
import com.cryptika.messenger.data.local.db.ConversationDao;
import com.cryptika.messenger.data.local.db.ConversationEntity;
import com.cryptika.messenger.data.remote.api.PresenceRequest;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.data.remote.api.TicketRequest;
import com.cryptika.messenger.data.remote.websocket.RelayEvent;
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.Lazy;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.StateFlow;
import okhttp3.OkHttpClient;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

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
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00bc\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0018\b\u0007\u0018\u0000 _2\u00020\u0001:\u0002_`Bo\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u0012\u0006\u0010\u0016\u001a\u00020\u0017\u0012\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019\u00a2\u0006\u0002\u0010\u001bJ\u0018\u00105\u001a\u00020\u001e2\u0006\u00106\u001a\u00020\u001e2\u0006\u00107\u001a\u00020\u001eH\u0002J\u0006\u00108\u001a\u00020&J\u001e\u00109\u001a\u00020&2\u0006\u0010:\u001a\u00020-2\u0006\u0010;\u001a\u00020#H\u0082@\u00a2\u0006\u0002\u0010<J\u0016\u0010=\u001a\u00020&2\u0006\u0010:\u001a\u00020-H\u0082@\u00a2\u0006\u0002\u0010>J\u0016\u0010?\u001a\u00020&2\u0006\u0010@\u001a\u00020A2\u0006\u0010B\u001a\u00020CJ\b\u0010D\u001a\u00020&H\u0002J\u0016\u0010E\u001a\u00020&2\u0006\u0010@\u001a\u00020A2\u0006\u0010B\u001a\u00020CJ\u0010\u0010F\u001a\u0004\u0018\u0001032\u0006\u0010G\u001a\u00020\u001eJ\u000e\u0010H\u001a\u00020I2\u0006\u0010G\u001a\u00020\u001eJ&\u0010J\u001a\u00020&2\u0006\u0010:\u001a\u00020-2\u0006\u0010K\u001a\u00020\u001e2\u0006\u0010L\u001a\u00020#H\u0082@\u00a2\u0006\u0002\u0010MJ\b\u0010N\u001a\u00020&H\u0002J\u00a3\u0001\u0010O\u001a\u00020&2\u0006\u0010G\u001a\u00020\u001e2F\u0010P\u001aB\b\u0001\u0012\u0013\u0012\u00110\u001e\u00a2\u0006\f\b \u0012\b\b!\u0012\u0004\b\b(\"\u0012\u0013\u0012\u00110#\u00a2\u0006\f\b \u0012\b\b!\u0012\u0004\b\b($\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0%\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001f2\"\u0010Q\u001a\u001e\b\u0001\u0012\u0004\u0012\u000203\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0%\u0012\u0006\u0012\u0004\u0018\u00010\u00010(2\"\u0010R\u001a\u001e\b\u0001\u0012\u0004\u0012\u00020)\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0%\u0012\u0006\u0012\u0004\u0018\u00010\u00010(\u00a2\u0006\u0002\u0010SJ\b\u0010T\u001a\u00020&H\u0002J&\u0010U\u001a\u00020I2\u0006\u0010G\u001a\u00020\u001e2\u0006\u0010K\u001a\u00020\u001e2\u0006\u0010$\u001a\u00020#H\u0086@\u00a2\u0006\u0002\u0010VJ\u001e\u0010W\u001a\u00020&2\u0006\u0010X\u001a\u00020\u001e2\u0006\u0010Y\u001a\u00020\u001eH\u0082@\u00a2\u0006\u0002\u0010ZJ\u0006\u0010[\u001a\u00020&J\u0006\u0010\\\u001a\u00020&J\u000e\u0010]\u001a\u00020&2\u0006\u0010G\u001a\u00020\u001eJ\f\u0010^\u001a\u00020\u001e*\u00020#H\u0002R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000RZ\u0010\u001c\u001aN\u0012\u0004\u0012\u00020\u001e\u0012D\u0012B\b\u0001\u0012\u0013\u0012\u00110\u001e\u00a2\u0006\f\b \u0012\b\b!\u0012\u0004\b\b(\"\u0012\u0013\u0012\u00110#\u00a2\u0006\f\b \u0012\b\b!\u0012\u0004\b\b($\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0%\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001f0\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R6\u0010\'\u001a*\u0012\u0004\u0012\u00020\u001e\u0012 \u0012\u001e\b\u0001\u0012\u0004\u0012\u00020)\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0%\u0012\u0006\u0012\u0004\u0018\u00010\u00010(0\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020+X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\u001e\u0012\u0004\u0012\u00020-0\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010.\u001a\u00020/X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u00100\u001a\u000201X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000RE\u00102\u001a9\u0012\u0004\u0012\u00020\u001e\u0012/\u0012-\b\u0001\u0012\u0013\u0012\u001103\u00a2\u0006\f\b \u0012\b\b!\u0012\u0004\b\b(4\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0%\u0012\u0006\u0012\u0004\u0018\u00010\u00010(0\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006a"}, d2 = {"Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "", "context", "Landroid/content/Context;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "messageRepository", "Lcom/cryptika/messenger/domain/repository/MessageRepository;", "conversationDao", "Lcom/cryptika/messenger/data/local/db/ConversationDao;", "handshakeManager", "Lcom/cryptika/messenger/domain/crypto/HandshakeManager;", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "ticketManager", "Lcom/cryptika/messenger/domain/crypto/TicketManager;", "relayApi", "Lcom/cryptika/messenger/data/remote/api/RelayApi;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "okHttpClient", "Lokhttp3/OkHttpClient;", "callManager", "Ldagger/Lazy;", "Lcom/cryptika/messenger/data/remote/CallManager;", "(Landroid/content/Context;Lcom/cryptika/messenger/domain/repository/ContactRepository;Lcom/cryptika/messenger/domain/repository/IdentityRepository;Lcom/cryptika/messenger/domain/repository/MessageRepository;Lcom/cryptika/messenger/data/local/db/ConversationDao;Lcom/cryptika/messenger/domain/crypto/HandshakeManager;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/domain/crypto/TicketManager;Lcom/cryptika/messenger/data/remote/api/RelayApi;Lcom/cryptika/messenger/data/remote/ServerConfig;Lokhttp3/OkHttpClient;Ldagger/Lazy;)V", "chatPacketHandlers", "Ljava/util/concurrent/ConcurrentHashMap;", "", "Lkotlin/Function3;", "Lkotlin/ParameterName;", "name", "msgId", "", "packet", "Lkotlin/coroutines/Continuation;", "", "connStateCallbacks", "Lkotlin/Function2;", "Lcom/cryptika/messenger/domain/model/ConnectionState;", "connectivityManager", "Landroid/net/ConnectivityManager;", "convStates", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager$ConvState;", "networkCallback", "Landroid/net/ConnectivityManager$NetworkCallback;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "sessionReadyCallbacks", "Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "processor", "buildConvId", "a", "b", "clearMessageNotification", "completeHandshake", "state", "offerBytes", "(Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager$ConvState;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "connectConversation", "(Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager$ConvState;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ensureConnected", "identity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "contact", "Lcom/cryptika/messenger/domain/model/Contact;", "ensureMessageChannel", "forceReconnect", "getMessageProcessor", "convId", "isConnected", "", "receiveInBackground", "messageId", "packetBytes", "(Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager$ConvState;Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "reconnectAll", "registerChatHandler", "packetHandler", "onSessionReady", "onConnectionStateChange", "(Ljava/lang/String;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function2;)V", "registerNetworkCallback", "sendPacket", "(Ljava/lang/String;Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "showUnreadNotification", "senderName", "conversationId", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "startAllConnections", "stopAll", "unregisterChatHandler", "toHexString", "Companion", "ConvState", "Cryptika_debug"})
public final class BackgroundConnectionManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.ContactRepository contactRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.IdentityRepository identityRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.MessageRepository messageRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.db.ConversationDao conversationDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.HandshakeManager handshakeManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.TicketManager ticketManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.api.RelayApi relayApi = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.ServerConfig serverConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final dagger.Lazy<com.cryptika.messenger.data.remote.CallManager> callManager = null;
    
    /**
     * Long-lived supervisor scope that outlives any single ViewModel.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final android.net.ConnectivityManager connectivityManager = null;
    
    /**
     * Reconnects all dead conversations when the device regains internet access.
     * Registered in [startAllConnections] and unregistered in [stopAll].
     */
    @org.jetbrains.annotations.NotNull()
    private final android.net.ConnectivityManager.NetworkCallback networkCallback = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "BackgroundConnMgr";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String MSG_CHANNEL_ID = "cryptika_messages";
    private static final int MSG_NOTIFICATION_ID = 2001;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.cryptika.messenger.data.remote.BackgroundConnectionManager.ConvState> convStates = null;
    
    /**
     * Raw MESSAGE packet handler set by the active ChatViewModel.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, kotlin.jvm.functions.Function3<java.lang.String, byte[], kotlin.coroutines.Continuation<? super kotlin.Unit>, java.lang.Object>> chatPacketHandlers = null;
    
    /**
     * Notified when the DH handshake completes for a conversation.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, kotlin.jvm.functions.Function2<com.cryptika.messenger.domain.crypto.MessageProcessor, kotlin.coroutines.Continuation<? super kotlin.Unit>, java.lang.Object>> sessionReadyCallbacks = null;
    
    /**
     * Notified on connection state changes.
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, kotlin.jvm.functions.Function2<com.cryptika.messenger.domain.model.ConnectionState, kotlin.coroutines.Continuation<? super kotlin.Unit>, java.lang.Object>> connStateCallbacks = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.remote.BackgroundConnectionManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public BackgroundConnectionManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.MessageRepository messageRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.ConversationDao conversationDao, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.HandshakeManager handshakeManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.TicketManager ticketManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.RelayApi relayApi, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig, @org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
    dagger.Lazy<com.cryptika.messenger.data.remote.CallManager> callManager) {
        super();
    }
    
    /**
     * Called by ChatViewModel when the chat screen opens.
     * - Routes incoming MESSAGE packets to [packetHandler].
     * - Calls [onSessionReady] immediately if a session is already established,
     *  or later when the handshake completes.
     */
    public final void registerChatHandler(@org.jetbrains.annotations.NotNull()
    java.lang.String convId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function3<? super java.lang.String, ? super byte[], ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> packetHandler, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.cryptika.messenger.domain.crypto.MessageProcessor, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> onSessionReady, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.cryptika.messenger.domain.model.ConnectionState, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> onConnectionStateChange) {
    }
    
    public final void unregisterChatHandler(@org.jetbrains.annotations.NotNull()
    java.lang.String convId) {
    }
    
    /**
     * Returns the established [MessageProcessor] for [convId], or null.
     */
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.domain.crypto.MessageProcessor getMessageProcessor(@org.jetbrains.annotations.NotNull()
    java.lang.String convId) {
        return null;
    }
    
    /**
     * Transmits a wire-packet for [convId] via this manager's WebSocket.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object sendPacket(@org.jetbrains.annotations.NotNull()
    java.lang.String convId, @org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    byte[] packet, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    public final boolean isConnected(@org.jetbrains.annotations.NotNull()
    java.lang.String convId) {
        return false;
    }
    
    /**
     * Starts background connections for all known contacts.
     * Idempotent — safe to call multiple times.
     * Called from [CryptikaApp.onCreate].
     */
    public final void startAllConnections() {
    }
    
    /**
     * Ensures a background connection exists for the given conversation.
     * Idempotent — no-op if already connected.
     */
    public final void ensureConnected(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.LocalIdentity identity, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    /**
     * Reconnects all dead/idle conversations. Called when:
     * - The device regains network access (via [networkCallback])
     * - Any other trigger that knows connectivity has been restored
     */
    private final void reconnectAll() {
    }
    
    /**
     * Tears down and re-establishes the connection for a conversation.
     * Called by ChatViewModel's "Retry Connection" button.
     */
    public final void forceReconnect(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.LocalIdentity identity, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact contact) {
    }
    
    private final java.lang.Object connectConversation(com.cryptika.messenger.data.remote.BackgroundConnectionManager.ConvState state, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object completeHandshake(com.cryptika.messenger.data.remote.BackgroundConnectionManager.ConvState state, byte[] offerBytes, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object receiveInBackground(com.cryptika.messenger.data.remote.BackgroundConnectionManager.ConvState state, java.lang.String messageId, byte[] packetBytes, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void stopAll() {
    }
    
    private final void registerNetworkCallback() {
    }
    
    private final java.lang.String buildConvId(java.lang.String a, java.lang.String b) {
        return null;
    }
    
    private final java.lang.String toHexString(byte[] $this$toHexString) {
        return null;
    }
    
    private final void ensureMessageChannel() {
    }
    
    private final java.lang.Object showUnreadNotification(java.lang.String senderName, java.lang.String conversationId, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void clearMessageNotification() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager$Companion;", "", "()V", "MSG_CHANNEL_ID", "", "MSG_NOTIFICATION_ID", "", "TAG", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0002\b\u0005\n\u0002\u0010#\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010<\u001a\u00020=2\u0006\u0010>\u001a\u00020\rR\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\r0\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u001c\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0004\b \u0010!R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u001c\u0010$\u001a\u0004\u0018\u00010%X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\'\"\u0004\b(\u0010)R\u001c\u0010*\u001a\u0004\u0018\u00010+X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b,\u0010-\"\u0004\b.\u0010/R\u0017\u00100\u001a\b\u0012\u0004\u0012\u00020\u000301\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u001c\u00104\u001a\u0004\u0018\u000105X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b6\u00107\"\u0004\b8\u00109R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010;\u00a8\u0006?"}, d2 = {"Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager$ConvState;", "", "conversationId", "", "contact", "Lcom/cryptika/messenger/domain/model/Contact;", "myIdentity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "wsClient", "Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;", "(Ljava/lang/String;Lcom/cryptika/messenger/domain/model/Contact;Lcom/cryptika/messenger/domain/model/LocalIdentity;Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;)V", "_connState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/cryptika/messenger/domain/model/ConnectionState;", "collectionJob", "Lkotlinx/coroutines/Job;", "getCollectionJob", "()Lkotlinx/coroutines/Job;", "setCollectionJob", "(Lkotlinx/coroutines/Job;)V", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "getContact", "()Lcom/cryptika/messenger/domain/model/Contact;", "getConversationId", "()Ljava/lang/String;", "messageProcessor", "Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "getMessageProcessor", "()Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "setMessageProcessor", "(Lcom/cryptika/messenger/domain/crypto/MessageProcessor;)V", "getMyIdentity", "()Lcom/cryptika/messenger/domain/model/LocalIdentity;", "ourEphemeralPair", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "getOurEphemeralPair", "()Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "setOurEphemeralPair", "(Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;)V", "ourOfferPacket", "", "getOurOfferPacket", "()[B", "setOurOfferPacket", "([B)V", "seenMessageIds", "", "getSeenMessageIds", "()Ljava/util/Set;", "verifiedTicket", "Lcom/cryptika/messenger/domain/model/VerifiedTicket;", "getVerifiedTicket", "()Lcom/cryptika/messenger/domain/model/VerifiedTicket;", "setVerifiedTicket", "(Lcom/cryptika/messenger/domain/model/VerifiedTicket;)V", "getWsClient", "()Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;", "updateConnectionState", "", "s", "Cryptika_debug"})
    public static final class ConvState {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String conversationId = null;
        @org.jetbrains.annotations.NotNull()
        private final com.cryptika.messenger.domain.model.Contact contact = null;
        @org.jetbrains.annotations.NotNull()
        private final com.cryptika.messenger.domain.model.LocalIdentity myIdentity = null;
        @org.jetbrains.annotations.NotNull()
        private final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient wsClient = null;
        @kotlin.jvm.Volatile()
        @org.jetbrains.annotations.Nullable()
        private volatile com.cryptika.messenger.domain.crypto.MessageProcessor messageProcessor;
        @kotlin.jvm.Volatile()
        @org.jetbrains.annotations.Nullable()
        private volatile com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair ourEphemeralPair;
        @kotlin.jvm.Volatile()
        @org.jetbrains.annotations.Nullable()
        private volatile byte[] ourOfferPacket;
        @kotlin.jvm.Volatile()
        @org.jetbrains.annotations.Nullable()
        private volatile com.cryptika.messenger.domain.model.VerifiedTicket verifiedTicket;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Set<java.lang.String> seenMessageIds = null;
        @org.jetbrains.annotations.Nullable()
        private kotlinx.coroutines.Job collectionJob;
        @org.jetbrains.annotations.NotNull()
        private final kotlinx.coroutines.flow.MutableStateFlow<com.cryptika.messenger.domain.model.ConnectionState> _connState = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.ConnectionState> connectionState = null;
        
        public ConvState(@org.jetbrains.annotations.NotNull()
        java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.domain.model.Contact contact, @org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.domain.model.LocalIdentity myIdentity, @org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient wsClient) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getConversationId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.domain.model.Contact getContact() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.domain.model.LocalIdentity getMyIdentity() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient getWsClient() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.crypto.MessageProcessor getMessageProcessor() {
            return null;
        }
        
        public final void setMessageProcessor(@org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.MessageProcessor p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair getOurEphemeralPair() {
            return null;
        }
        
        public final void setOurEphemeralPair(@org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final byte[] getOurOfferPacket() {
            return null;
        }
        
        public final void setOurOfferPacket(@org.jetbrains.annotations.Nullable()
        byte[] p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.model.VerifiedTicket getVerifiedTicket() {
            return null;
        }
        
        public final void setVerifiedTicket(@org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.model.VerifiedTicket p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Set<java.lang.String> getSeenMessageIds() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final kotlinx.coroutines.Job getCollectionJob() {
            return null;
        }
        
        public final void setCollectionJob(@org.jetbrains.annotations.Nullable()
        kotlinx.coroutines.Job p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final kotlinx.coroutines.flow.StateFlow<com.cryptika.messenger.domain.model.ConnectionState> getConnectionState() {
            return null;
        }
        
        public final void updateConnectionState(@org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.domain.model.ConnectionState s) {
        }
    }
}