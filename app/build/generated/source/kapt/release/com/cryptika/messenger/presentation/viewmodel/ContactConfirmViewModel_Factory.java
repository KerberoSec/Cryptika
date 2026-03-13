package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.domain.repository.ContactRepository;
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
public final class ContactConfirmViewModel_Factory implements Factory<ContactConfirmViewModel> {
  private final Provider<ContactRepository> contactRepositoryProvider;

  public ContactConfirmViewModel_Factory(Provider<ContactRepository> contactRepositoryProvider) {
    this.contactRepositoryProvider = contactRepositoryProvider;
  }

  @Override
  public ContactConfirmViewModel get() {
    return newInstance(contactRepositoryProvider.get());
  }

  public static ContactConfirmViewModel_Factory create(
      Provider<ContactRepository> contactRepositoryProvider) {
    return new ContactConfirmViewModel_Factory(contactRepositoryProvider);
  }

  public static ContactConfirmViewModel newInstance(ContactRepository contactRepository) {
    return new ContactConfirmViewModel(contactRepository);
  }
}
