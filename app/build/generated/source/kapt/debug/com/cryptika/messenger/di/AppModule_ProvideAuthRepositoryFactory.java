package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.AuthStore;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.api.AuthApi;
import com.cryptika.messenger.domain.repository.AuthRepository;
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
public final class AppModule_ProvideAuthRepositoryFactory implements Factory<AuthRepository> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<AuthStore> authStoreProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  public AppModule_ProvideAuthRepositoryFactory(Provider<AuthApi> authApiProvider,
      Provider<AuthStore> authStoreProvider, Provider<ServerConfig> serverConfigProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    this.authApiProvider = authApiProvider;
    this.authStoreProvider = authStoreProvider;
    this.serverConfigProvider = serverConfigProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public AuthRepository get() {
    return provideAuthRepository(authApiProvider.get(), authStoreProvider.get(), serverConfigProvider.get(), identityRepositoryProvider.get());
  }

  public static AppModule_ProvideAuthRepositoryFactory create(Provider<AuthApi> authApiProvider,
      Provider<AuthStore> authStoreProvider, Provider<ServerConfig> serverConfigProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new AppModule_ProvideAuthRepositoryFactory(authApiProvider, authStoreProvider, serverConfigProvider, identityRepositoryProvider);
  }

  public static AuthRepository provideAuthRepository(AuthApi authApi, AuthStore authStore,
      ServerConfig serverConfig, IdentityRepository identityRepository) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAuthRepository(authApi, authStore, serverConfig, identityRepository));
  }
}
