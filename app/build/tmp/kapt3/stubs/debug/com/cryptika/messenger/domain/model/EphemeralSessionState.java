package com.cryptika.messenger.domain.model;

/**
 * Observable state for the ephemeral session countdown.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0003\u0003\u0004\u0005B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0003\u0006\u0007\b\u00a8\u0006\t"}, d2 = {"Lcom/cryptika/messenger/domain/model/EphemeralSessionState;", "", "()V", "Active", "Expired", "None", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState$Active;", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState$Expired;", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState$None;", "Cryptika_debug"})
public abstract class EphemeralSessionState {
    
    private EphemeralSessionState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u00d6\u0003J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"Lcom/cryptika/messenger/domain/model/EphemeralSessionState$Active;", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState;", "remainingMs", "", "expiresAt", "(JJ)V", "getExpiresAt", "()J", "getRemainingMs", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "Cryptika_debug"})
    public static final class Active extends com.cryptika.messenger.domain.model.EphemeralSessionState {
        private final long remainingMs = 0L;
        private final long expiresAt = 0L;
        
        public Active(long remainingMs, long expiresAt) {
        }
        
        public final long getRemainingMs() {
            return 0L;
        }
        
        public final long getExpiresAt() {
            return 0L;
        }
        
        public final long component1() {
            return 0L;
        }
        
        public final long component2() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.domain.model.EphemeralSessionState.Active copy(long remainingMs, long expiresAt) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/EphemeralSessionState$Expired;", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState;", "()V", "Cryptika_debug"})
    public static final class Expired extends com.cryptika.messenger.domain.model.EphemeralSessionState {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.EphemeralSessionState.Expired INSTANCE = null;
        
        private Expired() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/EphemeralSessionState$None;", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState;", "()V", "Cryptika_debug"})
    public static final class None extends com.cryptika.messenger.domain.model.EphemeralSessionState {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.EphemeralSessionState.None INSTANCE = null;
        
        private None() {
        }
    }
}