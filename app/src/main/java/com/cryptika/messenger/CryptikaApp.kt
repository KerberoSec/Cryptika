// CryptikaApp.kt
package com.cryptika.messenger

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CryptikaApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var backgroundConnectionManager: com.cryptika.messenger.data.remote.BackgroundConnectionManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            com.cryptika.messenger.data.remote.ConnectionForegroundService.start(this)
        } catch (_: Exception) {}
        backgroundConnectionManager.startAllConnections()
        try {
            com.cryptika.messenger.data.local.worker.MessageExpiryWorker.schedule(this)
        } catch (_: Exception) {}
    }
}
