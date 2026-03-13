package com.cryptika.messenger.data.repository;

import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.AuthApi;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<AuthStore> authStoreProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  public AuthRepositoryImpl_Factory(Provider<AuthApi> authApiProvider,
      Provider<AuthStore> authStoreProvider, Provider<ServerConfig> serverConfigProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    this.authApiProvider = authApiProvider;
    this.authStoreProvider = authStoreProvider;
    this.serverConfigProvider = serverConfigProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(authApiProvider.get(), authStoreProvider.get(), serverConfigProvider.get(), identityRepositoryProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<AuthApi> authApiProvider,
      Provider<AuthStore> authStoreProvider, Provider<ServerConfig> serverConfigProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new AuthRepositoryImpl_Factory(authApiProvider, authStoreProvider, serverConfigProvider, identityRepositoryProvider);
  }

  public static AuthRepositoryImpl newInstance(AuthApi authApi, AuthStore authStore,
      ServerConfig serverConfig, IdentityRepository identityRepository) {
    return new AuthRepositoryImpl(authApi, authStore, serverConfig, identityRepository);
  }
}
