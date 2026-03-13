package com.cryptika.messenger.di;

import com.cryptika.messenger.data.remote.api.RelayApi;
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
public final class AppModule_ProvideRelayApiFactory implements Factory<RelayApi> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public AppModule_ProvideRelayApiFactory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public RelayApi get() {
    return provideRelayApi(okHttpClientProvider.get());
  }

  public static AppModule_ProvideRelayApiFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new AppModule_ProvideRelayApiFactory(okHttpClientProvider);
  }

  public static RelayApi provideRelayApi(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRelayApi(okHttpClient));
  }
}
