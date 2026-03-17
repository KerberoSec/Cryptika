package com.cryptika.messenger;

import com.cryptika.messenger.data.local.db.ContactDao;
import com.cryptika.messenger.data.local.db.ConversationDao;
import com.cryptika.messenger.data.local.db.MessageDao;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<EphemeralSessionManager> ephemeralSessionManagerProvider;

  private final Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private final Provider<ContactDao> contactDaoProvider;

  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  public MainActivity_MembersInjector(
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<MessageDao> messageDaoProvider,
      Provider<ContactDao> contactDaoProvider, Provider<ConversationDao> conversationDaoProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.ephemeralSessionManagerProvider = ephemeralSessionManagerProvider;
    this.backgroundConnectionManagerProvider = backgroundConnectionManagerProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.messageDaoProvider = messageDaoProvider;
    this.contactDaoProvider = contactDaoProvider;
    this.conversationDaoProvider = conversationDaoProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<MessageDao> messageDaoProvider,
      Provider<ContactDao> contactDaoProvider, Provider<ConversationDao> conversationDaoProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new MainActivity_MembersInjector(ephemeralSessionManagerProvider, backgroundConnectionManagerProvider, authRepositoryProvider, messageDaoProvider, contactDaoProvider, conversationDaoProvider, keystoreManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectEphemeralSessionManager(instance, ephemeralSessionManagerProvider.get());
    injectBackgroundConnectionManager(instance, backgroundConnectionManagerProvider.get());
    injectAuthRepository(instance, authRepositoryProvider.get());
    injectMessageDao(instance, messageDaoProvider.get());
    injectContactDao(instance, contactDaoProvider.get());
    injectConversationDao(instance, conversationDaoProvider.get());
    injectKeystoreManager(instance, keystoreManagerProvider.get());
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.ephemeralSessionManager")
  public static void injectEphemeralSessionManager(MainActivity instance,
      EphemeralSessionManager ephemeralSessionManager) {
    instance.ephemeralSessionManager = ephemeralSessionManager;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.backgroundConnectionManager")
  public static void injectBackgroundConnectionManager(MainActivity instance,
      BackgroundConnectionManager backgroundConnectionManager) {
    instance.backgroundConnectionManager = backgroundConnectionManager;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.authRepository")
  public static void injectAuthRepository(MainActivity instance, AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.messageDao")
  public static void injectMessageDao(MainActivity instance, MessageDao messageDao) {
    instance.messageDao = messageDao;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.contactDao")
  public static void injectContactDao(MainActivity instance, ContactDao contactDao) {
    instance.contactDao = contactDao;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.conversationDao")
  public static void injectConversationDao(MainActivity instance, ConversationDao conversationDao) {
    instance.conversationDao = conversationDao;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.keystoreManager")
  public static void injectKeystoreManager(MainActivity instance, KeystoreManager keystoreManager) {
    instance.keystoreManager = keystoreManager;
  }
}
