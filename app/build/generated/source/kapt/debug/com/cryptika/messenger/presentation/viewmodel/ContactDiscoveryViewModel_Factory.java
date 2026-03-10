package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
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
public final class ContactDiscoveryViewModel_Factory implements Factory<ContactDiscoveryViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<EphemeralSessionManager> ephemeralSessionManagerProvider;

  public ContactDiscoveryViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.ephemeralSessionManagerProvider = ephemeralSessionManagerProvider;
  }

  @Override
  public ContactDiscoveryViewModel get() {
    return newInstance(authRepositoryProvider.get(), ephemeralSessionManagerProvider.get());
  }

  public static ContactDiscoveryViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider) {
    return new ContactDiscoveryViewModel_Factory(authRepositoryProvider, ephemeralSessionManagerProvider);
  }

  public static ContactDiscoveryViewModel newInstance(AuthRepository authRepository,
      EphemeralSessionManager ephemeralSessionManager) {
    return new ContactDiscoveryViewModel(authRepository, ephemeralSessionManager);
  }
}
