package com.cryptika.messenger.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/cryptika/messenger/domain/model/ConnectionState;", "", "(Ljava/lang/String;I)V", "CONNECTING", "CONNECTED_P2P", "CONNECTED_RELAY", "DISCONNECTED", "ERROR", "Cryptika_debug"})
public enum ConnectionState {
    /*public static final*/ CONNECTING /* = new CONNECTING() */,
    /*public static final*/ CONNECTED_P2P /* = new CONNECTED_P2P() */,
    /*public static final*/ CONNECTED_RELAY /* = new CONNECTED_RELAY() */,
    /*public static final*/ DISCONNECTED /* = new DISCONNECTED() */,
    /*public static final*/ ERROR /* = new ERROR() */;
    
    ConnectionState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.cryptika.messenger.domain.model.ConnectionState> getEntries() {
        return null;
    }
}