package com.cryptika.messenger.data.repository;

import com.cryptika.messenger.data.local.db.*;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.domain.crypto.IdentityHash;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.*;
import kotlinx.coroutines.flow.Flow;
import java.security.MessageDigest;
import java.util.UUID;
import javax.inject.Inject;
import android.util.Base64;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u000eJ\u000e\u0010\u000f\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u000eJ&\u0010\u0013\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\u0017H\u0096@\u00a2\u0006\u0002\u0010\u0018J\u001c\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\n0\u001a2\u0006\u0010\r\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u000eJ\u001e\u0010\u001b\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u001cJ\u0018\u0010\u001d\u001a\u0004\u0018\u00010\n2\u0006\u0010\u001e\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u000eJ\u001c\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u001a0 2\u0006\u0010\r\u001a\u00020\bH\u0016J\u001e\u0010!\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\"\u001a\u00020#H\u0096@\u00a2\u0006\u0002\u0010$J\u001e\u0010%\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\b2\u0006\u0010&\u001a\u00020\'H\u0096@\u00a2\u0006\u0002\u0010(J\u0016\u0010)\u001a\u00020\n*\u00020*2\b\u0010+\u001a\u0004\u0018\u00010\bH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/cryptika/messenger/data/repository/MessageRepositoryImpl;", "Lcom/cryptika/messenger/domain/repository/MessageRepository;", "dao", "Lcom/cryptika/messenger/data/local/db/MessageDao;", "keystoreManager", "Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;", "(Lcom/cryptika/messenger/data/local/db/MessageDao;Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;)V", "buildHeaderJson", "", "message", "Lcom/cryptika/messenger/domain/model/Message;", "deleteConversationMessages", "", "conversationId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteExpiredMessages", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessage", "messageId", "deleteMessageByCounterAndSender", "convId", "senderHex", "counter", "", "(Ljava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFailedMessages", "", "getMaxCounter", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMessage", "id", "getMessages", "Lkotlinx/coroutines/flow/Flow;", "saveMessage", "plaintextBytes", "", "(Lcom/cryptika/messenger/domain/model/Message;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMessageState", "state", "Lcom/cryptika/messenger/domain/model/MessageState;", "(Ljava/lang/String;Lcom/cryptika/messenger/domain/model/MessageState;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toDomain", "Lcom/cryptika/messenger/data/local/db/MessageEntity;", "plaintext", "Cryptika_debug"})
public final class MessageRepositoryImpl implements com.cryptika.messenger.domain.repository.MessageRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.db.MessageDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.keystore.KeystoreManager keystoreManager = null;
    
    @javax.inject.Inject()
    public MessageRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.MessageDao dao, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.keystore.KeystoreManager keystoreManager) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.cryptika.messenger.domain.model.Message>> getMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object saveMessage(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Message message, @org.jetbrains.annotations.NotNull()
    byte[] plaintextBytes, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.Message> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteExpiredMessages(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteMessageByCounterAndSender(@org.jetbrains.annotations.NotNull()
    java.lang.String convId, @org.jetbrains.annotations.NotNull()
    java.lang.String senderHex, long counter, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteConversationMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMaxCounter(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String senderHex, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateMessageState(@org.jetbrains.annotations.NotNull()
    java.lang.String messageId, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageState state, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getFailedMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.cryptika.messenger.domain.model.Message>> $completion) {
        return null;
    }
    
    private final com.cryptika.messenger.domain.model.Message toDomain(com.cryptika.messenger.data.local.db.MessageEntity $this$toDomain, java.lang.String plaintext) {
        return null;
    }
    
    private final java.lang.String buildHeaderJson(com.cryptika.messenger.domain.model.Message message) {
        return null;
    }
}