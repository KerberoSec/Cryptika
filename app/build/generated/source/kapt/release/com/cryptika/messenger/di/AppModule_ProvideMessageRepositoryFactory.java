package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.MessageDao;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideMessageRepositoryFactory implements Factory<MessageRepository> {
  private final Provider<MessageDao> daoProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  public AppModule_ProvideMessageRepositoryFactory(Provider<MessageDao> daoProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.daoProvider = daoProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public MessageRepository get() {
    return provideMessageRepository(daoProvider.get(), keystoreManagerProvider.get());
  }

  public static AppModule_ProvideMessageRepositoryFactory create(Provider<MessageDao> daoProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new AppModule_ProvideMessageRepositoryFactory(daoProvider, keystoreManagerProvider);
  }

  public static MessageRepository provideMessageRepository(MessageDao dao,
      KeystoreManager keystoreManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMessageRepository(dao, keystoreManager));
  }
}
