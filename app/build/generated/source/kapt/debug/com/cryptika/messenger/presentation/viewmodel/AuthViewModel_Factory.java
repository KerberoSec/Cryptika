package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.domain.repository.AuthRepository;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  public AuthViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(authRepositoryProvider.get(), identityRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new AuthViewModel_Factory(authRepositoryProvider, identityRepositoryProvider);
  }

  public static AuthViewModel newInstance(AuthRepository authRepository,
      IdentityRepository identityRepository) {
    return new AuthViewModel(authRepository, identityRepository);
  }
}
