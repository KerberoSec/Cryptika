package com.cryptika.messenger.domain.repository;

import com.cryptika.messenger.domain.model.*;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u0006H\u00a6@\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0006H\u00a6@\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\b"}, d2 = {"Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "", "deleteIdentity", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateIdentity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "getLocalIdentity", "Cryptika_debug"})
public abstract interface IdentityRepository {
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLocalIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.LocalIdentity> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object generateIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.LocalIdentity> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}