package com.cryptika.messenger.data.remote;

import android.content.Context;
import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.domain.crypto.HandshakeManager;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class EphemeralSessionManager_Factory implements Factory<EphemeralSessionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<AuthStore> authStoreProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<HandshakeManager> handshakeManagerProvider;

  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public EphemeralSessionManager_Factory(Provider<Context> contextProvider,
      Provider<AuthStore> authStoreProvider, Provider<OkHttpClient> okHttpClientProvider,
      Provider<ServerConfig> serverConfigProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<HandshakeManager> handshakeManagerProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.authStoreProvider = authStoreProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.serverConfigProvider = serverConfigProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.handshakeManagerProvider = handshakeManagerProvider;
    this.identityKeyManagerProvider = identityKeyManagerProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public EphemeralSessionManager get() {
    return newInstance(contextProvider.get(), authStoreProvider.get(), okHttpClientProvider.get(), serverConfigProvider.get(), contactRepositoryProvider.get(), identityRepositoryProvider.get(), messageRepositoryProvider.get(), handshakeManagerProvider.get(), identityKeyManagerProvider.get(), authRepositoryProvider.get());
  }

  public static EphemeralSessionManager_Factory create(Provider<Context> contextProvider,
      Provider<AuthStore> authStoreProvider, Provider<OkHttpClient> okHttpClientProvider,
      Provider<ServerConfig> serverConfigProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<HandshakeManager> handshakeManagerProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new EphemeralSessionManager_Factory(contextProvider, authStoreProvider, okHttpClientProvider, serverConfigProvider, contactRepositoryProvider, identityRepositoryProvider, messageRepositoryProvider, handshakeManagerProvider, identityKeyManagerProvider, authRepositoryProvider);
  }

  public static EphemeralSessionManager newInstance(Context context, AuthStore authStore,
      OkHttpClient okHttpClient, ServerConfig serverConfig, ContactRepository contactRepository,
      IdentityRepository identityRepository, MessageRepository messageRepository,
      HandshakeManager handshakeManager, IdentityKeyManager identityKeyManager,
      AuthRepository authRepository) {
    return new EphemeralSessionManager(context, authStore, okHttpClient, serverConfig, contactRepository, identityRepository, messageRepository, handshakeManager, identityKeyManager, authRepository);
  }
}
