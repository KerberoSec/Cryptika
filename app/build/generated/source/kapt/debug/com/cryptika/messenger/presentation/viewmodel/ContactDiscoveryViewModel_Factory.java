package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.ContactRepository;
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

  private final Provider<ContactRepository> contactRepositoryProvider;

  public ContactDiscoveryViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.ephemeralSessionManagerProvider = ephemeralSessionManagerProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
  }

  @Override
  public ContactDiscoveryViewModel get() {
    return newInstance(authRepositoryProvider.get(), ephemeralSessionManagerProvider.get(), contactRepositoryProvider.get());
  }

  public static ContactDiscoveryViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider) {
    return new ContactDiscoveryViewModel_Factory(authRepositoryProvider, ephemeralSessionManagerProvider, contactRepositoryProvider);
  }

  public static ContactDiscoveryViewModel newInstance(AuthRepository authRepository,
      EphemeralSessionManager ephemeralSessionManager, ContactRepository contactRepository) {
    return new ContactDiscoveryViewModel(authRepository, ephemeralSessionManager, contactRepository);
  }
}
