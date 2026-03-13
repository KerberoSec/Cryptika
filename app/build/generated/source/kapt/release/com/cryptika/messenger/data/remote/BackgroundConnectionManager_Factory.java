package com.cryptika.messenger.data.remote;

import android.content.Context;
import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.local.db.ConversationDao;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.domain.crypto.HandshakeManager;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.crypto.TicketManager;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.Lazy;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
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
public final class BackgroundConnectionManager_Factory implements Factory<BackgroundConnectionManager> {
  private final Provider<Context> contextProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<HandshakeManager> handshakeManagerProvider;

  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  private final Provider<TicketManager> ticketManagerProvider;

  private final Provider<RelayApi> relayApiProvider;

  private final Provider<AuthStore> authStoreProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<CallManager> callManagerProvider;

  public BackgroundConnectionManager_Factory(Provider<Context> contextProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConversationDao> conversationDaoProvider,
      Provider<HandshakeManager> handshakeManagerProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<TicketManager> ticketManagerProvider, Provider<RelayApi> relayApiProvider,
      Provider<AuthStore> authStoreProvider, Provider<ServerConfig> serverConfigProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<CallManager> callManagerProvider) {
    this.contextProvider = contextProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.handshakeManagerProvider = handshakeManagerProvider;
    this.identityKeyManagerProvider = identityKeyManagerProvider;
    this.ticketManagerProvider = ticketManagerProvider;
    this.relayApiProvider = relayApiProvider;
    this.authStoreProvider = authStoreProvider;
    this.serverConfigProvider = serverConfigProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.callManagerProvider = callManagerProvider;
  }

  @Override
  public BackgroundConnectionManager get() {
    return newInstance(contextProvider.get(), contactRepositoryProvider.get(), identityRepositoryProvider.get(), messageRepositoryProvider.get(), conversationDaoProvider.get(), handshakeManagerProvider.get(), identityKeyManagerProvider.get(), ticketManagerProvider.get(), relayApiProvider.get(), authStoreProvider.get(), serverConfigProvider.get(), okHttpClientProvider.get(), DoubleCheck.lazy(callManagerProvider));
  }

  public static BackgroundConnectionManager_Factory create(Provider<Context> contextProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConversationDao> conversationDaoProvider,
      Provider<HandshakeManager> handshakeManagerProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<TicketManager> ticketManagerProvider, Provider<RelayApi> relayApiProvider,
      Provider<AuthStore> authStoreProvider, Provider<ServerConfig> serverConfigProvider,
      Provider<OkHttpClient> okHttpClientProvider, Provider<CallManager> callManagerProvider) {
    return new BackgroundConnectionManager_Factory(contextProvider, contactRepositoryProvider, identityRepositoryProvider, messageRepositoryProvider, conversationDaoProvider, handshakeManagerProvider, identityKeyManagerProvider, ticketManagerProvider, relayApiProvider, authStoreProvider, serverConfigProvider, okHttpClientProvider, callManagerProvider);
  }

  public static BackgroundConnectionManager newInstance(Context context,
      ContactRepository contactRepository, IdentityRepository identityRepository,
      MessageRepository messageRepository, ConversationDao conversationDao,
      HandshakeManager handshakeManager, IdentityKeyManager identityKeyManager,
      TicketManager ticketManager, RelayApi relayApi, AuthStore authStore,
      ServerConfig serverConfig, OkHttpClient okHttpClient, Lazy<CallManager> callManager) {
    return new BackgroundConnectionManager(context, contactRepository, identityRepository, messageRepository, conversationDao, handshakeManager, identityKeyManager, ticketManager, relayApi, authStore, serverConfig, okHttpClient, callManager);
  }
}
