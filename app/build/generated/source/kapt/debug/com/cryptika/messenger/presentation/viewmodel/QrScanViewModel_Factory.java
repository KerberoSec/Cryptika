package com.cryptika.messenger.presentation.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class QrScanViewModel_Factory implements Factory<QrScanViewModel> {
  @Override
  public QrScanViewModel get() {
    return newInstance();
  }

  public static QrScanViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static QrScanViewModel newInstance() {
    return new QrScanViewModel();
  }

  private static final class InstanceHolder {
    private static final QrScanViewModel_Factory INSTANCE = new QrScanViewModel_Factory();
  }
}
