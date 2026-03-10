package com.cryptika.messenger.data.repository;

import com.cryptika.messenger.data.local.db.MessageDao;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class MessageRepositoryImpl_Factory implements Factory<MessageRepositoryImpl> {
  private final Provider<MessageDao> daoProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  public MessageRepositoryImpl_Factory(Provider<MessageDao> daoProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.daoProvider = daoProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public MessageRepositoryImpl get() {
    return newInstance(daoProvider.get(), keystoreManagerProvider.get());
  }

  public static MessageRepositoryImpl_Factory create(Provider<MessageDao> daoProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new MessageRepositoryImpl_Factory(daoProvider, keystoreManagerProvider);
  }

  public static MessageRepositoryImpl newInstance(MessageDao dao, KeystoreManager keystoreManager) {
    return new MessageRepositoryImpl(dao, keystoreManager);
  }
}
