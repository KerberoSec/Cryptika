package com.cryptika.messenger.data.remote.websocket;

import com.cryptika.messenger.data.remote.ServerConfig;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.SharedFlow;
import okio.ByteString;
import android.util.Log;
import kotlinx.coroutines.Dispatchers;

/**
 * WebSocket client for the blind relay server.
 *
 * The relay only routes encrypted binary packets — it never inspects content.
 * Connection uses binary frames exclusively.
 *
 * Reconnect strategy: exponential backoff starting at 1s, max 30s.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0010\u0012\n\u0002\b\u0003\u0018\u0000 +2\u00020\u0001:\u0001+B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J \u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\u000b2\b\b\u0002\u0010!\u001a\u00020\u000bJ\"\u0010\"\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\u000b2\b\b\u0002\u0010!\u001a\u00020\u000bH\u0002J\u0006\u0010#\u001a\u00020\u001eJ\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010$\u001a\u00020\u0013J\"\u0010%\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\u000b2\b\b\u0002\u0010!\u001a\u00020\u000bH\u0002J&\u0010&\u001a\u00020\u00132\u0006\u0010\u001f\u001a\u00020\u000b2\u0006\u0010\'\u001a\u00020\u000b2\u0006\u0010(\u001a\u00020)H\u0086@\u00a2\u0006\u0002\u0010*R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;", "", "okHttpClient", "Lokhttp3/OkHttpClient;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "(Lokhttp3/OkHttpClient;Lcom/cryptika/messenger/data/remote/ServerConfig;)V", "_events", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent;", "currentAuthToken", "", "currentConversationId", "currentIdentityHash", "events", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "isConnected", "", "reconnectAttempts", "", "reconnectJob", "Lkotlinx/coroutines/Job;", "reconnectScope", "Lkotlinx/coroutines/CoroutineScope;", "shouldReconnect", "webSocket", "Lokhttp3/WebSocket;", "connect", "", "conversationId", "authToken", "identityHash", "connectInternal", "disconnect", "isReconnecting", "scheduleReconnect", "send", "messageId", "packet", "", "(Ljava/lang/String;Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "Cryptika_release"})
public final class RelayWebSocketClient {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.ServerConfig serverConfig = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "RelayWebSocket";
    private static final long MAX_BACKOFF_MS = 30000L;
    private static final long INITIAL_BACKOFF_MS = 1000L;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.cryptika.messenger.data.remote.websocket.RelayEvent> _events = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.cryptika.messenger.data.remote.websocket.RelayEvent> events = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile okhttp3.WebSocket webSocket;
    @kotlin.jvm.Volatile()
    private volatile boolean isConnected = false;
    @kotlin.jvm.Volatile()
    private volatile boolean shouldReconnect = true;
    private int reconnectAttempts = 0;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentConversationId;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentAuthToken;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentIdentityHash = "";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope reconnectScope = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job reconnectJob;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient.Companion Companion = null;
    
    public RelayWebSocketClient(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.cryptika.messenger.data.remote.websocket.RelayEvent> getEvents() {
        return null;
    }
    
    public final void connect(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String authToken, @org.jetbrains.annotations.NotNull()
    java.lang.String identityHash) {
    }
    
    public final void disconnect() {
    }
    
    public final boolean isConnected() {
        return false;
    }
    
    /**
     * True while a coroutine-based backoff reconnect delay is pending.
     */
    public final boolean isReconnecting() {
        return false;
    }
    
    /**
     * Sends raw wire packet bytes over the WebSocket.
     * The relay envelope wraps the packet with routing metadata.
     *
     * Envelope format (binary):
     *  [2 bytes] conversationId length
     *  [n bytes] conversationId UTF-8
     *  [2 bytes] messageId length
     *  [m bytes] messageId UTF-8
     *  [remaining] packet bytes
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object send(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    byte[] packet, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final void connectInternal(java.lang.String conversationId, java.lang.String authToken, java.lang.String identityHash) {
    }
    
    private final void scheduleReconnect(java.lang.String conversationId, java.lang.String authToken, java.lang.String identityHash) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient$Companion;", "", "()V", "INITIAL_BACKOFF_MS", "", "MAX_BACKOFF_MS", "TAG", "", "Cryptika_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}