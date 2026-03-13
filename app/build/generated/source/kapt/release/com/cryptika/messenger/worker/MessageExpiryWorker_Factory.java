package com.cryptika.messenger.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.cryptika.messenger.domain.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
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
public final class MessageExpiryWorker_Factory {
  private final Provider<MessageRepository> messageRepositoryProvider;

  public MessageExpiryWorker_Factory(Provider<MessageRepository> messageRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  public MessageExpiryWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, messageRepositoryProvider.get());
  }

  public static MessageExpiryWorker_Factory create(
      Provider<MessageRepository> messageRepositoryProvider) {
    return new MessageExpiryWorker_Factory(messageRepositoryProvider);
  }

  public static MessageExpiryWorker newInstance(Context context, WorkerParameters params,
      MessageRepository messageRepository) {
    return new MessageExpiryWorker(context, params, messageRepository);
  }
}
