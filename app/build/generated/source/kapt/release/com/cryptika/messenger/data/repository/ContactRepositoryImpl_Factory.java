package com.cryptika.messenger.data.repository;

import com.cryptika.messenger.data.local.db.ContactDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ContactRepositoryImpl_Factory implements Factory<ContactRepositoryImpl> {
  private final Provider<ContactDao> daoProvider;

  public ContactRepositoryImpl_Factory(Provider<ContactDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ContactRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ContactRepositoryImpl_Factory create(Provider<ContactDao> daoProvider) {
    return new ContactRepositoryImpl_Factory(daoProvider);
  }

  public static ContactRepositoryImpl newInstance(ContactDao dao) {
    return new ContactRepositoryImpl(dao);
  }
}
