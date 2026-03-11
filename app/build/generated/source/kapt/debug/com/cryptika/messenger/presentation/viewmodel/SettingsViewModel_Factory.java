package com.cryptika.messenger.presentation.viewmodel;

import android.content.Context;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<EphemeralSessionManager> ephemeralSessionManagerProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<Context> appContextProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  public SettingsViewModel_Factory(Provider<IdentityRepository> identityRepositoryProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<Context> appContextProvider,
      Provider<ServerConfig> serverConfigProvider) {
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.ephemeralSessionManagerProvider = ephemeralSessionManagerProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.appContextProvider = appContextProvider;
    this.serverConfigProvider = serverConfigProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(identityRepositoryProvider.get(), ephemeralSessionManagerProvider.get(), authRepositoryProvider.get(), appContextProvider.get(), serverConfigProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<Context> appContextProvider,
      Provider<ServerConfig> serverConfigProvider) {
    return new SettingsViewModel_Factory(identityRepositoryProvider, ephemeralSessionManagerProvider, authRepositoryProvider, appContextProvider, serverConfigProvider);
  }

  public static SettingsViewModel newInstance(IdentityRepository identityRepository,
      EphemeralSessionManager ephemeralSessionManager, AuthRepository authRepository,
      Context appContext, ServerConfig serverConfig) {
    return new SettingsViewModel(identityRepository, ephemeralSessionManager, authRepository, appContext, serverConfig);
  }
}
