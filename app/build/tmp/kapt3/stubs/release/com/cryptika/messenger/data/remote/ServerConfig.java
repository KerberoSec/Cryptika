package com.cryptika.messenger.data.remote;

import android.content.Context;
import com.cryptika.messenger.BuildConfig;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Holds and persists the relay server URL so users can change it at runtime.
 *
 * Only the WebSocket URL needs to be set (e.g. ws://192.168.1.10:8443).
 * The HTTP base URL is derived automatically by swapping the scheme.
 *
 * Default: [BuildConfig.RELAY_BASE_URL] (set at build time).
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n \u000b*\u0004\u0018\u00010\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R$\u0010\r\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u000e\u0010\b\"\u0004\b\u000f\u0010\u0010\u00a8\u0006\u0012"}, d2 = {"Lcom/cryptika/messenger/data/remote/ServerConfig;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "apiBaseUrl", "", "getApiBaseUrl", "()Ljava/lang/String;", "prefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "value", "relayBaseUrl", "getRelayBaseUrl", "setRelayBaseUrl", "(Ljava/lang/String;)V", "Companion", "Cryptika_release"})
public final class ServerConfig {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_RELAY_URL = "server_relay_url";
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.data.remote.ServerConfig.Companion Companion = null;
    
    @javax.inject.Inject()
    public ServerConfig(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRelayBaseUrl() {
        return null;
    }
    
    public final void setRelayBaseUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getApiBaseUrl() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/cryptika/messenger/data/remote/ServerConfig$Companion;", "", "()V", "KEY_RELAY_URL", "", "Cryptika_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}