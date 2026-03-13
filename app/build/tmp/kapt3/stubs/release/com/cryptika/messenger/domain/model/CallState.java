package com.cryptika.messenger.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/cryptika/messenger/domain/model/CallState;", "", "(Ljava/lang/String;I)V", "IDLE", "OUTGOING_RINGING", "INCOMING_RINGING", "ACTIVE", "ENDING", "Cryptika_release"})
public enum CallState {
    /*public static final*/ IDLE /* = new IDLE() */,
    /*public static final*/ OUTGOING_RINGING /* = new OUTGOING_RINGING() */,
    /*public static final*/ INCOMING_RINGING /* = new INCOMING_RINGING() */,
    /*public static final*/ ACTIVE /* = new ACTIVE() */,
    /*public static final*/ ENDING /* = new ENDING() */;
    
    CallState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.cryptika.messenger.domain.model.CallState> getEntries() {
        return null;
    }
}