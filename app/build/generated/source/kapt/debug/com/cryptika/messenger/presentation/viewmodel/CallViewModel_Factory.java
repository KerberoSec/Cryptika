package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.CallManager;
import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
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
public final class CallViewModel_Factory implements Factory<CallViewModel> {
  private final Provider<CallManager> callManagerProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

  public CallViewModel_Factory(Provider<CallManager> callManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider) {
    this.callManagerProvider = callManagerProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.backgroundConnectionManagerProvider = backgroundConnectionManagerProvider;
  }

  @Override
  public CallViewModel get() {
    return newInstance(callManagerProvider.get(), contactRepositoryProvider.get(), identityRepositoryProvider.get(), backgroundConnectionManagerProvider.get());
  }

  public static CallViewModel_Factory create(Provider<CallManager> callManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider) {
    return new CallViewModel_Factory(callManagerProvider, contactRepositoryProvider, identityRepositoryProvider, backgroundConnectionManagerProvider);
  }

  public static CallViewModel newInstance(CallManager callManager,
      ContactRepository contactRepository, IdentityRepository identityRepository,
      BackgroundConnectionManager backgroundConnectionManager) {
    return new CallViewModel(callManager, contactRepository, identityRepository, backgroundConnectionManager);
  }
}
