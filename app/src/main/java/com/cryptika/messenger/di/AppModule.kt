// di/AppModule.kt
package com.cryptika.messenger.di

import android.content.Context
import com.cryptika.messenger.BuildConfig
import com.cryptika.messenger.data.local.AuthStore
import com.cryptika.messenger.data.local.db.*
import com.cryptika.messenger.data.local.keystore.KeystoreManager
import com.cryptika.messenger.data.remote.ServerConfig
import com.cryptika.messenger.data.remote.api.AuthApi
import com.cryptika.messenger.data.remote.api.RelayApi
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient
import com.cryptika.messenger.data.repository.*
import com.cryptika.messenger.domain.crypto.*
import com.cryptika.messenger.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Crypto

    @Provides @Singleton
    fun provideIdentityKeyManager(@ApplicationContext context: Context): IdentityKeyManager =
        IdentityKeyManager(context)

    @Provides @Singleton
    fun provideSessionKeyManager(): SessionKeyManager = SessionKeyManager()

    @Provides @Singleton
    fun provideTicketManager(): TicketManager {
        val serverPubKeyHex = BuildConfig.SERVER_PUBLIC_KEY_HEX
        val serverPubKey = serverPubKeyHex.hexToByteArray()
        return TicketManager(serverPubKey)
    }

    @Provides @Singleton
    fun provideKeystoreManager(): KeystoreManager = KeystoreManager()

    // Database

    @Provides @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keystoreManager: KeystoreManager
    ): AppDatabase {
        val prefs = context.getSharedPreferences("cryptika_db_prefs", Context.MODE_PRIVATE)
        val passphrase = keystoreManager.retrieveDbPassphrase(prefs)
            ?: keystoreManager.generateAndStoreDbPassphrase(prefs)
        return AppDatabase.getInstance(context, passphrase).also {
            passphrase.fill(0)  // Zeroize after use
        }
    }

    @Provides fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()
    @Provides fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()
    @Provides fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()
    @Provides fun provideLocalIdentityDao(db: AppDatabase): LocalIdentityDao = db.localIdentityDao()

    // Networking

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // No logging interceptor: app must never write plaintext request/response data to logs.
        // Release: MODERN_TLS only — enforces TLS 1.2+ with approved cipher suites at the
        //   OkHttp layer as a second line of defence after the network security config.
        // Debug: CLEARTEXT added so ws:// / http:// can reach the plain-HTTP dev server.
        val specs = if (BuildConfig.DEBUG) {
            listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT)
        } else {
            listOf(ConnectionSpec.MODERN_TLS)
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionSpecs(specs)
            .build()
    }

    @Provides @Singleton
    fun provideRelayApi(okHttpClient: OkHttpClient): RelayApi =
        Retrofit.Builder()
            // Placeholder base URL: actual URL is provided per-call via @Url parameter
            // so the server address can be changed at runtime without rebuilding.
            .baseUrl("http://localhost/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RelayApi::class.java)

    @Provides @Singleton
    fun provideAuthApi(okHttpClient: OkHttpClient): AuthApi =
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)

    @Provides @Singleton
    fun provideRelayWebSocketClient(
        okHttpClient: OkHttpClient,
        serverConfig: ServerConfig
    ): RelayWebSocketClient = RelayWebSocketClient(okHttpClient, serverConfig)

    // Repositories

    @Provides @Singleton
    fun provideIdentityRepository(
        dao: LocalIdentityDao,
        identityKeyManager: IdentityKeyManager
    ): IdentityRepository = IdentityRepositoryImpl(dao, identityKeyManager)

    @Provides @Singleton
    fun provideContactRepository(dao: ContactDao): ContactRepository =
        ContactRepositoryImpl(dao)

    @Provides @Singleton
    fun provideMessageRepository(
        dao: MessageDao,
        keystoreManager: KeystoreManager
    ): MessageRepository = MessageRepositoryImpl(dao, keystoreManager)

    @Provides @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        authStore: AuthStore,
        serverConfig: ServerConfig,
        identityRepository: IdentityRepository
    ): AuthRepository = AuthRepositoryImpl(authApi, authStore, serverConfig, identityRepository)
}

private fun String.hexToByteArray(): ByteArray {
    val s = this
    return ByteArray(s.length / 2) { i ->
        ((s[i * 2].digitToInt(16) shl 4) or s[i * 2 + 1].digitToInt(16)).toByte()
    }
}
