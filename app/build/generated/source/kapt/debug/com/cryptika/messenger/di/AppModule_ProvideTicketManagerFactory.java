package com.cryptika.messenger.di;

import com.cryptika.messenger.domain.crypto.TicketManager;
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
public final class AppModule_ProvideTicketManagerFactory implements Factory<TicketManager> {
  @Override
  public TicketManager get() {
    return provideTicketManager();
  }

  public static AppModule_ProvideTicketManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TicketManager provideTicketManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTicketManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideTicketManagerFactory INSTANCE = new AppModule_ProvideTicketManagerFactory();
  }
}
