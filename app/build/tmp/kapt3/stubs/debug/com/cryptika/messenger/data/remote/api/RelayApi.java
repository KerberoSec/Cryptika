package com.cryptika.messenger.data.remote.api;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\"\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\"\u0010\t\u001a\u00020\n2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\f\u00a8\u0006\r"}, d2 = {"Lcom/cryptika/messenger/data/remote/api/RelayApi;", "", "registerPresence", "Lcom/cryptika/messenger/data/remote/api/PresenceResponse;", "url", "", "request", "Lcom/cryptika/messenger/data/remote/api/PresenceRequest;", "(Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/PresenceRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "requestTicket", "Lcom/cryptika/messenger/data/remote/api/TicketResponse;", "Lcom/cryptika/messenger/data/remote/api/TicketRequest;", "(Ljava/lang/String;Lcom/cryptika/messenger/data/remote/api/TicketRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Cryptika_debug"})
public abstract interface RelayApi {
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object requestTicket(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.TicketRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.TicketResponse> $completion);
    
    @retrofit2.http.POST()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object registerPresence(@retrofit2.http.Url()
    @org.jetbrains.annotations.NotNull()
    java.lang.String url, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.PresenceRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.remote.api.PresenceResponse> $completion);
}