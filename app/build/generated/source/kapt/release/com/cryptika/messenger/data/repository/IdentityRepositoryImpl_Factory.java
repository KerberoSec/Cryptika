package com.cryptika.messenger.data.repository;

import com.cryptika.messenger.data.local.db.LocalIdentityDao;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
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
public final class IdentityRepositoryImpl_Factory implements Factory<IdentityRepositoryImpl> {
  private final Provider<LocalIdentityDao> daoProvider;

  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  public IdentityRepositoryImpl_Factory(Provider<LocalIdentityDao> daoProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider) {
    this.daoProvider = daoProvider;
    this.identityKeyManagerProvider = identityKeyManagerProvider;
  }

  @Override
  public IdentityRepositoryImpl get() {
    return newInstance(daoProvider.get(), identityKeyManagerProvider.get());
  }

  public static IdentityRepositoryImpl_Factory create(Provider<LocalIdentityDao> daoProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider) {
    return new IdentityRepositoryImpl_Factory(daoProvider, identityKeyManagerProvider);
  }

  public static IdentityRepositoryImpl newInstance(LocalIdentityDao dao,
      IdentityKeyManager identityKeyManager) {
    return new IdentityRepositoryImpl(dao, identityKeyManager);
  }
}
