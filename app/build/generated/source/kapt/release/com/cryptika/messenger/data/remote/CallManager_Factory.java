package com.cryptika.messenger.data.remote;

import android.content.Context;
import com.cryptika.messenger.domain.crypto.IdentityKeyManager;
import com.cryptika.messenger.domain.crypto.SessionKeyManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CallManager_Factory implements Factory<CallManager> {
  private final Provider<Context> contextProvider;

  private final Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider;

  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  private final Provider<SessionKeyManager> sessionKeyManagerProvider;

  public CallManager_Factory(Provider<Context> contextProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<SessionKeyManager> sessionKeyManagerProvider) {
    this.contextProvider = contextProvider;
    this.backgroundConnectionManagerProvider = backgroundConnectionManagerProvider;
    this.identityKeyManagerProvider = identityKeyManagerProvider;
    this.sessionKeyManagerProvider = sessionKeyManagerProvider;
  }

  @Override
  public CallManager get() {
    return newInstance(contextProvider.get(), backgroundConnectionManagerProvider.get(), identityKeyManagerProvider.get(), sessionKeyManagerProvider.get());
  }

  public static CallManager_Factory create(Provider<Context> contextProvider,
      Provider<BackgroundConnectionManager> backgroundConnectionManagerProvider,
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<SessionKeyManager> sessionKeyManagerProvider) {
    return new CallManager_Factory(contextProvider, backgroundConnectionManagerProvider, identityKeyManagerProvider, sessionKeyManagerProvider);
  }

  public static CallManager newInstance(Context context,
      BackgroundConnectionManager backgroundConnectionManager,
      IdentityKeyManager identityKeyManager, SessionKeyManager sessionKeyManager) {
    return new CallManager(context, backgroundConnectionManager, identityKeyManager, sessionKeyManager);
  }
}
