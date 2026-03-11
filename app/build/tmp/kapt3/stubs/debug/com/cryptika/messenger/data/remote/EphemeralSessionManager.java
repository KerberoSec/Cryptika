package com.cryptika.messenger.data.remote;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.hilt.android.qualifiers.ApplicationContext;
import android.content.Context;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.StateFlow;
import okhttp3.OkHttpClient;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tracks ephemeral anonymous sessions created via contact accept flow.
 *
 * Each session:
 * - Has a UUID for WebSocket routing (replaces conversation ID)
 * - Lives for exactly 30 minutes (server-enforced, client-enforced)
 * - On expiry: all crypto material, message history, and contact data are wiped
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00b4\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u0019\b\u0007\u0018\u0000 [2\u00020\u0001:\u0002[\\BY\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\u0006\u0010\u0012\u001a\u00020\u0013\u0012\u0006\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\u0002\u0010\u0016J\u001e\u00106\u001a\u00020,2\u0006\u00107\u001a\u0002052\u0006\u00108\u001a\u00020\u0018H\u0082@\u00a2\u0006\u0002\u00109J \u0010:\u001a\u00020,2\u0006\u00107\u001a\u0002052\u0006\u0010;\u001a\u00020\u001a2\u0006\u0010<\u001a\u00020=H\u0002J\u0006\u0010>\u001a\u00020,J\u0016\u0010?\u001a\u00020,2\u0006\u0010@\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010AJ\u0010\u0010B\u001a\u0004\u0018\u00010\u001a2\u0006\u0010@\u001a\u00020\u001aJ\u000e\u0010C\u001a\u00020D2\u0006\u0010@\u001a\u00020\u001aJ\u0010\u0010E\u001a\u0004\u0018\u0001032\u0006\u0010@\u001a\u00020\u001aJ\u0010\u0010F\u001a\u00020#2\u0006\u0010G\u001a\u00020\u0018H\u0002J6\u0010H\u001a\u00020,2\u0006\u0010@\u001a\u00020\u001a2\u0006\u0010I\u001a\u00020D2\u0006\u0010J\u001a\u00020\u001a2\u0006\u0010K\u001a\u00020\u001a2\u0006\u0010L\u001a\u00020\u001aH\u0086@\u00a2\u0006\u0002\u0010MJ\u0081\u0001\u0010N\u001a\u00020,2\u0006\u0010@\u001a\u00020\u001a2(\u0010O\u001a$\b\u0001\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u0018\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0+\u0012\u0006\u0012\u0004\u0018\u00010\u00010&2\"\u0010P\u001a\u001e\b\u0001\u0012\u0004\u0012\u000203\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0+\u0012\u0006\u0012\u0004\u0018\u00010\u0001022\u001e\b\u0002\u0010Q\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0+\u0012\u0006\u0012\u0004\u0018\u00010\u00010.\u00a2\u0006\u0002\u0010RJ&\u0010S\u001a\u00020#2\u0006\u0010@\u001a\u00020\u001a2\u0006\u0010T\u001a\u00020\u001a2\u0006\u0010*\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010UJ\u000e\u0010V\u001a\u00020,H\u0082@\u00a2\u0006\u0002\u0010WJ\u000e\u0010X\u001a\u00020,2\u0006\u0010@\u001a\u00020\u001aJ\b\u0010Y\u001a\u00020,H\u0002J\f\u0010Z\u001a\u00020\u0018*\u00020\u001aH\u0002R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u001d0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u001d0\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020#X\u0082\u000e\u00a2\u0006\u0002\n\u0000RZ\u0010$\u001aN\u0012\u0004\u0012\u00020\u001a\u0012D\u0012B\b\u0001\u0012\u0013\u0012\u00110\u001a\u00a2\u0006\f\b\'\u0012\b\b(\u0012\u0004\b\b()\u0012\u0013\u0012\u00110\u0018\u00a2\u0006\f\b\'\u0012\b\b(\u0012\u0004\b\b(*\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0+\u0012\u0006\u0012\u0004\u0018\u00010\u00010&0%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R0\u0010-\u001a$\u0012\u0004\u0012\u00020\u001a\u0012\u001a\u0012\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0+\u0012\u0006\u0012\u0004\u0018\u00010\u00010.0%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u000200X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R6\u00101\u001a*\u0012\u0004\u0012\u00020\u001a\u0012 \u0012\u001e\b\u0001\u0012\u0004\u0012\u000203\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0+\u0012\u0006\u0012\u0004\u0018\u00010\u0001020%X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u00104\u001a\u000e\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u0002050%X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006]"}, d2 = {"Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "", "context", "Landroid/content/Context;", "authStore", "Lcom/cryptika/messenger/data/local/AuthStore;", "okHttpClient", "Lokhttp3/OkHttpClient;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "contactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "messageRepository", "Lcom/cryptika/messenger/domain/repository/MessageRepository;", "handshakeManager", "Lcom/cryptika/messenger/domain/crypto/HandshakeManager;", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "authRepository", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "(Landroid/content/Context;Lcom/cryptika/messenger/data/local/AuthStore;Lokhttp3/OkHttpClient;Lcom/cryptika/messenger/data/remote/ServerConfig;Lcom/cryptika/messenger/domain/repository/ContactRepository;Lcom/cryptika/messenger/domain/repository/IdentityRepository;Lcom/cryptika/messenger/domain/repository/MessageRepository;Lcom/cryptika/messenger/domain/crypto/HandshakeManager;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;Lcom/cryptika/messenger/domain/repository/AuthRepository;)V", "PEER_DISCONNECTED_PREFIX", "", "PEER_DISCONNECTED_TAG", "", "_activeSessions", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "activeSessions", "Lkotlinx/coroutines/flow/StateFlow;", "getActiveSessions", "()Lkotlinx/coroutines/flow/StateFlow;", "burnTriggered", "", "chatPacketHandlers", "Ljava/util/concurrent/ConcurrentHashMap;", "Lkotlin/Function3;", "Lkotlin/ParameterName;", "name", "msgId", "packet", "Lkotlin/coroutines/Continuation;", "", "peerDisconnectedCallbacks", "Lkotlin/Function1;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "sessionReadyCallbacks", "Lkotlin/Function2;", "Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "sessions", "Lcom/cryptika/messenger/data/remote/EphemeralSessionManager$EphemeralSession;", "completeHandshake", "session", "offerPacket", "(Lcom/cryptika/messenger/data/remote/EphemeralSessionManager$EphemeralSession;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "connectSession", "jwtToken", "identity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "destroyAllSessions", "destroySession", "sessionUUID", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContactId", "getExpiresAt", "", "getMessageProcessor", "isPeerDisconnectedSignal", "data", "joinSession", "expiresAt", "peerIdentityHash", "peerPublicKeyB64", "peerNickname", "(Ljava/lang/String;JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "registerChatHandler", "packetHandler", "onSessionReady", "onPeerDisconnected", "(Ljava/lang/String;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;)V", "sendPacket", "messageId", "(Ljava/lang/String;Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "triggerBurnOnFirstMessage", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "unregisterChatHandler", "updateActiveSessionsList", "hexToBytes", "Companion", "EphemeralSession", "Cryptika_debug"})
public final class EphemeralSessionManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.AuthStore authStore = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient okHttpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.ServerConfig serverConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.ContactRepository contactRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.IdentityRepository identityRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.MessageRepository messageRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.HandshakeManager handshakeManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "EphemeralSessionMgr";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.cryptika.messenger.data.remote.EphemeralSessionManager.EphemeralSession> sessions = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<java.lang.String>> _activeSessions = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> activeSessions = null;
    @org.jetbrains.annotations.NotNull()
    private final byte[] PEER_DISCONNECTED_PREFIX = {(byte)-1, (byte)-2};
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String PEER_DISCONNECTED_TAG = "PEER_DISCONNECTED";
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, kotlin.jvm.functions.Function3<java.lang.String, byte[], kotlin.coroutines.Continuation<? super kotlin.Unit>, java.lang.Object>> chatPacketHandlers = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, kotlin.jvm.functions.Function2<com.cryptika.messenger.domain.crypto.MessageProcessor, kotlin.coroutines.Continuation<? super kotlin.Unit>, java.lang.Object>> sessionReadyCallbacks = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, kotlin.jvm.functions.Function1<kotlin.coroutines.Continuation<? super kotlin.Unit>, java.lang.Object>> peerDisconnectedCallbacks = null;
    
    /**
     * Burns server credentials once on the first successfully sent message.
     */
    @kotlin.jvm.Volatile()
    private volatile boolean burnTriggered = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.remote.EphemeralSessionManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public EphemeralSessionManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.AuthStore authStore, @org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.ContactRepository contactRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.MessageRepository messageRepository, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.HandshakeManager handshakeManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> getActiveSessions() {
        return null;
    }
    
    public final void registerChatHandler(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function3<? super java.lang.String, ? super byte[], ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> packetHandler, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.cryptika.messenger.domain.crypto.MessageProcessor, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> onSessionReady, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> onPeerDisconnected) {
    }
    
    public final void unregisterChatHandler(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID) {
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
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object joinSession(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID, long expiresAt, @org.jetbrains.annotations.NotNull()
    java.lang.String peerIdentityHash, @org.jetbrains.annotations.NotNull()
    java.lang.String peerPublicKeyB64, @org.jetbrains.annotations.NotNull()
    java.lang.String peerNickname, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void connectSession(com.cryptika.messenger.data.remote.EphemeralSessionManager.EphemeralSession session, java.lang.String jwtToken, com.cryptika.messenger.domain.model.LocalIdentity identity) {
    }
    
    private final java.lang.Object completeHandshake(com.cryptika.messenger.data.remote.EphemeralSessionManager.EphemeralSession session, byte[] offerPacket, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Send a packet via an ephemeral session's WebSocket. Triggers credential burn on first message.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object sendPacket(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID, @org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    byte[] packet, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object triggerBurnOnFirstMessage(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.cryptika.messenger.domain.crypto.MessageProcessor getMessageProcessor(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getContactId(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID) {
        return null;
    }
    
    public final long getExpiresAt(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID) {
        return 0L;
    }
    
    /**
     * Destroy a session — cryptographic erasure.
     * 1. Close WebSocket
     * 2. Zeroize all crypto material (DH keys, session keys, ratchet state)
     * 3. Delete all messages for this conversation from DB
     * 4. Delete the contact record
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object destroySession(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionUUID, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Destroy ALL sessions — called on logout or app wipe.
     */
    public final void destroyAllSessions() {
    }
    
    private final void updateActiveSessionsList() {
    }
    
    /**
     * Check if a received packet is the PEER_DISCONNECTED control frame.
     */
    private final boolean isPeerDisconnectedSignal(byte[] data) {
        return false;
    }
    
    private final byte[] hexToBytes(java.lang.String $this$hexToBytes) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/cryptika/messenger/data/remote/EphemeralSessionManager$Companion;", "", "()V", "TAG", "", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001d\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BI\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\u0002\u0010\u000fJ\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0006H\u00c6\u0003J\t\u0010&\u001a\u00020\bH\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000b\u0010)\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003JU\u0010*\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000eH\u00c6\u0001J\u0013\u0010+\u001a\u00020,2\b\u0010-\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010.\u001a\u00020/H\u00d6\u0001J\t\u00100\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001c\u0010\t\u001a\u0004\u0018\u00010\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u001c\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u001c\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0011R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"\u00a8\u00061"}, d2 = {"Lcom/cryptika/messenger/data/remote/EphemeralSessionManager$EphemeralSession;", "", "sessionUUID", "", "contactId", "expiresAt", "", "wsClient", "Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;", "destroyJob", "Lkotlinx/coroutines/Job;", "messageProcessor", "Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "ephemeralKeyPair", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "(Ljava/lang/String;Ljava/lang/String;JLcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;Lkotlinx/coroutines/Job;Lcom/cryptika/messenger/domain/crypto/MessageProcessor;Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;)V", "getContactId", "()Ljava/lang/String;", "getDestroyJob", "()Lkotlinx/coroutines/Job;", "setDestroyJob", "(Lkotlinx/coroutines/Job;)V", "getEphemeralKeyPair", "()Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;", "setEphemeralKeyPair", "(Lcom/cryptika/messenger/domain/crypto/SessionKeyManager$EphemeralKeyPair;)V", "getExpiresAt", "()J", "getMessageProcessor", "()Lcom/cryptika/messenger/domain/crypto/MessageProcessor;", "setMessageProcessor", "(Lcom/cryptika/messenger/domain/crypto/MessageProcessor;)V", "getSessionUUID", "getWsClient", "()Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", "Cryptika_debug"})
    public static final class EphemeralSession {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String sessionUUID = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String contactId = null;
        private final long expiresAt = 0L;
        @org.jetbrains.annotations.NotNull()
        private final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient wsClient = null;
        @org.jetbrains.annotations.Nullable()
        private kotlinx.coroutines.Job destroyJob;
        @kotlin.jvm.Volatile()
        @org.jetbrains.annotations.Nullable()
        private volatile com.cryptika.messenger.domain.crypto.MessageProcessor messageProcessor;
        @kotlin.jvm.Volatile()
        @org.jetbrains.annotations.Nullable()
        private volatile com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair ephemeralKeyPair;
        
        public EphemeralSession(@org.jetbrains.annotations.NotNull()
        java.lang.String sessionUUID, @org.jetbrains.annotations.NotNull()
        java.lang.String contactId, long expiresAt, @org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient wsClient, @org.jetbrains.annotations.Nullable()
        kotlinx.coroutines.Job destroyJob, @org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.MessageProcessor messageProcessor, @org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair ephemeralKeyPair) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSessionUUID() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getContactId() {
            return null;
        }
        
        public final long getExpiresAt() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient getWsClient() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final kotlinx.coroutines.Job getDestroyJob() {
            return null;
        }
        
        public final void setDestroyJob(@org.jetbrains.annotations.Nullable()
        kotlinx.coroutines.Job p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.crypto.MessageProcessor getMessageProcessor() {
            return null;
        }
        
        public final void setMessageProcessor(@org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.MessageProcessor p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair getEphemeralKeyPair() {
            return null;
        }
        
        public final void setEphemeralKeyPair(@org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        public final long component3() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient component4() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final kotlinx.coroutines.Job component5() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.crypto.MessageProcessor component6() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair component7() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.EphemeralSessionManager.EphemeralSession copy(@org.jetbrains.annotations.NotNull()
        java.lang.String sessionUUID, @org.jetbrains.annotations.NotNull()
        java.lang.String contactId, long expiresAt, @org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient wsClient, @org.jetbrains.annotations.Nullable()
        kotlinx.coroutines.Job destroyJob, @org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.MessageProcessor messageProcessor, @org.jetbrains.annotations.Nullable()
        com.cryptika.messenger.domain.crypto.SessionKeyManager.EphemeralKeyPair ephemeralKeyPair) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}