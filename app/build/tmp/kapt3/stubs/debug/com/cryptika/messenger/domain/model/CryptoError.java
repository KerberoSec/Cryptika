package com.cryptika.messenger.domain.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00060\u0001j\u0002`\u0002:\u000b\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000eB\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0003\u0082\u0001\u000b\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u00a8\u0006\u001a"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "()V", "AEADAuthFailed", "DhExchangeFailed", "InvalidPublicKey", "KeystoreError", "ReplayDetected", "SignatureInvalid", "StorageTampered", "TicketExpired", "TicketSignatureInvalid", "TimestampStale", "Unknown", "Lcom/cryptika/messenger/domain/model/CryptoError$AEADAuthFailed;", "Lcom/cryptika/messenger/domain/model/CryptoError$DhExchangeFailed;", "Lcom/cryptika/messenger/domain/model/CryptoError$InvalidPublicKey;", "Lcom/cryptika/messenger/domain/model/CryptoError$KeystoreError;", "Lcom/cryptika/messenger/domain/model/CryptoError$ReplayDetected;", "Lcom/cryptika/messenger/domain/model/CryptoError$SignatureInvalid;", "Lcom/cryptika/messenger/domain/model/CryptoError$StorageTampered;", "Lcom/cryptika/messenger/domain/model/CryptoError$TicketExpired;", "Lcom/cryptika/messenger/domain/model/CryptoError$TicketSignatureInvalid;", "Lcom/cryptika/messenger/domain/model/CryptoError$TimestampStale;", "Lcom/cryptika/messenger/domain/model/CryptoError$Unknown;", "Cryptika_debug"})
public abstract class CryptoError extends java.lang.Exception {
    
    private CryptoError() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$AEADAuthFailed;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class AEADAuthFailed extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.AEADAuthFailed INSTANCE = null;
        
        private AEADAuthFailed() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$DhExchangeFailed;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class DhExchangeFailed extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.DhExchangeFailed INSTANCE = null;
        
        private DhExchangeFailed() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$InvalidPublicKey;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class InvalidPublicKey extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.InvalidPublicKey INSTANCE = null;
        
        private InvalidPublicKey() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$KeystoreError;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class KeystoreError extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.KeystoreError INSTANCE = null;
        
        private KeystoreError() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$ReplayDetected;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class ReplayDetected extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.ReplayDetected INSTANCE = null;
        
        private ReplayDetected() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$SignatureInvalid;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class SignatureInvalid extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.SignatureInvalid INSTANCE = null;
        
        private SignatureInvalid() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$StorageTampered;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class StorageTampered extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.StorageTampered INSTANCE = null;
        
        private StorageTampered() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$TicketExpired;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class TicketExpired extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.TicketExpired INSTANCE = null;
        
        private TicketExpired() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$TicketSignatureInvalid;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class TicketSignatureInvalid extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.TicketSignatureInvalid INSTANCE = null;
        
        private TicketSignatureInvalid() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$TimestampStale;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "()V", "Cryptika_debug"})
    public static final class TimestampStale extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.NotNull()
        public static final com.cryptika.messenger.domain.model.CryptoError.TimestampStale INSTANCE = null;
        
        private TimestampStale() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u0004J\u000b\u0010\u0007\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0015\u0010\b\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0016\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/cryptika/messenger/domain/model/CryptoError$Unknown;", "Lcom/cryptika/messenger/domain/model/CryptoError;", "cause", "", "(Ljava/lang/Throwable;)V", "getCause", "()Ljava/lang/Throwable;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "Cryptika_debug"})
    public static final class Unknown extends com.cryptika.messenger.domain.model.CryptoError {
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Throwable cause = null;
        
        public Unknown(@org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.Nullable()
        public java.lang.Throwable getCause() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Throwable component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.domain.model.CryptoError.Unknown copy(@org.jetbrains.annotations.Nullable()
        java.lang.Throwable cause) {
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