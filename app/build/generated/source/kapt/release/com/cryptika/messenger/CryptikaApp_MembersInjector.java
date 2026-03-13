package com.cryptika.messenger;

import androidx.hilt.work.HiltWorkerFactory;
import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
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
public final class CryptikaApp_MembersInjector implements MembersInjector<CryptikaApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

  public CryptikaApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
    this.backgroundConnectionManagerProvider = backgroundConnectionManagerProvider;
  }

  public static MembersInjector<CryptikaApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider) {
    return new CryptikaApp_MembersInjector(workerFactoryProvider, backgroundConnectionManagerProvider);
  }

  @Override
  public void injectMembers(CryptikaApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectBackgroundConnectionManager(instance, backgroundConnectionManagerProvider.get());
  }

  @InjectedFieldSignature("com.cryptika.messenger.CryptikaApp.workerFactory")
  public static void injectWorkerFactory(CryptikaApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.cryptika.messenger.CryptikaApp.backgroundConnectionManager")
  public static void injectBackgroundConnectionManager(CryptikaApp instance,
      BackgroundConnectionManager backgroundConnectionManager) {
    instance.backgroundConnectionManager = backgroundConnectionManager;
  }
}
