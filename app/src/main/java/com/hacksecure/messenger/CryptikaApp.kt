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
        // Start foreground service FIRST — keeps the process alive under Doze/battery-optimisation.
        // BackgroundConnectionManager (started next) runs in this same process's coroutine scope.
        com.cryptika.messenger.data.remote.ConnectionForegroundService.start(this)
        backgroundConnectionManager.startAllConnections()
    }
}
