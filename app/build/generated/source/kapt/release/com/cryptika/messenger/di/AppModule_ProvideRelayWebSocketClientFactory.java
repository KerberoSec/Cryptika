package com.cryptika.messenger.di;

import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.websocket.RelayWebSocketClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class AppModule_ProvideRelayWebSocketClientFactory implements Factory<RelayWebSocketClient> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<ServerConfig> serverConfigProvider;

  public AppModule_ProvideRelayWebSocketClientFactory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<ServerConfig> serverConfigProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.serverConfigProvider = serverConfigProvider;
  }

  @Override
  public RelayWebSocketClient get() {
    return provideRelayWebSocketClient(okHttpClientProvider.get(), serverConfigProvider.get());
  }

  public static AppModule_ProvideRelayWebSocketClientFactory create(
      Provider<OkHttpClient> okHttpClientProvider, Provider<ServerConfig> serverConfigProvider) {
    return new AppModule_ProvideRelayWebSocketClientFactory(okHttpClientProvider, serverConfigProvider);
  }

  public static RelayWebSocketClient provideRelayWebSocketClient(OkHttpClient okHttpClient,
      ServerConfig serverConfig) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRelayWebSocketClient(okHttpClient, serverConfig));
  }
}
