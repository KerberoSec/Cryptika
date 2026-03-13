package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.keystore.KeystoreManager;
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
public final class AppModule_ProvideKeystoreManagerFactory implements Factory<KeystoreManager> {
  @Override
  public KeystoreManager get() {
    return provideKeystoreManager();
  }

  public static AppModule_ProvideKeystoreManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static KeystoreManager provideKeystoreManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideKeystoreManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideKeystoreManagerFactory INSTANCE = new AppModule_ProvideKeystoreManagerFactory();
  }
}
