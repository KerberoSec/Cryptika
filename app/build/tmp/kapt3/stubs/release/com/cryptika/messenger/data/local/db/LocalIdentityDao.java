package com.cryptika.messenger.data.local.db;

import android.content.Context;
import androidx.room.*;
import androidx.sqlite.db.SupportSQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\t\u00a8\u0006\n"}, d2 = {"Lcom/cryptika/messenger/data/local/db/LocalIdentityDao;", "", "deleteAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getIdentity", "Lcom/cryptika/messenger/data/local/db/LocalIdentityEntity;", "saveIdentity", "identity", "(Lcom/cryptika/messenger/data/local/db/LocalIdentityEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Cryptika_release"})
@androidx.room.Dao()
public abstract interface LocalIdentityDao {
    
    @androidx.room.Query(value = "SELECT * FROM local_identity WHERE id = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getIdentity(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.cryptika.messenger.data.local.db.LocalIdentityEntity> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object saveIdentity(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.LocalIdentityEntity identity, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM local_identity")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}