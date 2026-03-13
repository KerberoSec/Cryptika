package com.cryptika.messenger.di;

import android.content.Context;
import com.cryptika.messenger.data.local.db.AppDatabase;
import com.cryptika.messenger.data.local.keystore.KeystoreManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideDatabaseFactory implements Factory<AppDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<KeystoreManager> keystoreManagerProvider;

  public AppModule_ProvideDatabaseFactory(Provider<Context> contextProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    this.contextProvider = contextProvider;
    this.keystoreManagerProvider = keystoreManagerProvider;
  }

  @Override
  public AppDatabase get() {
    return provideDatabase(contextProvider.get(), keystoreManagerProvider.get());
  }

  public static AppModule_ProvideDatabaseFactory create(Provider<Context> contextProvider,
      Provider<KeystoreManager> keystoreManagerProvider) {
    return new AppModule_ProvideDatabaseFactory(contextProvider, keystoreManagerProvider);
  }

  public static AppDatabase provideDatabase(Context context, KeystoreManager keystoreManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDatabase(context, keystoreManager));
  }
}
