# Cryptika Messenger ProGuard Rules

# Keep BouncyCastle crypto primitives
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Keep Room entities
-keep class com.cryptika.messenger.data.local.db.** { *; }

# Keep domain models & API DTOs (Gson serialization & Retrofit)
-keep class com.cryptika.messenger.domain.model.** { *; }
-keep class com.cryptika.messenger.data.remote.api.** { *; }

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Keep Hilt generated code
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# ZXing
-keep class com.google.zxing.** { *; }

# Google Tink / error-prone annotations (transitive via AndroidX Security Crypto)
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
