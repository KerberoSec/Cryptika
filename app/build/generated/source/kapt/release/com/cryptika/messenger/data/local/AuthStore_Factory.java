package com.cryptika.messenger.data.local;

import android.content.Context;
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
public final class AuthStore_Factory implements Factory<AuthStore> {
  private final Provider<Context> contextProvider;

  public AuthStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AuthStore get() {
    return newInstance(contextProvider.get());
  }

  public static AuthStore_Factory create(Provider<Context> contextProvider) {
    return new AuthStore_Factory(contextProvider);
  }

  public static AuthStore newInstance(Context context) {
    return new AuthStore(context);
  }
}
