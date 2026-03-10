// data/remote/CallForegroundService.kt
// Foreground service with type "microphone" that keeps AudioRecord and AudioTrack alive
// while a call is active and the app is in the background.
package com.cryptika.messenger.data.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cryptika.messenger.R

/**
 * Foreground service that keeps [AudioRecord] and [AudioTrack] alive when the app is minimized.
 *
 * On API 26+ Android aggressively kills audio capture/playback for background apps that do not
 * hold a foreground service. Declaring `foregroundServiceType="microphone"` satisfies the
 * Android 12 (API 31) requirement and, together with the FOREGROUND_SERVICE_MICROPHONE permission
 * in the manifest, satisfies the Android 14 (API 34) requirement.
 *
 * Lifecycle:
 *  • Started by [CallManager.startAudio] the moment audio I/O begins (state = ACTIVE).
 *  • Stopped by [CallManager.cleanup] when the call ends for any reason.
 */
class CallForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID      = "cryptika_call"

        const val ACTION_START        = "com.cryptika.messenger.CALL_START"
        const val ACTION_STOP         = "com.cryptika.messenger.CALL_STOP"
        const val EXTRA_CONTACT_NAME  = "contact_name"

        /** Start (or update) the call foreground service. */
        fun start(context: Context, contactName: String) {
            val intent = Intent(context, CallForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CONTACT_NAME, contactName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Signal the service to tear down the foreground notification and stop itself. */
        fun stop(context: Context) {
            context.startService(
                Intent(context, CallForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
            )
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val name = intent.getStringExtra(EXTRA_CONTACT_NAME) ?: ""
                val notification = buildNotification(name)

                // On API 29+ pass the service type so the OS knows this foreground service
                // is using the microphone (required for Android 12+ strict enforcement).
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            }

            ACTION_STOP -> {
                @Suppress("DEPRECATION")
                stopForeground(true)   // compatible with all minSdk=26 devices
                stopSelf()
            }
        }
        // START_NOT_STICKY: do not recreate this service if killed — the call is already dead.
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active encrypted voice call"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contactName: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Encrypted call in progress")
            .setContentText(contactName.ifEmpty { "Unknown" })
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
}
