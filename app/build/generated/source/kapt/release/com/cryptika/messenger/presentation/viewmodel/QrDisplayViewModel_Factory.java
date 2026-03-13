package com.cryptika.messenger.presentation.viewmodel;

import com.cryptika.messenger.domain.repository.IdentityRepository;
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
public final class QrDisplayViewModel_Factory implements Factory<QrDisplayViewModel> {
  private final Provider<IdentityRepository> identityRepositoryProvider;

  public QrDisplayViewModel_Factory(Provider<IdentityRepository> identityRepositoryProvider) {
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public QrDisplayViewModel get() {
    return newInstance(identityRepositoryProvider.get());
  }

  public static QrDisplayViewModel_Factory create(
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new QrDisplayViewModel_Factory(identityRepositoryProvider);
  }

  public static QrDisplayViewModel newInstance(IdentityRepository identityRepository) {
    return new QrDisplayViewModel(identityRepository);
  }
}
