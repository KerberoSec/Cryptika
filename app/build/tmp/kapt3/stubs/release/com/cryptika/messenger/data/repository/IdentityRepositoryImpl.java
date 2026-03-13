package com.cryptika.messenger.data.repository;

import com.cryptika.messenger.data.local.db.*;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.domain.crypto.IdentityHash;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.*;
import kotlinx.coroutines.flow.Flow;
import java.security.MessageDigest;
import java.util.UUID;
import javax.inject.Inject;
import android.util.Base64;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\u000bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0010\u0010\f\u001a\u0004\u0018\u00010\u000bH\u0096@\u00a2\u0006\u0002\u0010\tJ\f\u0010\r\u001a\u00020\u000e*\u00020\u000fH\u0002J\f\u0010\u0010\u001a\u00020\u000f*\u00020\u000eH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/cryptika/messenger/data/repository/IdentityRepositoryImpl;", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "dao", "Lcom/cryptika/messenger/data/local/db/LocalIdentityDao;", "identityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "(Lcom/cryptika/messenger/data/local/db/LocalIdentityDao;Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;)V", "deleteIdentity", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateIdentity", "Lcom/cryptika/messenger/domain/model/LocalIdentity;", "getLocalIdentity", "hexToByteArray", "", "", "toHexString", "Cryptika_release"})
public final class IdentityRepositoryImpl implements com.cryptika.messenger.domain.repository.IdentityRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.db.LocalIdentityDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager = null;
    
    @javax.inject.Inject()
    public IdentityRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.LocalIdentityDao dao, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getLocalIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.LocalIdentity> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object generateIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.LocalIdentity> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String toHexString(byte[] $this$toHexString) {
        return null;
    }
    
    private final byte[] hexToByteArray(java.lang.String $this$hexToByteArray) {
        return null;
    }
}