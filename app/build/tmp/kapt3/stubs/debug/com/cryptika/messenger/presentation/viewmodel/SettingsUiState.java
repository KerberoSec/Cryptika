package com.cryptika.messenger.presentation.viewmodel;

import android.util.Base64;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import com.cryptika.messenger.data.remote.api.RelayApi;
import com.cryptika.messenger.data.remote.ServerConfig;
import com.cryptika.messenger.data.remote.websocket.RelayEvent;
import com.cryptika.messenger.domain.crypto.*;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.domain.repository.*;
import dagger.hilt.android.lifecycle.HiltViewModel;
import com.cryptika.messenger.data.remote.CallManager;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.*;
import java.util.UUID;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b!\b\u0086\b\u0018\u00002\u00020\u0001B_\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\r\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0005H\u00c6\u0003Jc\u0010#\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\u0005H\u00c6\u0001J\u0013\u0010$\u001a\u00020\u00052\b\u0010%\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010&\u001a\u00020\u0007H\u00d6\u0001J\t\u0010\'\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010R\u0011\u0010\r\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u0015R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0015R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0010R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015\u00a8\u0006("}, d2 = {"Lcom/cryptika/messenger/presentation/viewmodel/SettingsUiState;", "", "fingerprintHex", "", "screenshotBlockingEnabled", "", "defaultExpirySeconds", "", "appVersion", "showRegenerateConfirm", "serverRelayUrl", "pingStatus", "isPinging", "forceLogout", "(Ljava/lang/String;ZILjava/lang/String;ZLjava/lang/String;Ljava/lang/String;ZZ)V", "getAppVersion", "()Ljava/lang/String;", "getDefaultExpirySeconds", "()I", "getFingerprintHex", "getForceLogout", "()Z", "getPingStatus", "getScreenshotBlockingEnabled", "getServerRelayUrl", "getShowRegenerateConfirm", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "Cryptika_debug"})
public final class SettingsUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fingerprintHex = null;
    private final boolean screenshotBlockingEnabled = false;
    private final int defaultExpirySeconds = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String appVersion = null;
    private final boolean showRegenerateConfirm = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String serverRelayUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String pingStatus = null;
    private final boolean isPinging = false;
    private final boolean forceLogout = false;
    
    public SettingsUiState(@org.jetbrains.annotations.NotNull()
    java.lang.String fingerprintHex, boolean screenshotBlockingEnabled, int defaultExpirySeconds, @org.jetbrains.annotations.NotNull()
    java.lang.String appVersion, boolean showRegenerateConfirm, @org.jetbrains.annotations.NotNull()
    java.lang.String serverRelayUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String pingStatus, boolean isPinging, boolean forceLogout) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFingerprintHex() {
        return null;
    }
    
    public final boolean getScreenshotBlockingEnabled() {
        return false;
    }
    
    public final int getDefaultExpirySeconds() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAppVersion() {
        return null;
    }
    
    public final boolean getShowRegenerateConfirm() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getServerRelayUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPingStatus() {
        return null;
    }
    
    public final boolean isPinging() {
        return false;
    }
    
    public final boolean getForceLogout() {
        return false;
    }
    
    public SettingsUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.presentation.viewmodel.SettingsUiState copy(@org.jetbrains.annotations.NotNull()
    java.lang.String fingerprintHex, boolean screenshotBlockingEnabled, int defaultExpirySeconds, @org.jetbrains.annotations.NotNull()
    java.lang.String appVersion, boolean showRegenerateConfirm, @org.jetbrains.annotations.NotNull()
    java.lang.String serverRelayUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String pingStatus, boolean isPinging, boolean forceLogout) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}