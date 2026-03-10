package com.cryptika.messenger.di;

import com.cryptika.messenger.domain.crypto.SessionKeyManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideSessionKeyManagerFactory implements Factory<SessionKeyManager> {
  @Override
  public SessionKeyManager get() {
    return provideSessionKeyManager();
  }

  public static AppModule_ProvideSessionKeyManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SessionKeyManager provideSessionKeyManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSessionKeyManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideSessionKeyManagerFactory INSTANCE = new AppModule_ProvideSessionKeyManagerFactory();
  }
}
