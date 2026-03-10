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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0018\u0010\n\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0018\u0010\f\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\r\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00100\u000fH\u0016J\u0016\u0010\u0011\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u000bH\u0096@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0012\u001a\u00020\u000bH\u0096@\u00a2\u0006\u0002\u0010\u0013J\f\u0010\u0015\u001a\u00020\u0016*\u00020\bH\u0002J\f\u0010\u0017\u001a\u00020\u000b*\u00020\u0018H\u0002J\f\u0010\u0019\u001a\u00020\u0018*\u00020\u000bH\u0002J\f\u0010\u001a\u001a\u00020\b*\u00020\u0016H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/cryptika/messenger/data/repository/ContactRepositoryImpl;", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "dao", "Lcom/cryptika/messenger/data/local/db/ContactDao;", "(Lcom/cryptika/messenger/data/local/db/ContactDao;)V", "deleteContact", "", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContact", "Lcom/cryptika/messenger/domain/model/Contact;", "getContactByIdentityHash", "hex", "getContacts", "Lkotlinx/coroutines/flow/Flow;", "", "saveContact", "contact", "(Lcom/cryptika/messenger/domain/model/Contact;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateContact", "hexToByteArray", "", "toDomain", "Lcom/cryptika/messenger/data/local/db/ContactEntity;", "toEntity", "toHexString", "Cryptika_debug"})
public final class ContactRepositoryImpl implements com.cryptika.messenger.domain.repository.ContactRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.cryptika.messenger.data.local.db.ContactDao dao = null;
    
    @javax.inject.Inject()
    public ContactRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.ContactDao dao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.util.List<com.cryptika.messenger.domain.model.Contact>> getContacts() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getContact(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.Contact> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getContactByIdentityHash(@org.jetbrains.annotations.NotNull()
    java.lang.String hex, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.domain.model.Contact> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object saveContact(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object updateContact(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.model.Contact contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteContact(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final com.cryptika.messenger.domain.model.Contact toDomain(com.cryptika.messenger.data.local.db.ContactEntity $this$toDomain) {
        return null;
    }
    
    private final com.cryptika.messenger.data.local.db.ContactEntity toEntity(com.cryptika.messenger.domain.model.Contact $this$toEntity) {
        return null;
    }
    
    private final java.lang.String toHexString(byte[] $this$toHexString) {
        return null;
    }
    
    private final byte[] hexToByteArray(java.lang.String $this$hexToByteArray) {
        return null;
    }
}