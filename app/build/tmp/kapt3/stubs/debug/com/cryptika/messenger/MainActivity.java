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

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0002J\u0012\u0010\u0005\u001a\u00020\u00042\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0014J\b\u0010\b\u001a\u00020\u0004H\u0014\u00a8\u0006\t"}, d2 = {"Lcom/cryptika/messenger/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "applyScreenshotBlockingPreference", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "Cryptika_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    private final void applyScreenshotBlockingPreference() {
    }
}