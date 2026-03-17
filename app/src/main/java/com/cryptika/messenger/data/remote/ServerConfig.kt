// data/remote/ServerConfig.kt
// Runtime-configurable relay server URL, persisted in SharedPreferences.
package com.cryptika.messenger.data.remote

import android.content.Context
import com.cryptika.messenger.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds and persists the relay server URL so users can change it at runtime.
 *
 * Only the WebSocket URL needs to be set (e.g. ws://192.168.1.10:8443 for development,
 * or wss://yourdomain.com for production).
 * The HTTP(S) base URL for REST calls is derived automatically by swapping the scheme:
 *   ws://  → http://
 *   wss:// → https://
 *
 * TLS enforcement is handled at the OS level via android:usesCleartextTraffic in the
 * manifest (true in debug builds, false in release builds). Doing a forced scheme upgrade
 * here would cause "unable to parse TLS packet header" errors against plain-HTTP servers.
 *
 * Default: [BuildConfig.RELAY_BASE_URL] (set at build time in build.gradle.kts).
 */
@Singleton
class ServerConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("cryptika_settings", Context.MODE_PRIVATE)

    /** WebSocket base URL exactly as configured (ws:// or wss://). */
    var relayBaseUrl: String
        get() {
            var stored = prefs.getString(KEY_RELAY_URL, null)
            // One-time migration: pre-fix builds forced ws:// → wss:// in the setter so
            // SharedPreferences may still hold "wss://..." even on a debug build whose
            // BuildConfig default is "ws://...". Silently downgrade the stored value so
            // the client never sends a TLS ClientHello to a plain-HTTP server.
            if (stored != null) {
                val buildScheme = if (BuildConfig.RELAY_BASE_URL.startsWith("wss://")) "wss://" else "ws://"
                val storedScheme = if (stored.startsWith("wss://")) "wss://" else "ws://"
                if (buildScheme != storedScheme) {
                    stored = buildScheme + stored.removePrefix(storedScheme)
                    prefs.edit().putString(KEY_RELAY_URL, stored).apply()
                }
            }
            return (stored ?: BuildConfig.RELAY_BASE_URL).trimEnd('/')
        }
        set(value) {
            prefs.edit().putString(KEY_RELAY_URL, value.trimEnd('/')).apply()
        }

    /** HTTP(S) base URL derived from the relay URL (ws→http, wss→https). */
    val apiBaseUrl: String
        get() = relayBaseUrl
            .replace("wss://", "https://")
            .replace("ws://",  "http://")

    companion object {
        private const val KEY_RELAY_URL = "server_relay_url"
    }
}
