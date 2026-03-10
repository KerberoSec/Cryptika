// data/remote/ConnectionForegroundService.kt
// Foreground service that keeps the process alive while the app is in the background.
// Without this, Android (Doze mode / battery optimisation) terminates WebSocket connections
// within minutes of the app leaving the foreground.
package com.cryptika.messenger.data.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cryptika.messenger.R

/**
 * A minimal foreground service whose only job is to hold a PRIORITY_MIN notification so
 * Android does not kill the process while [BackgroundConnectionManager] keeps WebSocket
 * connections alive for all conversations.
 *
 * Does NOT extend HiltAndroidApp — no Hilt injection needed; we just keep the process alive.
 * All real work is performed inside [BackgroundConnectionManager] which lives in the
 * application-scope coroutine.
 *
 * Service type `remoteMessaging` (API 34) satisfies Android 14's strict foreground-service
 * type enforcement for apps that maintain network connections to deliver messages.
 */
class ConnectionForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID     = "cryptika_bg_connection"

        fun start(context: Context) {
            val intent = Intent(context, ConnectionForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ConnectionForegroundService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        // START_STICKY: if killed by OS, restart without delivering the original intent
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null  // not a bound service

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Secure Connection",
                NotificationManager.IMPORTANCE_MIN   // silent, no sound, minimal footprint
            ).apply {
                description = "Maintains encrypted relay connections in the background"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cryptika")
            .setContentText("Encrypted connection active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)           // cannot be dismissed by user
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
}
