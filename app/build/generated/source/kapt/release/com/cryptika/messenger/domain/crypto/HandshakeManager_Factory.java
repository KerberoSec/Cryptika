package com.cryptika.messenger.domain.crypto;

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
public final class HandshakeManager_Factory implements Factory<HandshakeManager> {
  private final Provider<IdentityKeyManager> identityKeyManagerProvider;

  private final Provider<SessionKeyManager> sessionKeyManagerProvider;

  public HandshakeManager_Factory(Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<SessionKeyManager> sessionKeyManagerProvider) {
    this.identityKeyManagerProvider = identityKeyManagerProvider;
    this.sessionKeyManagerProvider = sessionKeyManagerProvider;
  }

  @Override
  public HandshakeManager get() {
    return newInstance(identityKeyManagerProvider.get(), sessionKeyManagerProvider.get());
  }

  public static HandshakeManager_Factory create(
      Provider<IdentityKeyManager> identityKeyManagerProvider,
      Provider<SessionKeyManager> sessionKeyManagerProvider) {
    return new HandshakeManager_Factory(identityKeyManagerProvider, sessionKeyManagerProvider);
  }

  public static HandshakeManager newInstance(IdentityKeyManager identityKeyManager,
      SessionKeyManager sessionKeyManager) {
    return new HandshakeManager(identityKeyManager, sessionKeyManager);
  }
}
