package com.cryptika.messenger.presentation.viewmodel;

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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<IdentityRepository> identityRepositoryProvider;

  public SplashViewModel_Factory(Provider<IdentityRepository> identityRepositoryProvider) {
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(identityRepositoryProvider.get());
  }

  public static SplashViewModel_Factory create(
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new SplashViewModel_Factory(identityRepositoryProvider);
  }

  public static SplashViewModel newInstance(IdentityRepository identityRepository) {
    return new SplashViewModel(identityRepository);
  }
}
