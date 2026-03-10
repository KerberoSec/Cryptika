package com.cryptika.messenger;

import android.os.Bundle;
import android.view.WindowManager;
import androidx.activity.ComponentActivity;
import androidx.compose.runtime.*;
import androidx.navigation.*;
import androidx.navigation.compose.*;
import com.cryptika.messenger.presentation.ui.screens.*;
import com.cryptika.messenger.presentation.viewmodel.CallViewModel;
import com.cryptika.messenger.worker.MessageExpiryWorker;
import dagger.hilt.android.AndroidEntryPoint;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\f\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u00042\b\b\u0002\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\r\u001a\u00020\u0004J\u000e\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/cryptika/messenger/Routes;", "", "()V", "CALL", "", "CHAT", "CONTACT_CONFIRM", "HOME", "QR_DISPLAY", "QR_SCAN", "SETTINGS", "SPLASH", "call", "contactId", "isIncoming", "", "chat", "contactConfirm", "pubKeyB64", "Cryptika_debug"})
public final class Routes {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SPLASH = "splash";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String HOME = "home";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String QR_DISPLAY = "qr_display";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String QR_SCAN = "qr_scan";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CONTACT_CONFIRM = "contact_confirm/{pubKeyB64}";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHAT = "chat/{contactId}";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SETTINGS = "settings";
    
    /**
     * Call screen: contactId + isIncoming flag
     */
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CALL = "call/{contactId}/{isIncoming}";
    @org.jetbrains.annotations.NotNull()
    public static final com.cryptika.messenger.Routes INSTANCE = null;
    
    private Routes() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String contactConfirm(@org.jetbrains.annotations.NotNull()
    java.lang.String pubKeyB64) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String chat(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String call(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId, boolean isIncoming) {
        return null;
    }
}