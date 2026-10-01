// data/local/AuthStore.kt
// Secure storage for auth tokens using EncryptedSharedPreferences.
package com.cryptika.messenger.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.security.GeneralSecurityException
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
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs: SharedPreferences by lazy {
        createEncryptedPrefs()
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                "cryptika_auth_store",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            when (e) {
                is GeneralSecurityException, is IOException -> {
                    context.deleteSharedPreferences("cryptika_auth_store")
                    EncryptedSharedPreferences.create(
                        context,
                        "cryptika_auth_store",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                }
                else -> throw e
            }
        }
    }

    var jwtToken: String?
        get() = prefs.getString(KEY_JWT, null)
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_JWT).commit()
            } else {
                prefs.edit().putString(KEY_JWT, value).commit()
            }
        }

    var contactToken: String?
        get() = prefs.getString(KEY_CONTACT_TOKEN, null)
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_CONTACT_TOKEN).commit()
            } else {
                prefs.edit().putString(KEY_CONTACT_TOKEN, value).commit()
            }
        }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_USERNAME).commit()
            } else {
                prefs.edit().putString(KEY_USERNAME, value).commit()
            }
        }

    var tokenExpiresAt: Long
        get() = prefs.getLong(KEY_EXPIRES_AT, 0)
        set(value) = prefs.edit().putLong(KEY_EXPIRES_AT, value).commit().let { }

    var credentialsBurned: Boolean
        get() = prefs.getBoolean(KEY_CREDENTIALS_BURNED, false)
        set(value) = prefs.edit().putBoolean(KEY_CREDENTIALS_BURNED, value).commit().let { }

    val isLoggedIn: Boolean
        get() {
            if (credentialsBurned) return false
            val token = jwtToken ?: return false
            return token.isNotEmpty() && tokenExpiresAt > System.currentTimeMillis()
        }

    fun burnCredentials() {
        prefs.edit()
            .putBoolean(KEY_CREDENTIALS_BURNED, true)
            .remove(KEY_JWT)
            .remove(KEY_CONTACT_TOKEN)
            .putLong(KEY_EXPIRES_AT, 0)
            .commit()
    }

    fun clear() {
        prefs.edit().clear().commit()
    }

    companion object {
        private const val KEY_JWT = "jwt_token"
        private const val KEY_CONTACT_TOKEN = "contact_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_EXPIRES_AT = "token_expires_at"
        private const val KEY_CREDENTIALS_BURNED = "credentials_burned"
    }
}
