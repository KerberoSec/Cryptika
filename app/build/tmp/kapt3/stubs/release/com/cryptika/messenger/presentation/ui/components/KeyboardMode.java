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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/cryptika/messenger/presentation/ui/components/KeyboardMode;", "", "(Ljava/lang/String;I)V", "LETTERS", "SYMBOLS_1", "SYMBOLS_2", "EMOJI", "Cryptika_release"})
enum KeyboardMode {
    /*public static final*/ LETTERS /* = new LETTERS() */,
    /*public static final*/ SYMBOLS_1 /* = new SYMBOLS_1() */,
    /*public static final*/ SYMBOLS_2 /* = new SYMBOLS_2() */,
    /*public static final*/ EMOJI /* = new EMOJI() */;
    
    KeyboardMode() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.cryptika.messenger.presentation.ui.components.KeyboardMode> getEntries() {
        return null;
    }
}