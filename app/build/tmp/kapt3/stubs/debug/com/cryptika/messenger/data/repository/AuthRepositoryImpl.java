package com.cryptika.messenger.data.repository;

import android.util.Base64;
import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.*;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import kotlinx.coroutines.Dispatchers;
import org.json.JSONObject;
import retrofit2.HttpException;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0010\u0010\u0011J\b\u0010\u0012\u001a\u00020\u000fH\u0002J\u001c\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0015\u0010\u0016J$\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00140\f2\u0006\u0010\u0018\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0019\u0010\u0011J\"\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001c0\u001b0\fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001d\u0010\u0016J\n\u0010\u001e\u001a\u0004\u0018\u00010\u000fH\u0016J\n\u0010\u001f\u001a\u0004\u0018\u00010\u000fH\u0016J\"\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0\u001b0\fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\"\u0010\u0016J\n\u0010#\u001a\u0004\u0018\u00010\u000fH\u0016J\b\u0010$\u001a\u00020%H\u0016J\b\u0010&\u001a\u00020%H\u0016J\b\u0010\'\u001a\u00020\u0014H\u0016J$\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00140\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b)\u0010\u0011J,\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00140\f2\u0006\u0010+\u001a\u00020\u000f2\u0006\u0010,\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b-\u0010.R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006/"}, d2 = {"Lcom/cryptika/messenger/data/repository/AuthRepositoryImpl;", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "authApi", "Lcom/cryptika/messenger/data/remote/api/AuthApi;", "authStore", "Lcom/cryptika/messenger/data/local/AuthStore;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "(Lcom/cryptika/messenger/data/remote/api/AuthApi;Lcom/cryptika/messenger/data/local/AuthStore;Lcom/cryptika/messenger/data/remote/ServerConfig;Lcom/cryptika/messenger/domain/repository/IdentityRepository;)V", "acceptRequest", "Lkotlin/Result;", "Lcom/cryptika/messenger/data/remote/api/AcceptRequestResponse;", "requestId", "", "acceptRequest-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "authHeader", "burnCredentials", "", "burnCredentials-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enter", "username", "enter-gIAlu-s", "getAcceptedSessions", "", "Lcom/cryptika/messenger/data/remote/api/AcceptedSession;", "getAcceptedSessions-IoAF18A", "getContactToken", "getJwtToken", "getPendingRequests", "Lcom/cryptika/messenger/data/remote/api/PendingRequest;", "getPendingRequests-IoAF18A", "getUsername", "isCredentialsBurned", "", "isLoggedIn", "logout", "rejectRequest", "rejectRequest-gIAlu-s", "sendContactRequest", "targetUsername", "nickname", "sendContactRequest-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Cryptika_debug"})
public final class AuthRepositoryImpl implements com.cryptika.messenger.domain.repository.AuthRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.api.AuthApi authApi = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.AuthStore authStore = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.remote.ServerConfig serverConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.repository.IdentityRepository identityRepository = null;
    
    @javax.inject.Inject()
    public AuthRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.AuthApi authApi, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.AuthStore authStore, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository) {
        super();
    }
    
    private final java.lang.String authHeader() {
        return null;
    }
    
    @java.lang.Override()
    public boolean isLoggedIn() {
        return false;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.String getUsername() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.String getContactToken() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.String getJwtToken() {
        return null;
    }
    
    @java.lang.Override()
    public void logout() {
    }
    
    @java.lang.Override()
    public boolean isCredentialsBurned() {
        return false;
    }
}