# HackSecure Messenger ProGuard Rules

# Keep BouncyCastle crypto primitives
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep Room entities
-keep class com.hacksecure.messenger.data.local.db.** { *; }

# Keep domain models (Gson serialization)
-keep class com.hacksecure.messenger.domain.model.** { *; }

# Keep Hilt generated code
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# ZXing
-keep class com.google.zxing.** { *; }
