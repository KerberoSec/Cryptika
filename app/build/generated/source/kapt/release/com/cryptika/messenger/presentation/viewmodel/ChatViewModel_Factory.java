package com.cryptika.messenger.presentation.viewmodel;

import android.content.Context;
import com.cryptika.messenger.data.local.db.ConversationDao;
import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  private final Provider<RelayApi> relayApiProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  private final Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

  private final Provider<EphemeralSessionManager> ephemeralSessionManagerProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<Context> appContextProvider;

  public ChatViewModel_Factory(Provider<MessageRepository> messageRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider, Provider<RelayApi> relayApiProvider,
      Provider<ServerConfig> serverConfigProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<ConversationDao> conversationDaoProvider, Provider<Context> appContextProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.identityKeyManagerProvider = identityKeyManagerProvider;
    this.relayApiProvider = relayApiProvider;
    this.serverConfigProvider = serverConfigProvider;
    this.backgroundConnectionManagerProvider = backgroundConnectionManagerProvider;
    this.ephemeralSessionManagerProvider = ephemeralSessionManagerProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(messageRepositoryProvider.get(), contactRepositoryProvider.get(), identityRepositoryProvider.get(), identityKeyManagerProvider.get(), relayApiProvider.get(), serverConfigProvider.get(), backgroundConnectionManagerProvider.get(), ephemeralSessionManagerProvider.get(), conversationDaoProvider.get(), appContextProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<MessageRepository> messageRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider, Provider<RelayApi> relayApiProvider,
      Provider<ServerConfig> serverConfigProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<ConversationDao> conversationDaoProvider, Provider<Context> appContextProvider) {
    return new ChatViewModel_Factory(messageRepositoryProvider, contactRepositoryProvider, identityRepositoryProvider, identityKeyManagerProvider, relayApiProvider, serverConfigProvider, backgroundConnectionManagerProvider, ephemeralSessionManagerProvider, conversationDaoProvider, appContextProvider);
  }

  public static ChatViewModel newInstance(MessageRepository messageRepository,
      ContactRepository contactRepository, IdentityRepository identityRepository,
      IdentityKeyManager identityKeyManager, RelayApi relayApi, ServerConfig serverConfig,
      BackgroundConnectionManager backgroundConnectionManager,
      EphemeralSessionManager ephemeralSessionManager, ConversationDao conversationDao,
      Context appContext) {
    return new ChatViewModel(messageRepository, contactRepository, identityRepository, identityKeyManager, relayApi, serverConfig, backgroundConnectionManager, ephemeralSessionManager, conversationDao, appContext);
  }
}
