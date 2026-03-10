package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.AppDatabase;
import com.cryptika.messenger.data.local.db.MessageDao;
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
public final class AppModule_ProvideMessageDaoFactory implements Factory<MessageDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideMessageDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MessageDao get() {
    return provideMessageDao(dbProvider.get());
  }

  public static AppModule_ProvideMessageDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideMessageDaoFactory(dbProvider);
  }

  public static MessageDao provideMessageDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMessageDao(db));
  }
}
