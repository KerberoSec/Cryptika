package com.cryptika.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.activity.ComponentActivity;
import androidx.compose.runtime.*;
import androidx.navigation.*;
import androidx.navigation.compose.*;
import com.cryptika.messenger.data.remote.BackgroundConnectionManager;
import com.cryptika.messenger.data.remote.EphemeralSessionManager;
import com.cryptika.messenger.domain.repository.AuthRepository;
import com.cryptika.messenger.presentation.ui.screens.*;
import com.cryptika.messenger.presentation.viewmodel.CallViewModel;
import com.cryptika.messenger.worker.MessageExpiryWorker;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.*;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\u0012\u0010\u001b\u001a\u00020\u001a2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0014J\b\u0010\u001e\u001a\u00020\u001aH\u0014J\b\u0010\u001f\u001a\u00020\u001aH\u0014R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001e\u0010\u000f\u001a\u00020\u00108\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/cryptika/messenger/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "authRepository", "Lcom/cryptika/messenger/domain/repository/AuthRepository;", "getAuthRepository", "()Lcom/cryptika/messenger/domain/repository/AuthRepository;", "setAuthRepository", "(Lcom/cryptika/messenger/domain/repository/AuthRepository;)V", "backgroundConnectionManager", "Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "getBackgroundConnectionManager", "()Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;", "setBackgroundConnectionManager", "(Lcom/cryptika/messenger/data/remote/BackgroundConnectionManager;)V", "ephemeralSessionManager", "Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "getEphemeralSessionManager", "()Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;", "setEphemeralSessionManager", "(Lcom/cryptika/messenger/data/remote/EphemeralSessionManager;)V", "screenOffReceiver", "Landroid/content/BroadcastReceiver;", "wipeScope", "Lkotlinx/coroutines/CoroutineScope;", "applyScreenshotBlockingPreference", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onResume", "Cryptika_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @javax.inject.Inject()
    public com.cryptika.messenger.data.remote.EphemeralSessionManager ephemeralSessionManager;
    @javax.inject.Inject()
    public com.cryptika.messenger.data.remote.BackgroundConnectionManager backgroundConnectionManager;
    @javax.inject.Inject()
    public com.cryptika.messenger.domain.repository.AuthRepository authRepository;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope wipeScope = null;
    
    /**
     * Screen-off → destroy all sessions, wipe auth, force re-register
     */
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver screenOffReceiver = null;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.EphemeralSessionManager getEphemeralSessionManager() {
        return null;
    }
    
    public final void setEphemeralSessionManager(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.EphemeralSessionManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.data.remote.BackgroundConnectionManager getBackgroundConnectionManager() {
        return null;
    }
    
    public final void setBackgroundConnectionManager(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.data.remote.BackgroundConnectionManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.cryptika.messenger.domain.repository.AuthRepository getAuthRepository() {
        return null;
    }
    
    public final void setAuthRepository(@org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.domain.repository.AuthRepository p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    private final void applyScreenshotBlockingPreference() {
    }
}