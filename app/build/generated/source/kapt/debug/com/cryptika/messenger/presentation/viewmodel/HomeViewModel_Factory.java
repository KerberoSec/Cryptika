package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.domain.repository.ContactRepository;
import com.cryptika.messenger.domain.repository.IdentityRepository;
import com.cryptika.messenger.domain.repository.MessageRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  public HomeViewModel_Factory(Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(contactRepositoryProvider.get(), identityRepositoryProvider.get(), messageRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    return new HomeViewModel_Factory(contactRepositoryProvider, identityRepositoryProvider, messageRepositoryProvider);
  }

  public static HomeViewModel newInstance(ContactRepository contactRepository,
      IdentityRepository identityRepository, MessageRepository messageRepository) {
    return new HomeViewModel(contactRepository, identityRepository, messageRepository);
  }
}
