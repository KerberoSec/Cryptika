package com.cryptika.messenger.di;

import com.cryptika.messenger.data.local.db.ContactDao;
import com.cryptika.messenger.domain.repository.ContactRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppModule_ProvideContactRepositoryFactory implements Factory<ContactRepository> {
  private final Provider<ContactDao> daoProvider;

  public AppModule_ProvideContactRepositoryFactory(Provider<ContactDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ContactRepository get() {
    return provideContactRepository(daoProvider.get());
  }

  public static AppModule_ProvideContactRepositoryFactory create(Provider<ContactDao> daoProvider) {
    return new AppModule_ProvideContactRepositoryFactory(daoProvider);
  }

  public static ContactRepository provideContactRepository(ContactDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideContactRepository(dao));
  }
}
