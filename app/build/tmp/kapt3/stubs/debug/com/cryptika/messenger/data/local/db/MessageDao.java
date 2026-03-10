package com.cryptika.messenger.data.local.db;

import android.content.Context;
import androidx.room.*;
import androidx.sqlite.db.SupportSQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001c\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J \u0010\u0010\u001a\u0004\u0018\u00010\r2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0018\u0010\u0013\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J(\u0010\u0014\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0016\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u001c\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u00192\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0016\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u001e\u0010\u001d\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u00052\u0006\u0010\u001e\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0016\u0010\u001f\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006 "}, d2 = {"Lcom/cryptika/messenger/data/local/db/MessageDao;", "", "deleteConversationMessages", "", "conversationId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMessage", "id", "getExpiredMessages", "", "Lcom/cryptika/messenger/data/local/db/MessageEntity;", "nowMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFailedMessages", "getMaxCounter", "senderHex", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMessage", "getMessageByCounterAndSender", "convId", "counter", "(Ljava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMessages", "Lkotlinx/coroutines/flow/Flow;", "insertMessage", "message", "(Lcom/cryptika/messenger/data/local/db/MessageEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMessageState", "state", "zeroizeAndMarkUnrecoverable", "Cryptika_debug"})
@androidx.room.Dao()
public abstract interface MessageDao {
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestampMs ASC, rowid ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.cryptika.messenger.data.local.db.MessageEntity>> getMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.local.db.MessageEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :convId AND senderIdHex = :senderHex AND counter = :counter LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMessageByCounterAndSender(@org.jetbrains.annotations.NotNull()
    java.lang.String convId, @org.jetbrains.annotations.NotNull()
    java.lang.String senderHex, long counter, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.local.db.MessageEntity> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE expiryMs IS NOT NULL AND expiryMs <= :nowMs AND isDecryptable = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getExpiredMessages(long nowMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.cryptika.messenger.data.local.db.MessageEntity>> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.MessageEntity message, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE messages SET ciphertextBlob = x\'00\', isDecryptable = 0 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object zeroizeAndMarkUnrecoverable(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM messages WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM messages WHERE conversationId = :conversationId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteConversationMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT MAX(counter) FROM messages WHERE conversationId = :conversationId AND senderIdHex = :senderHex")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMaxCounter(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String senderHex, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    /**
     * Persists the delivery state of a message (SENT, FAILED, DELIVERED, etc.).
     */
    @androidx.room.Query(value = "UPDATE messages SET messageState = :state WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMessageState(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String state, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Returns outgoing messages in FAILED state — used by the retry mechanism.
     */
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :conversationId AND isOutgoing = 1 AND messageState = \'FAILED\' ORDER BY counter ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFailedMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.cryptika.messenger.data.local.db.MessageEntity>> $completion);
}