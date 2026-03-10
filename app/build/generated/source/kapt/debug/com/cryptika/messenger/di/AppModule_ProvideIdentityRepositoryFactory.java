package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.LocalIdentityDao;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.repository.IdentityRepository;
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
public final class AppModule_ProvideIdentityRepositoryFactory implements Factory<IdentityRepository> {
  private final Provider<LocalIdentityDao> daoProvider;

  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  public AppModule_ProvideIdentityRepositoryFactory(Provider<LocalIdentityDao> daoProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider) {
    this.daoProvider = daoProvider;
    this.identityKeyManagerProvider = identityKeyManagerProvider;
  }

  @Override
  public IdentityRepository get() {
    return provideIdentityRepository(daoProvider.get(), identityKeyManagerProvider.get());
  }

  public static AppModule_ProvideIdentityRepositoryFactory create(
      Provider<LocalIdentityDao> daoProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider) {
    return new AppModule_ProvideIdentityRepositoryFactory(daoProvider, identityKeyManagerProvider);
  }

  public static IdentityRepository provideIdentityRepository(LocalIdentityDao dao,
      IdentityKeyManager identityKeyManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideIdentityRepository(dao, identityKeyManager));
  }
}
