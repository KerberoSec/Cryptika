package com.cryptika.messenger;

import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<EphemeralSessionManager> ephemeralSessionManagerProvider;

  private final Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public MainActivity_MembersInjector(
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.ephemeralSessionManagerProvider = ephemeralSessionManagerProvider;
    this.backgroundConnectionManagerProvider = backgroundConnectionManagerProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<EphemeralSessionManager> ephemeralSessionManagerProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new MainActivity_MembersInjector(ephemeralSessionManagerProvider, backgroundConnectionManagerProvider, authRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectEphemeralSessionManager(instance, ephemeralSessionManagerProvider.get());
    injectBackgroundConnectionManager(instance, backgroundConnectionManagerProvider.get());
    injectAuthRepository(instance, authRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.ephemeralSessionManager")
  public static void injectEphemeralSessionManager(MainActivity instance,
      EphemeralSessionManager ephemeralSessionManager) {
    instance.ephemeralSessionManager = ephemeralSessionManager;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.backgroundConnectionManager")
  public static void injectBackgroundConnectionManager(MainActivity instance,
      BackgroundConnectionManager backgroundConnectionManager) {
    instance.backgroundConnectionManager = backgroundConnectionManager;
  }

  @InjectedFieldSignature("com.cryptika.messenger.MainActivity.authRepository")
  public static void injectAuthRepository(MainActivity instance, AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }
}
