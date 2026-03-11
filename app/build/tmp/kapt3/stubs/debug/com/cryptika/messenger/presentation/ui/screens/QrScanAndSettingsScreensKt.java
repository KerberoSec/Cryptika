package com.cryptika.messenger.presentation.ui.screens;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.shape.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.*;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.unit.*;
import androidx.core.content.ContextCompat;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.cryptika.messenger.domain.model.AppVersion;
import com.cryptika.messenger.presentation.ui.theme.*;
import com.cryptika.messenger.presentation.viewmodel.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000@\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u001a$\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a6\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\u00062\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u001a4\u0010\u000e\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\f\u001a\u00020\u0010H\u0007\u001a\b\u0010\u0011\u001a\u00020\u0001H\u0003\u001a>\u0010\u0012\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\n2\b\b\u0002\u0010\f\u001a\u00020\u0015H\u0007\u001a\u0010\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u0006H\u0003\u001a\u0012\u0010\u0018\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0019\u001a\u00020\u001aH\u0002\u00a8\u0006\u001b"}, d2 = {"CameraPreview", "", "modifier", "Landroidx/compose/ui/Modifier;", "onQrCodeDetected", "Lkotlin/Function1;", "", "ContactConfirmScreen", "publicKeyB64", "onBack", "Lkotlin/Function0;", "onConfirmed", "viewModel", "Lcom/cryptika/messenger/presentation/viewmodel/ContactConfirmViewModel;", "QrScanScreen", "onScanSuccess", "Lcom/cryptika/messenger/presentation/viewmodel/QrScanViewModel;", "ScanningOverlay", "SettingsScreen", "onNavigateToQrDisplay", "onForceLogout", "Lcom/cryptika/messenger/presentation/viewmodel/SettingsViewModel;", "SettingsSectionHeader", "title", "decodeQrFromImage", "imageProxy", "Landroidx/camera/core/ImageProxy;", "Cryptika_debug"})
public final class QrScanAndSettingsScreensKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void QrScanScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onScanSuccess, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.presentation.viewmodel.QrScanViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CameraPreview(androidx.compose.ui.Modifier modifier, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQrCodeDetected) {
    }
    
    private static final java.lang.String decodeQrFromImage(androidx.camera.core.ImageProxy imageProxy) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ScanningOverlay() {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ContactConfirmScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String publicKeyB64, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirmed, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.presentation.viewmodel.ContactConfirmViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SettingsScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToQrDisplay, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onForceLogout, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.presentation.viewmodel.SettingsViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingsSectionHeader(java.lang.String title) {
    }
}