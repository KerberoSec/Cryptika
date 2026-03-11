package com.cryptika.messenger.presentation.ui.components;

import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000<\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\u001a:\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a0\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00052\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\r\u001a\u00020\u000eH\u0003\u001a<\u0010\u000f\u001a\u00020\u00012\u0006\u0010\u0010\u001a\u00020\u00052\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u0012H\u0003\u001a~\u0010\u0014\u001a\u00020\u00012\u0012\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00160\u00162\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0006\u0010\u001b\u001a\u00020\u0005H\u0003\u001ab\u0010\u001c\u001a\u00020\u00012\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001aN\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00122\u0012\u0010\u001f\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0007\u001aT\u0010!\u001a\u00020\u00012\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001ab\u0010#\u001a\u00020\u00012\u0012\u0010\u0017\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00010\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u00a8\u0006$"}, d2 = {"BottomRow", "", "onSymbols", "Lkotlin/Function0;", "symbolLabel", "", "onSpace", "onDone", "IconKeyButton", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "contentDescription", "onClick", "modifier", "Landroidx/compose/ui/Modifier;", "KeyButton", "label", "isSpecial", "", "isSpace", "KeyboardLayout", "rows", "", "onKey", "Lkotlin/Function1;", "onBackspace", "onShift", "shiftLabel", "LowerKeyboard", "SecureKeyboard", "visible", "onKeyPress", "onToggle", "SymbolKeyboard", "onAbc", "UpperKeyboard", "Cryptika_debug"})
public final class SecureKeyboardKt {
    
    /**
     * In-app secure keyboard that replaces the system keyboard.
     * Fully self-contained — no system IME involvement.
     *
     * @param visible Whether the keyboard is displayed
     * @param onKeyPress Called when a character key is pressed, provides the character string
     * @param onBackspace Called when the backspace key is pressed
     * @param onDone Called when the done/enter key is pressed
     * @param onToggle Called to toggle keyboard visibility (collapse/expand)
     */
    @androidx.compose.runtime.Composable()
    public static final void SecureKeyboard(boolean visible, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKeyPress, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDone, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onToggle) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LowerKeyboard(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onShift, kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void UpperKeyboard(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onShift, kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SymbolKeyboard(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onAbc, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void KeyboardLayout(java.util.List<? extends java.util.List<java.lang.String>> rows, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onShift, kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone, java.lang.String shiftLabel) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void BottomRow(kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, java.lang.String symbolLabel, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void KeyButton(java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier, boolean isSpecial, boolean isSpace) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void IconKeyButton(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String contentDescription, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
}