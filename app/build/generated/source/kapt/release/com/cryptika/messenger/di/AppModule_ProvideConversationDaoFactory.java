package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.AppDatabase;
import com.cryptika.messenger.data.local.db.ConversationDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AppModule_ProvideConversationDaoFactory implements Factory<ConversationDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideConversationDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ConversationDao get() {
    return provideConversationDao(dbProvider.get());
  }

  public static AppModule_ProvideConversationDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideConversationDaoFactory(dbProvider);
  }

  public static ConversationDao provideConversationDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideConversationDao(db));
  }
}
