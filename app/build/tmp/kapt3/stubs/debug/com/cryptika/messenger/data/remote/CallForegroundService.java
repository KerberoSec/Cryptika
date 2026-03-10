package com.cryptika.messenger.data.remote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.cryptika.messenger.R;

/**
 * Foreground service that keeps [AudioRecord] and [AudioTrack] alive when the app is minimized.
 *
 * On API 26+ Android aggressively kills audio capture/playback for background apps that do not
 * hold a foreground service. Declaring `foregroundServiceType="microphone"` satisfies the
 * Android 12 (API 31) requirement and, together with the FOREGROUND_SERVICE_MICROPHONE permission
 * in the manifest, satisfies the Android 14 (API 34) requirement.
 *
 * Lifecycle:
 * • Started by [CallManager.startAudio] the moment audio I/O begins (state = ACTIVE).
 * • Stopped by [CallManager.cleanup] when the call ends for any reason.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\b\u0010\u0007\u001a\u00020\bH\u0002J\u0014\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0016J\b\u0010\r\u001a\u00020\bH\u0016J\"\u0010\u000e\u001a\u00020\u000f2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u000fH\u0016\u00a8\u0006\u0013"}, d2 = {"Lcom/cryptika/messenger/data/remote/CallForegroundService;", "Landroid/app/Service;", "()V", "buildNotification", "Landroid/app/Notification;", "contactName", "", "createChannel", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onStartCommand", "", "flags", "startId", "Companion", "Cryptika_debug"})
public final class CallForegroundService extends android.app.Service {
    private static final int NOTIFICATION_ID = 1002;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "cryptika_call";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_START = "com.cryptika.messenger.CALL_START";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "com.cryptika.messenger.CALL_STOP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_CONTACT_NAME = "contact_name";
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.remote.CallForegroundService.Companion Companion = null;
    
    public CallForegroundService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    private final void createChannel() {
    }
    
    private final android.app.Notification buildNotification(java.lang.String contactName) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u0004J\u000e\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/cryptika/messenger/data/remote/CallForegroundService$Companion;", "", "()V", "ACTION_START", "", "ACTION_STOP", "CHANNEL_ID", "EXTRA_CONTACT_NAME", "NOTIFICATION_ID", "", "start", "", "context", "Landroid/content/Context;", "contactName", "stop", "Cryptika_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Start (or update) the call foreground service.
         */
        public final void start(@org.jetbrains.annotations.NotNull()
        android.content.Context context, @org.jetbrains.annotations.NotNull()
        java.lang.String contactName) {
        }
        
        /**
         * Signal the service to tear down the foreground notification and stop itself.
         */
        public final void stop(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
    }
}