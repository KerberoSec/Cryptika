package com.cryptika.messenger.data.local.db;

import android.content.Context;
import androidx.room.*;
import androidx.sqlite.db.SupportSQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u000b2\u00020\u0001:\u0001\u000bB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&\u00a8\u0006\f"}, d2 = {"Lcom/cryptika/messenger/data/local/db/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "contactDao", "Lcom/cryptika/messenger/data/local/db/ContactDao;", "conversationDao", "Lcom/cryptika/messenger/data/local/db/ConversationDao;", "localIdentityDao", "Lcom/cryptika/messenger/data/local/db/LocalIdentityDao;", "messageDao", "Lcom/cryptika/messenger/data/local/db/MessageDao;", "Companion", "Cryptika_release"})
@androidx.room.Database(entities = {com.cryptika.messenger.data.local.db.ContactEntity.class, com.cryptika.messenger.data.local.db.MessageEntity.class, com.cryptika.messenger.data.local.db.ConversationEntity.class, com.cryptika.messenger.data.local.db.LocalIdentityEntity.class}, version = 3, exportSchema = true)
@androidx.room.TypeConverters(value = {com.cryptika.messenger.data.local.db.Converters.class})
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.cryptika.messenger.data.local.db.AppDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.local.db.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.cryptika.messenger.data.local.db.ContactDao contactDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.cryptika.messenger.data.local.db.MessageDao messageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.cryptika.messenger.data.local.db.ConversationDao conversationDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.cryptika.messenger.data.local.db.LocalIdentityDao localIdentityDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/cryptika/messenger/data/local/db/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/cryptika/messenger/data/local/db/AppDatabase;", "getInstance", "context", "Landroid/content/Context;", "passphrase", "", "Cryptika_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.cryptika.messenger.data.local.db.AppDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context, @org.jetbrains.annotations.NotNull()
        byte[] passphrase) {
            return null;
        }
    }
}