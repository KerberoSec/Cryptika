package com.cryptika.messenger.presentation.ui.components;

import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.hapticfeedback.HapticFeedbackType;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.unit.Dp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\\\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\b\u001a \u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0003\u001aF\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u00102\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u001a\u001a\u00020\u00012\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001d\u0010\u001e\u001a \u0010\u001f\u001a\u00020\u00122\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0003\u001aT\u0010 \u001a\u00020\u00122\u0012\u0010!\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00120\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u0003\u001a<\u0010\'\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u00102\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u001a\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b(\u0010)\u001a6\u0010*\u001a\u00020\u00122\u0006\u0010+\u001a\u00020\b2\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010-\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0003\u001a2\u0010.\u001a\u00020\u00122\u0006\u0010/\u001a\u00020\b2\u0012\u00100\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00120\"2\f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u0003\u001a\u009c\u0001\u00102\u001a\u00020\u00122\u0006\u0010+\u001a\u00020\b2\u0006\u00103\u001a\u0002042\u0012\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00120\"2\f\u00106\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u00108\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u0003\u001ap\u0010:\u001a\u00020\u00122\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00120\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u00107\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u0003\u001a2\u0010<\u001a\u00020\u00122\f\u0010=\u001a\b\u0012\u0004\u0012\u00020\u00100\u000e2\u0012\u00105\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00120\"2\u0006\u0010\u001b\u001a\u00020\u001cH\u0003\u001aN\u0010>\u001a\u00020\u00122\u0006\u0010?\u001a\u00020@2\u0012\u0010A\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00120\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010B\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u0007\u001a(\u0010C\u001a\u00020\u00122\u0006\u0010D\u001a\u0002042\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0003\u001a*\u0010E\u001a\u00020\u00122\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0018\u001a\u00020\u0010H\u0003\u001ab\u0010F\u001a\u00020\u00122\u0012\u0010;\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00120\"2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010G\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00120\u00142\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014H\u0003\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\"\u0010\u0010\n\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u000b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\f\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"&\u0010\r\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000e0\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006H"}, d2 = {"H_PAD", "Landroidx/compose/ui/unit/Dp;", "F", "KEY_HEIGHT", "KEY_RADIUS", "KEY_SPACING", "LAYOUTS", "", "Lcom/cryptika/messenger/presentation/ui/components/KeyboardLanguage;", "Lcom/cryptika/messenger/presentation/ui/components/LangLayout;", "NUMPAD_KEY_H", "NUM_KEY_H", "ROW_SPACING", "emojiData", "", "Lkotlin/Pair;", "", "BackspaceKey", "", "onDelete", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "CharKey", "label", "onClick", "height", "fontScale", "", "CharKey-2lqI77k", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;FF)V", "DoneKey", "EmojiKeys", "onEmoji", "Lkotlin/Function1;", "onBackspace", "onAbc", "onSpace", "onDone", "LabelKey", "LabelKey-eqLRuRQ", "(Ljava/lang/String;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;F)V", "LangKey", "language", "onPress", "onLongPress", "LanguagePickerDialog", "current", "onSelect", "onDismiss", "LetterKeys", "shiftState", "Lcom/cryptika/messenger/presentation/ui/components/ShiftState;", "onChar", "onShift", "onSymbols", "onLangCycle", "onLangLongPress", "NumpadKeys", "onKey", "ScriptRow", "keys", "SecureKeyboard", "visible", "", "onKeyPress", "onToggle", "ShiftKey", "state", "SpaceKey", "SymbolKeys", "onNumpad", "Cryptika_release"})
public final class SecureKeyboardKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<com.cryptika.messenger.presentation.ui.components.KeyboardLanguage, com.cryptika.messenger.presentation.ui.components.LangLayout> LAYOUTS = null;
    private static final float KEY_HEIGHT = 0.0F;
    private static final float NUM_KEY_H = 0.0F;
    private static final float NUMPAD_KEY_H = 0.0F;
    private static final float KEY_SPACING = 0.0F;
    private static final float KEY_RADIUS = 0.0F;
    private static final float ROW_SPACING = 0.0F;
    private static final float H_PAD = 0.0F;
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
    private static final void LanguagePickerDialog(com.cryptika.messenger.presentation.ui.components.KeyboardLanguage current, kotlin.jvm.functions.Function1<? super com.cryptika.messenger.presentation.ui.components.KeyboardLanguage, kotlin.Unit> onSelect, kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LetterKeys(com.cryptika.messenger.presentation.ui.components.KeyboardLanguage language, com.cryptika.messenger.presentation.ui.components.ShiftState shiftState, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onChar, kotlin.jvm.functions.Function0<kotlin.Unit> onShift, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, kotlin.jvm.functions.Function0<kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone, kotlin.jvm.functions.Function0<kotlin.Unit> onLangCycle, kotlin.jvm.functions.Function0<kotlin.Unit> onLangLongPress) {
    }
    
    /**
     * A full-width row of equally-weighted character keys.
     */
    @androidx.compose.runtime.Composable()
    private static final void ScriptRow(java.util.List<java.lang.String> keys, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onChar, float fontScale) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SymbolKeys(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onAbc, kotlin.jvm.functions.Function0<kotlin.Unit> onNumpad, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void NumpadKeys(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onKey, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onAbc, kotlin.jvm.functions.Function0<kotlin.Unit> onSymbols, kotlin.jvm.functions.Function0<kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void EmojiKeys(kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onEmoji, kotlin.jvm.functions.Function0<kotlin.Unit> onBackspace, kotlin.jvm.functions.Function0<kotlin.Unit> onAbc, kotlin.jvm.functions.Function0<kotlin.Unit> onSpace, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    /**
     * Language key: shows language code; tap = cycle, long-press = picker.
     */
    @androidx.compose.runtime.Composable()
    private static final void LangKey(com.cryptika.messenger.presentation.ui.components.KeyboardLanguage language, kotlin.jvm.functions.Function0<kotlin.Unit> onPress, kotlin.jvm.functions.Function0<kotlin.Unit> onLongPress, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Shift key: three visual states (off / single / caps-lock).
     */
    @androidx.compose.runtime.Composable()
    private static final void ShiftKey(com.cryptika.messenger.presentation.ui.components.ShiftState state, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Backspace with repeat-on-hold (400 ms then 40 ms repeat).
     */
    @androidx.compose.runtime.Composable()
    private static final void BackspaceKey(kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Space bar: optionally shows a language name hint.
     */
    @androidx.compose.runtime.Composable()
    private static final void SpaceKey(kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier, java.lang.String label) {
    }
    
    /**
     * Done / Enter key, highlighted in primary colour.
     */
    @androidx.compose.runtime.Composable()
    private static final void DoneKey(kotlin.jvm.functions.Function0<kotlin.Unit> onClick, androidx.compose.ui.Modifier modifier) {
    }
}