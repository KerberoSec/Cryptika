package com.cryptika.messenger.data.remote.websocket;

import com.cryptika.messenger.data.remote.ServerConfig;
import kotlinx.coroutines.channels.BufferOverflow;
import kotlinx.coroutines.flow.SharedFlow;
import okio.ByteString;
import android.util.Log;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0004\u0003\u0004\u0005\u0006B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0004\u0007\b\t\n\u00a8\u0006\u000b"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayEvent;", "", "()V", "Connected", "Disconnected", "Error", "MessageReceived", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$Connected;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$Disconnected;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$Error;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$MessageReceived;", "Cryptika_release"})
public abstract class RelayEvent {
    
    private RelayEvent() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$Connected;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent;", "()V", "Cryptika_release"})
    public static final class Connected extends com.cryptika.messenger.data.remote.websocket.RelayEvent {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.data.remote.websocket.RelayEvent.Connected INSTANCE = null;
        
        private Connected() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$Disconnected;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent;", "()V", "Cryptika_release"})
    public static final class Disconnected extends com.cryptika.messenger.data.remote.websocket.RelayEvent {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.data.remote.websocket.RelayEvent.Disconnected INSTANCE = null;
        
        private Disconnected() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$Error;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent;", "throwable", "", "(Ljava/lang/Throwable;)V", "getThrowable", "()Ljava/lang/Throwable;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "Cryptika_release"})
    public static final class Error extends com.cryptika.messenger.data.remote.websocket.RelayEvent {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.Throwable throwable = null;
        
        public Error(@org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.Throwable getThrowable() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.Throwable component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RelayEvent.Error copy(@org.jetbrains.annotations.NotNull()
        java.lang.Throwable throwable) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/cryptika/messenger/data/remote/websocket/RelayEvent$MessageReceived;", "Lcom/cryptika/messenger/data/remote/websocket/RelayEvent;", "message", "Lcom/cryptika/messenger/data/remote/websocket/RawRelayMessage;", "(Lcom/cryptika/messenger/data/remote/websocket/RawRelayMessage;)V", "getMessage", "()Lcom/cryptika/messenger/data/remote/websocket/RawRelayMessage;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "Cryptika_release"})
    public static final class MessageReceived extends com.cryptika.messenger.data.remote.websocket.RelayEvent {
        @org.jetbrains.annotations.NotNull()
        private final com.cryptika.messenger.data.remote.websocket.RawRelayMessage message = null;
        
        public MessageReceived(@org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.data.remote.websocket.RawRelayMessage message) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RawRelayMessage getMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RawRelayMessage component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.remote.websocket.RelayEvent.MessageReceived copy(@org.jetbrains.annotations.NotNull()
        com.cryptika.messenger.data.remote.websocket.RawRelayMessage message) {
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