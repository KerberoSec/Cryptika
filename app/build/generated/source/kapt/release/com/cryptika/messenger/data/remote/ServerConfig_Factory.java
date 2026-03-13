package com.cryptika.messenger.data.remote;

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
public final class ServerConfig_Factory implements Factory<ServerConfig> {
  private final Provider<Context> contextProvider;

  public ServerConfig_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ServerConfig get() {
    return newInstance(contextProvider.get());
  }

  public static ServerConfig_Factory create(Provider<Context> contextProvider) {
    return new ServerConfig_Factory(contextProvider);
  }

  public static ServerConfig newInstance(Context context) {
    return new ServerConfig(context);
  }
}
