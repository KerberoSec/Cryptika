package com.cryptika.messenger.di;

import android.content.Context;
import com.cryptika.messenger.BuildConfig;
import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.local.db.*;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.AuthApi;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient;
import com.cryptika.messenger.data.repository.*;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.repository.*;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008e\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J(\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0007J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0007J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0011H\u0007J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0012\u001a\u00020\u0013H\u0007J\u001a\u0010\u0019\u001a\u00020\u00132\b\b\u0001\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dH\u0007J\u0012\u0010\u001e\u001a\u00020\u001f2\b\b\u0001\u0010\u001a\u001a\u00020\u001bH\u0007J\u0018\u0010 \u001a\u00020\u000f2\u0006\u0010\u0016\u001a\u00020!2\u0006\u0010\"\u001a\u00020\u001fH\u0007J\b\u0010#\u001a\u00020\u001dH\u0007J\u0010\u0010$\u001a\u00020!2\u0006\u0010\u0012\u001a\u00020\u0013H\u0007J\u0010\u0010%\u001a\u00020&2\u0006\u0010\u0012\u001a\u00020\u0013H\u0007J\u0018\u0010\'\u001a\u00020(2\u0006\u0010\u0016\u001a\u00020&2\u0006\u0010\u001c\u001a\u00020\u001dH\u0007J\b\u0010)\u001a\u00020\u0006H\u0007J\u0010\u0010*\u001a\u00020+2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0018\u0010,\u001a\u00020-2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0007J\b\u0010.\u001a\u00020/H\u0007J\b\u00100\u001a\u000201H\u0007\u00a8\u00062"}, d2 = {"Lcom/cryptika/messenger/di/AppModule;", "", "()V", "provideAuthApi", "Lcom/cryptika/messenger/data/remote/api/AuthApi;", "okHttpClient", "Lokhttp3/OkHttpClient;", "provideAuthRepository", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "authApi", "authStore", "Lcom/cryptika/messenger/data/local/AuthStore;", "serverConfig", "Lcom/cryptika/messenger/data/remote/ServerConfig;", "identityRepository", "Lcom/cryptika/messenger/domain/repository/IdentityRepository;", "provideContactDao", "Lcom/cryptika/messenger/data/local/db/ContactDao;", "db", "Lcom/cryptika/messenger/data/local/db/AppDatabase;", "provideContactRepository", "Lcom/cryptika/messenger/domain/repository/ContactRepository;", "dao", "provideConversationDao", "Lcom/cryptika/messenger/data/local/db/ConversationDao;", "provideDatabase", "context", "Landroid/content/Context;", "keystoreManager", "Lcom/cryptika/messenger/data/local/keystore/KeystoreManager;", "provideIdentityKeyManager", "Lcom/cryptika/messenger/domain/crypto/IdentityKeyManager;", "provideIdentityRepository", "Lcom/cryptika/messenger/data/local/db/LocalIdentityDao;", "identityKeyManager", "provideKeystoreManager", "provideLocalIdentityDao", "provideMessageDao", "Lcom/cryptika/messenger/data/local/db/MessageDao;", "provideMessageRepository", "Lcom/cryptika/messenger/domain/repository/MessageRepository;", "provideOkHttpClient", "provideRelayApi", "Lcom/cryptika/messenger/data/remote/api/RelayApi;", "provideRelayWebSocketClient", "Lcom/cryptika/messenger/data/remote/websocket/RelayWebSocketClient;", "provideSessionKeyManager", "Lcom/cryptika/messenger/domain/crypto/SessionKeyManager;", "provideTicketManager", "Lcom/cryptika/messenger/domain/crypto/TicketManager;", "Cryptika_release"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.crypto.IdentityKeyManager provideIdentityKeyManager(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.crypto.SessionKeyManager provideSessionKeyManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.crypto.TicketManager provideTicketManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.keystore.KeystoreManager provideKeystoreManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.AppDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.keystore.KeystoreManager keystoreManager) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.ContactDao provideContactDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.MessageDao provideMessageDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.ConversationDao provideConversationDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.local.db.LocalIdentityDao provideLocalIdentityDao(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.AppDatabase db) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final okhttp3.OkHttpClient provideOkHttpClient() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.api.RelayApi provideRelayApi(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.api.AuthApi provideAuthApi(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient provideRelayWebSocketClient(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.repository.IdentityRepository provideIdentityRepository(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.LocalIdentityDao dao, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.crypto.IdentityKeyManager identityKeyManager) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.repository.ContactRepository provideContactRepository(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.ContactDao dao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.repository.MessageRepository provideMessageRepository(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.db.MessageDao dao, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.keystore.KeystoreManager keystoreManager) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.repository.AuthRepository provideAuthRepository(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.api.AuthApi authApi, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.local.AuthStore authStore, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.ServerConfig serverConfig, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.IdentityRepository identityRepository) {
        return null;
    }
}