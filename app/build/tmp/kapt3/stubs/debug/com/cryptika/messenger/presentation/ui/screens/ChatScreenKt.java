package com.cryptika.messenger.presentation.ui.screens;

import androidx.compose.animation.*;
import androidx.compose.foundation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.*;
import androidx.compose.foundation.shape.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.*;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.unit.*;
import com.cryptika.messenger.domain.model.*;
import com.cryptika.messenger.presentation.ui.theme.*;
import com.cryptika.messenger.presentation.viewmodel.*;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000f\n\u0000\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a`\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00012\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\u00060\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bH\u0003\u001aT\u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00012\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\u000e\b\u0002\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\u000e\b\u0002\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00012\b\b\u0002\u0010\u0018\u001a\u00020\u0019H\u0007\u001aJ\u0010\u001a\u001a\u00020\u00062\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\u0006\u0010\u001d\u001a\u00020\u00022\u0006\u0010\f\u001a\u00020\r2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\u000e\b\u0002\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\b\b\u0002\u0010\u001e\u001a\u00020\u001fH\u0003\u001a\u0010\u0010 \u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u0002H\u0003\u001a\u001e\u0010\"\u001a\u00020\u00062\u0006\u0010\u001d\u001a\u00020\u00022\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bH\u0003\u001a2\u0010$\u001a\u00020\u00062\u0006\u0010%\u001a\u00020\u000f2\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00060\t2\f\u0010\'\u001a\b\u0012\u0004\u0012\u00020\u00060\u000bH\u0003\u001a<\u0010(\u001a\u00020\u00062\u0006\u0010)\u001a\u00020*2\u000e\b\u0002\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00060\u000b2\u001a\b\u0002\u0010,\u001a\u0014\u0012\u0004\u0012\u00020\u0001\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00060-H\u0003\u001a\b\u0010.\u001a\u00020\u0006H\u0003\u001a\u0010\u0010/\u001a\u00020\u00012\u0006\u00100\u001a\u000201H\u0002\u001a\u0011\u00102\u001a\u000203*\u00020\u0002H\u0003\u00a2\u0006\u0002\u00104\"\u0018\u0010\u0000\u001a\u00020\u0001*\u00020\u00028BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0003\u0010\u0004\u00a8\u00065"}, d2 = {"label", "", "Lcom/cryptika/messenger/domain/model/ConnectionState;", "getLabel", "(Lcom/cryptika/messenger/domain/model/ConnectionState;)Ljava/lang/String;", "ChatInputBar", "", "text", "onTextChange", "Lkotlin/Function1;", "onSend", "Lkotlin/Function0;", "sessionEstablished", "", "expirySeconds", "", "onTimerClick", "onFieldClick", "ChatScreen", "contactId", "onBack", "onStartCall", "onForceLogout", "sessionUUID", "viewModel", "Lcom/cryptika/messenger/presentation/viewmodel/ChatViewModel;", "ChatTopBar", "contact", "Lcom/cryptika/messenger/domain/model/Contact;", "connectionState", "ephemeralState", "Lcom/cryptika/messenger/domain/model/EphemeralSessionState;", "ConnectionIndicator", "state", "ConnectionTroubleshootCard", "onRetry", "ExpiryPickerSheet", "selectedSeconds", "onSelect", "onDismiss", "MessageBubble", "message", "Lcom/cryptika/messenger/domain/model/Message;", "onExpired", "onDeleteMessage", "Lkotlin/Function2;", "SessionStatusBanner", "formatCountdown", "seconds", "", "indicatorColor", "Landroidx/compose/ui/graphics/Color;", "(Lcom/cryptika/messenger/domain/model/ConnectionState;)J", "Cryptika_debug"})
public final class ChatScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ChatScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String contactId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStartCall, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onForceLogout, @org.jetbrains.annotations.Nullable()
    java.lang.String sessionUUID, @org.jetbrains.annotations.NotNull()
    com.cryptika.messenger.presentation.viewmodel.ChatViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void ChatTopBar(com.cryptika.messenger.domain.model.Contact contact, com.cryptika.messenger.domain.model.ConnectionState connectionState, boolean sessionEstablished, kotlin.jvm.functions.Function0<kotlin.Unit> onBack, kotlin.jvm.functions.Function0<kotlin.Unit> onStartCall, com.cryptika.messenger.domain.model.EphemeralSessionState ephemeralState) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConnectionIndicator(com.cryptika.messenger.domain.model.ConnectionState state) {
    }
    
    private static final java.lang.String getLabel(com.cryptika.messenger.domain.model.ConnectionState $this$label) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    private static final long indicatorColor(com.cryptika.messenger.domain.model.ConnectionState $this$indicatorColor) {
        return 0L;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SessionStatusBanner() {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class, androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable()
    private static final void MessageBubble(com.cryptika.messenger.domain.model.Message message, kotlin.jvm.functions.Function0<kotlin.Unit> onExpired, kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.Boolean, kotlin.Unit> onDeleteMessage) {
    }
    
    private static final java.lang.String formatCountdown(long seconds) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ChatInputBar(java.lang.String text, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onTextChange, kotlin.jvm.functions.Function0<kotlin.Unit> onSend, boolean sessionEstablished, int expirySeconds, kotlin.jvm.functions.Function0<kotlin.Unit> onTimerClick, kotlin.jvm.functions.Function0<kotlin.Unit> onFieldClick) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void ExpiryPickerSheet(int selectedSeconds, kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConnectionTroubleshootCard(com.cryptika.messenger.domain.model.ConnectionState connectionState, kotlin.jvm.functions.Function0<kotlin.Unit> onRetry) {
    }
}