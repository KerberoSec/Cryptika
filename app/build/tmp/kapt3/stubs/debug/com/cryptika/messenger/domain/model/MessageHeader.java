package com.cryptika.messenger.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B/\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0019\u001a\u00020\nH\u00c6\u0003J;\u0010\u001a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001J\u0013\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\bH\u00d6\u0001J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\r\u00a8\u0006!"}, d2 = {"Lcom/cryptika/messenger/domain/model/MessageHeader;", "", "senderId", "", "timestampMs", "", "counter", "expirySeconds", "", "messageType", "Lcom/cryptika/messenger/domain/model/MessageType;", "([BJJILcom/cryptika/messenger/domain/model/MessageType;)V", "getCounter", "()J", "getExpirySeconds", "()I", "getMessageType", "()Lcom/cryptika/messenger/domain/model/MessageType;", "getSenderId", "()[B", "getTimestampMs", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "", "Cryptika_debug"})
public final class MessageHeader {
    @org.jetbrains.annotations.NotNull()
    private final byte[] senderId = null;
    private final long timestampMs = 0L;
    private final long counter = 0L;
    private final int expirySeconds = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.model.MessageType messageType = null;
    
    public MessageHeader(@org.jetbrains.annotations.NotNull()
    byte[] senderId, long timestampMs, long counter, int expirySeconds, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.MessageType messageType) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] getSenderId() {
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
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageType getMessageType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] component1() {
        return null;
    }
    
    public final long component2() {
        return 0L;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageType component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.model.MessageHeader copy(@org.jetbrains.annotations.NotNull()
    byte[] senderId, long timestampMs, long counter, int expirySeconds, @org.jetbrains.annotations.NotNull()
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