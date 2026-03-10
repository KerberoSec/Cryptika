package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.AppDatabase;
import com.cryptika.messenger.data.local.db.ContactDao;
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
public final class AppModule_ProvideContactDaoFactory implements Factory<ContactDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideContactDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ContactDao get() {
    return provideContactDao(dbProvider.get());
  }

  public static AppModule_ProvideContactDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideContactDaoFactory(dbProvider);
  }

  public static ContactDao provideContactDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideContactDao(db));
  }
}
