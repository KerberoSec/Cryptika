package com.cryptika.messenger.presentation.ui.components;

import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.unit.Dp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000L\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0003\u001a \u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u001ad\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\n2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u000eH\u0003\u001a<\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\n2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u001c\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a \u0010\u001f\u001a\u00020\f2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u001aT\u0010 \u001a\u00020\f2\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\f0!2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u000eH\u0003\u001a(\u0010$\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\n2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u001ax\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\'2\u0012\u0010(\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\f0!2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010*\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u000eH\u0003\u001aN\u0010+\u001a\u00020\f2\u0006\u0010,\u001a\u00020-2\u0012\u0010.\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\f0!2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010/\u001a\b\u0012\u0004\u0012\u00020\f0\u000eH\u0007\u001a(\u00100\u001a\u00020\f2\u0006\u00101\u001a\u00020\'2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u001a \u00102\u001a\u00020\f2\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0003\u001ax\u00103\u001a\u00020\f2\u0006\u00104\u001a\u0002052\u0012\u00106\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\f0!2\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u00107\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u000eH\u0003\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"&\u0010\u0007\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\n\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\b0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u00068"}, d2 = {"KEY_HEIGHT", "Landroidx/compose/ui/unit/Dp;", "F", "KEY_RADIUS", "KEY_SPACING", "NUM_KEY_HEIGHT", "ROW_SPACING", "emojiData", "", "Lkotlin/Pair;", "", "BackspaceKey", "", "onDelete", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "BottomRow", "leftLabel", "onLeft", "onComma", "onEmoji", "onSpace", "onPeriod", "onDone", "CharKey", "label", "onClick", "height", "CharKey-eqLRuRQ", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;F)V", "DoneKey", "EmojiKeys", "Lkotlin/Function1;", "onBackspace", "onAbc", "LabelKey", "LetterKeys", "shiftState", "Lcom/cryptika/messenger/presentation/ui/components/ShiftState;", "onChar", "onShift", "onSymbols", "SecureKeyboard", "visible", "", "onKeyPress", "onToggle", "ShiftKey", "state", "SpaceKey", "SymbolKeys", "page", "", "onKey", "onPage", "Cryptika_release"})
public final class SecureKeyboardKt {
    private static final float KEY_HEIGHT = 0.0F;
    private static final float NUM_KEY_HEIGHT = 0.0F;
    private static final float KEY_SPACING = 0.0F;
    private static final float KEY_RADIUS = 0.0F;
    private static final float ROW_SPACING = 0.0F;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<kotlin.Pair<java.lang.String, java.util.List<java.lang.String>>> emojiData = null;
    
    @androidx.compose.runtime.Composable()
    public static final void SecureKeyboard(boolean visible, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKeyPress, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDone, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onToggle) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LetterKeys(com.cryptika.messenger.presentation.ui.components.ShiftState shiftState, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onChar, kotlin.jvm.functions.Function0<kotlin.Unit> onShift, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, kotlin.jvm.functions.Function0<kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SymbolKeys(int page, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onAbc, kotlin.jvm.functions.Function0<kotlin.Unit> onPage, kotlin.jvm.functions.Function0<kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EmojiKeys(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onAbc, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void BottomRow(java.lang.String leftLabel, kotlin.jvm.functions.Function0<kotlin.Unit> onLeft, kotlin.jvm.functions.Function0<kotlin.Unit> onComma, kotlin.jvm.functions.Function0<kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onPeriod, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    /**
     * Special key with text label (ABC, ?123, ½, etc.).
     */
    @androidx.compose.runtime.Composable()
    private static final void LabelKey(java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Shift key with three visual states: off → single → caps-lock.
     */
    @androidx.compose.runtime.Composable()
    private static final void ShiftKey(com.cryptika.messenger.presentation.ui.components.ShiftState state, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Backspace key with repeat-on-hold (hold > 400 ms starts repeating).
     */
    @androidx.compose.runtime.Composable()
    private static final void BackspaceKey(kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Space bar showing "space" label.
     */
    @androidx.compose.runtime.Composable()
    private static final void SpaceKey(kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Done / Enter key – highlighted in primary color.
     */
    @androidx.compose.runtime.Composable()
    private static final void DoneKey(kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
}