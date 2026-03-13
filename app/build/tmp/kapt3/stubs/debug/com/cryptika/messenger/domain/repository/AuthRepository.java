package com.cryptika.messenger.domain.repository;

import com.cryptika.messenger.data.remote.api.AcceptRequestResponse;
import com.cryptika.messenger.data.remote.api.AcceptedSession;
import com.cryptika.messenger.data.remote.api.PendingRequest;

/**
 * Repository for authentication and contact discovery operations.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\r\bf\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0007\u0010\bJ\u001c\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0003H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000b\u0010\fJ$\u0010\r\u001a\b\u0012\u0004\u0012\u00020\n0\u00032\u0006\u0010\u000e\u001a\u00020\u0006H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\bJ\"\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0003H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0013\u0010\fJ\n\u0010\u0014\u001a\u0004\u0018\u00010\u0006H&J\n\u0010\u0015\u001a\u0004\u0018\u00010\u0006H&J\"\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00110\u0003H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0018\u0010\fJ\n\u0010\u0019\u001a\u0004\u0018\u00010\u0006H&J\b\u0010\u001a\u001a\u00020\u001bH&J\b\u0010\u001c\u001a\u00020\u001bH&J\b\u0010\u001d\u001a\u00020\nH&J$\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\n0\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001f\u0010\bJ,\u0010 \u001a\b\u0012\u0004\u0012\u00020\n0\u00032\u0006\u0010!\u001a\u00020\u00062\u0006\u0010\"\u001a\u00020\u0006H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b#\u0010$J,\u0010%\u001a\b\u0012\u0004\u0012\u00020\n0\u00032\u0006\u0010&\u001a\u00020\u00062\u0006\u0010\"\u001a\u00020\u0006H\u00a6@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\'\u0010$\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006("}, d2 = {"Lcom/cryptika/messenger/domain/repository/AuthRepository;", "", "acceptRequest", "Lkotlin/Result;", "Lcom/cryptika/messenger/data/remote/api/AcceptRequestResponse;", "requestId", "", "acceptRequest-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "burnCredentials", "", "burnCredentials-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enter", "username", "enter-gIAlu-s", "getAcceptedSessions", "", "Lcom/cryptika/messenger/data/remote/api/AcceptedSession;", "getAcceptedSessions-IoAF18A", "getContactToken", "getJwtToken", "getPendingRequests", "Lcom/cryptika/messenger/data/remote/api/PendingRequest;", "getPendingRequests-IoAF18A", "getUsername", "isCredentialsBurned", "", "isLoggedIn", "logout", "rejectRequest", "rejectRequest-gIAlu-s", "sendContactRequest", "targetUsername", "nickname", "sendContactRequest-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendContactRequestByFingerprint", "targetIdentityHash", "sendContactRequestByFingerprint-0E7RQCE", "Cryptika_debug"})
public abstract interface AuthRepository {
    
    public abstract boolean isLoggedIn();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.String getUsername();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.String getContactToken();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.String getJwtToken();
    
    public abstract void logout();
    
    /**
     * Whether credentials have been burned (server-side deletion complete).
     */
    public abstract boolean isCredentialsBurned();
}