package com.cryptika.messenger.data.remote.api;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J,\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\"\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\fJ\"\u0010\r\u001a\u00020\u000e2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\fJ\"\u0010\u000f\u001a\u00020\u00102\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\"\u0010\u0013\u001a\u00020\u00142\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\u0015H\u00a7@\u00a2\u0006\u0002\u0010\u0016J,\u0010\u0017\u001a\u00020\u00182\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ,\u0010\u001b\u001a\u00020\u001c2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\u001dH\u00a7@\u00a2\u0006\u0002\u0010\u001e\u00a8\u0006\u001f"}, d2 = {"Lcom/cryptika/messenger/data/remote/api/AuthApi;", "", "acceptContactRequest", "Lcom/cryptika/messenger/data/remote/api/AcceptRequestResponse;", "url", "", "auth", "request", "Lcom/cryptika/messenger/data/remote/api/AcceptRequestBody;", "(Ljava/lang/String;Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/AcceptRequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAcceptedSessions", "Lcom/cryptika/messenger/data/remote/api/AcceptedSessionsResponse;", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPendingRequests", "Lcom/cryptika/messenger/data/remote/api/PendingRequestsResponse;", "login", "Lcom/cryptika/messenger/data/remote/api/LoginResponse;", "Lcom/cryptika/messenger/data/remote/api/LoginRequest;", "(Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "register", "Lcom/cryptika/messenger/data/remote/api/RegisterResponse;", "Lcom/cryptika/messenger/data/remote/api/RegisterRequest;", "(Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/RegisterRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "rejectContactRequest", "Lcom/cryptika/messenger/data/remote/api/RejectRequestResponse;", "Lcom/cryptika/messenger/data/remote/api/RejectRequestBody;", "(Ljava/lang/String;Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/RejectRequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendContactRequest", "Lcom/cryptika/messenger/data/remote/api/ContactRequestResponse;", "Lcom/cryptika/messenger/data/remote/api/ContactRequestBody;", "(Ljava/lang/String;Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/ContactRequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Cryptika_debug"})
public abstract interface AuthApi {
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.RegisterRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.RegisterResponse> $completion);
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.LoginResponse> $completion);
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendContactRequest(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String auth, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.ContactRequestBody request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.ContactRequestResponse> $completion);
    
    @retrofit2.http.GET()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPendingRequests(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String auth, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.PendingRequestsResponse> $completion);
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object acceptContactRequest(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String auth, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.AcceptRequestBody request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.AcceptRequestResponse> $completion);
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object rejectContactRequest(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String auth, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.RejectRequestBody request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.RejectRequestResponse> $completion);
    
    @retrofit2.http.GET()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAcceptedSessions(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String auth, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.AcceptedSessionsResponse> $completion);
}