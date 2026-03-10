// data/remote/ServerConfig.kt
// Runtime-configurable relay server URL — persisted in SharedPreferences.
// Allows physical devices on the same LAN to connect without rebuilding the app.
package com.cryptika.messenger.data.remote

import android.content.Context
import com.cryptika.messenger.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds and persists the relay server URL so users can change it at runtime.
 *
 * Only the WebSocket URL needs to be set (e.g. ws://192.168.1.10:8443).
 * The HTTP base URL is derived automatically by swapping the scheme.
 *
 * Default: [BuildConfig.RELAY_BASE_URL] (set at build time).
 */
@Singleton
class ServerConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("cryptika_settings", Context.MODE_PRIVATE)

    /** WebSocket base URL — e.g. "ws://10.0.2.2:8443" or "ws://192.168.1.10:8443" */
    var relayBaseUrl: String
        get() = (prefs.getString(KEY_RELAY_URL, null) ?: BuildConfig.RELAY_BASE_URL).trimEnd('/')
        set(value) = prefs.edit().putString(KEY_RELAY_URL, value.trimEnd('/')).apply()

    /** HTTP base URL derived from the relay URL — e.g. "http://10.0.2.2:8443" */
    val apiBaseUrl: String
        get() = relayBaseUrl
            .replace("wss://", "https://")
            .replace("ws://", "http://")

    companion object {
        private const val KEY_RELAY_URL = "server_relay_url"
    }
}
