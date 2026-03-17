// worker/MessageExpiryWorker.kt
package com.cryptika.messenger.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cryptika.messenger.domain.repository.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MessageExpiryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            messageRepository.deleteExpiredMessages()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "message_expiry_worker"

        fun schedule(context: Context) {
            val periodicRequest = PeriodicWorkRequestBuilder<MessageExpiryWorker>(
                15, TimeUnit.MINUTES   // WorkManager minimum is 15 min; 60s is silently ignored
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun runOnce(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<MessageExpiryWorker>().build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
