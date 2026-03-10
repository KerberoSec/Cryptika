package com.cryptika.messenger.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b%\b\u0086\b\u0018\u00002\u00020\u0001Ba\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\b\u0010\f\u001a\u0004\u0018\u00010\b\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\u0002\u0010\u0013J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\u0010H\u00c6\u0003J\t\u0010(\u001a\u00020\u0012H\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\bH\u00c6\u0003J\t\u0010-\u001a\u00020\bH\u00c6\u0003J\t\u0010.\u001a\u00020\u000bH\u00c6\u0003J\u0010\u0010/\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u001aJ\t\u00100\u001a\u00020\u000eH\u00c6\u0003J~\u00101\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u00c6\u0001\u00a2\u0006\u0002\u00102J\u0013\u00103\u001a\u00020\u000e2\b\u00104\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00105\u001a\u00020\u000bH\u00d6\u0001J\t\u00106\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0015\u0010\f\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u001b\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0015R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u001fR\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0015R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0018\u00a8\u00067"}, d2 = {"Lcom/cryptika/messenger/domain/model/Message;", "", "id", "", "conversationId", "senderId", "content", "timestampMs", "", "counter", "expirySeconds", "", "expiryDeadlineMs", "isOutgoing", "", "state", "Lcom/cryptika/messenger/domain/model/MessageState;", "messageType", "Lcom/cryptika/messenger/domain/model/MessageType;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJILjava/lang/Long;ZLcom/cryptika/messenger/domain/model/MessageState;Lcom/cryptika/messenger/domain/model/MessageType;)V", "getContent", "()Ljava/lang/String;", "getConversationId", "getCounter", "()J", "getExpiryDeadlineMs", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getExpirySeconds", "()I", "getId", "()Z", "getMessageType", "()Lcom/cryptika/messenger/domain/model/MessageType;", "getSenderId", "getState", "()Lcom/cryptika/messenger/domain/model/MessageState;", "getTimestampMs", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJILjava/lang/Long;ZLcom/cryptika/messenger/domain/model/MessageState;Lcom/cryptika/messenger/domain/model/MessageType;)Lcom/cryptika/messenger/domain/model/Message;", "equals", "other", "hashCode", "toString", "Cryptika_debug"})
public final class Message {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String conversationId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String senderId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String content = null;
    private final long timestampMs = 0L;
    private final long counter = 0L;
    private final int expirySeconds = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long expiryDeadlineMs = null;
    private final boolean isOutgoing = false;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.model.MessageState state = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.model.MessageType messageType = null;
    
    public Message(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String senderId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long timestampMs, long counter, int expirySeconds, @org.jetbrains.annotations.Nullable()
    java.lang.Long expiryDeadlineMs, boolean isOutgoing, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageState state, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageType messageType) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getConversationId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSenderId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getContent() {
        return null;
    }
    
    public final long getTimestampMs() {
        return 0L;
    }
    
    public final long getCounter() {
        return 0L;
    }
    
    public final int getExpirySeconds() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getExpiryDeadlineMs() {
        return null;
    }
    
    public final boolean isOutgoing() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageState getState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageType getMessageType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageState component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageType component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    public final long component5() {
        return 0L;
    }
    
    public final long component6() {
        return 0L;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component8() {
        return null;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.Message copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String senderId, @org.jetbrains.annotations.NotNull()
    java.lang.String content, long timestampMs, long counter, int expirySeconds, @org.jetbrains.annotations.Nullable()
    java.lang.Long expiryDeadlineMs, boolean isOutgoing, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageState state, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageType messageType) {
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