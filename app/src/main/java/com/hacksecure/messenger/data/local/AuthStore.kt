// data/local/AuthStore.kt
// Secure storage for auth tokens using EncryptedSharedPreferences.
package com.cryptika.messenger.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores JWT token, contact token, and username in AES-256 encrypted SharedPreferences.
 * All sensitive auth data is encrypted at rest using AndroidKeystore-backed keys.
 */
@Singleton
class AuthStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "cryptika_auth_store",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var jwtToken: String?
        get() = prefs.getString(KEY_JWT, null)
        set(value) = prefs.edit().putString(KEY_JWT, value).apply()

    var contactToken: String?
        get() = prefs.getString(KEY_CONTACT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_CONTACT_TOKEN, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var tokenExpiresAt: Long
        get() = prefs.getLong(KEY_EXPIRES_AT, 0)
        set(value) = prefs.edit().putLong(KEY_EXPIRES_AT, value).apply()

    val isLoggedIn: Boolean
        get() {
            val token = jwtToken ?: return false
            return token.isNotEmpty() && tokenExpiresAt > System.currentTimeMillis()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_JWT = "jwt_token"
        private const val KEY_CONTACT_TOKEN = "contact_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EXPIRES_AT = "token_expires_at"
    }
}
