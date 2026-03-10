package com.cryptika.messenger.domain.model;

/**
 * Byte codes embedded in the signal packet at offset [1].
 * Must never overlap with HandshakeManager.PACKET_TYPE (0x01) since signals
 * use magic byte 0x02 at offset [0] to distinguish themselves.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u0005\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u0000 \f2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\fB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000b\u00a8\u0006\r"}, d2 = {"Lcom/cryptika/messenger/domain/model/CallSignalType;", "", "code", "", "(Ljava/lang/String;IB)V", "getCode", "()B", "OFFER", "ANSWER", "REJECT", "HANGUP", "BUSY", "Companion", "Cryptika_debug"})
public enum CallSignalType {
    /*public static final*/ OFFER /* = new OFFER(0) */,
    /*public static final*/ ANSWER /* = new ANSWER(0) */,
    /*public static final*/ REJECT /* = new REJECT(0) */,
    /*public static final*/ HANGUP /* = new HANGUP(0) */,
    /*public static final*/ BUSY /* = new BUSY(0) */;
    private final byte code = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.domain.model.CallSignalType.Companion Companion = null;
    
    CallSignalType(byte code) {
    }
    
    public final byte getCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.cryptika.messenger.domain.model.CallSignalType> getEntries() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0005\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/cryptika/messenger/domain/model/CallSignalType$Companion;", "", "()V", "fromByte", "Lcom/cryptika/messenger/domain/model/CallSignalType;", "b", "", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.cryptika.messenger.domain.model.CallSignalType fromByte(byte b) {
            return null;
        }
    }
}