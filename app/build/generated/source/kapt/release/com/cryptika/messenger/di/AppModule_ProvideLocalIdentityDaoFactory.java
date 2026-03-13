package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.AppDatabase;
import com.cryptika.messenger.data.local.db.LocalIdentityDao;
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
public final class AppModule_ProvideLocalIdentityDaoFactory implements Factory<LocalIdentityDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideLocalIdentityDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LocalIdentityDao get() {
    return provideLocalIdentityDao(dbProvider.get());
  }

  public static AppModule_ProvideLocalIdentityDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideLocalIdentityDaoFactory(dbProvider);
  }

  public static LocalIdentityDao provideLocalIdentityDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideLocalIdentityDao(db));
  }
}
