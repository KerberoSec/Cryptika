package com.cryptika.messenger.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MessageExpiryWorker_AssistedFactory_Impl implements MessageExpiryWorker_AssistedFactory {
  private final MessageExpiryWorker_Factory delegateFactory;

  MessageExpiryWorker_AssistedFactory_Impl(MessageExpiryWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public MessageExpiryWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<MessageExpiryWorker_AssistedFactory> create(
      MessageExpiryWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MessageExpiryWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<MessageExpiryWorker_AssistedFactory> createFactoryProvider(
      MessageExpiryWorker_Factory delegateFactory) {
    return InstanceFactory.create(new MessageExpiryWorker_AssistedFactory_Impl(delegateFactory));
  }
}
