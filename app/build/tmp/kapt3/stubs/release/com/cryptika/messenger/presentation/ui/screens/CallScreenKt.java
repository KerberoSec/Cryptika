package com.cryptika.messenger.presentation.ui.screens;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.*;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.unit.*;
import androidx.core.content.ContextCompat;
import com.cryptika.messenger.domain.model.CallState;
import com.cryptika.messenger.presentation.viewmodel.CallViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\b\n\u0000\u001aB\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0003\u001a.\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00032\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0003\u001a0\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u00032\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0007\u001a$\u0010\u0016\u001a\u00020\u00012\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u00062\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0003\u001a\u0016\u0010\u0019\u001a\u00020\u00012\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006H\u0003\u001a\b\u0010\u001b\u001a\u00020\u0001H\u0003\u001a\u0010\u0010\u001c\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u001eH\u0002\u00a8\u0006\u001f"}, d2 = {"ActiveCallButtons", "", "isMuted", "", "isSpeakerOn", "onToggleMute", "Lkotlin/Function0;", "onToggleSpeaker", "onHangup", "CallActionButton", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "label", "", "active", "onClick", "CallScreen", "contactId", "isIncoming", "onCallEnded", "viewModel", "Lcom/cryptika/messenger/presentation/viewmodel/CallViewModel;", "IncomingCallButtons", "onAnswer", "onReject", "OutgoingRingingButtons", "onCancel", "RingingPulse", "formatDuration", "seconds", "", "Cryptika_release"})
public final class CallScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void CallScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId, boolean isIncoming, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onCallEnded, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.presentation.viewmodel.CallViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void IncomingCallButtons(kotlin.jvm.functions.Function0<kotlin.Unit> onAnswer, kotlin.jvm.functions.Function0<kotlin.Unit> onReject) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void OutgoingRingingButtons(kotlin.jvm.functions.Function0<kotlin.Unit> onCancel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ActiveCallButtons(boolean isMuted, boolean isSpeakerOn, kotlin.jvm.functions.Function0<kotlin.Unit> onToggleMute, kotlin.jvm.functions.Function0<kotlin.Unit> onToggleSpeaker, kotlin.jvm.functions.Function0<kotlin.Unit> onHangup) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CallActionButton(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, boolean active, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RingingPulse() {
    }
    
    private static final java.lang.String formatDuration(int seconds) {
        return null;
    }
}