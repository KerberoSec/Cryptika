// presentation/ui/components/SecureKeyboard.kt
package com.cryptika.messenger.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Keyboard layout mode: lowercase, uppercase (shift), symbols.
 */
private enum class KeyboardMode { LOWER, UPPER, SYMBOLS }

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
@Composable
fun SecureKeyboard(
    visible: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var mode by remember { mutableStateOf(KeyboardMode.LOWER) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Toggle bar: tap to show/hide keyboard
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (visible) Icons.Default.KeyboardArrowDown
                    else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (visible) "Hide keyboard" else "Show keyboard",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (visible) "Hide Secure Keyboard" else "Show Secure Keyboard",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .navigationBarsPadding()
                ) {
                    when (mode) {
                        KeyboardMode.LOWER -> LowerKeyboard(
                            onKey = { key ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onKeyPress(key)
                            },
                            onBackspace = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onBackspace()
                            },
                            onShift = { mode = KeyboardMode.UPPER },
                            onSymbols = { mode = KeyboardMode.SYMBOLS },
                            onSpace = { onKeyPress(" ") },
                            onDone = onDone
                        )
                        KeyboardMode.UPPER -> UpperKeyboard(
                            onKey = { key ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onKeyPress(key)
                                mode = KeyboardMode.LOWER // auto-revert after one character
                            },
                            onBackspace = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onBackspace()
                            },
                            onShift = { mode = KeyboardMode.LOWER },
                            onSymbols = { mode = KeyboardMode.SYMBOLS },
                            onSpace = { onKeyPress(" ") },
                            onDone = onDone
                        )
                        KeyboardMode.SYMBOLS -> SymbolKeyboard(
                            onKey = { key ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onKeyPress(key)
                            },
                            onBackspace = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onBackspace()
                            },
                            onAbc = { mode = KeyboardMode.LOWER },
                            onSpace = { onKeyPress(" ") },
                            onDone = onDone
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// KEYBOARD LAYOUTS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LowerKeyboard(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onShift: () -> Unit,
    onSymbols: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )
    KeyboardLayout(rows, onKey, onBackspace, onShift, onSymbols, onSpace, onDone, shiftLabel = "⇧")
}

@Composable
private fun UpperKeyboard(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onShift: () -> Unit,
    onSymbols: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    val rows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("Z", "X", "C", "V", "B", "N", "M")
    )
    KeyboardLayout(rows, onKey, onBackspace, onShift, onSymbols, onSpace, onDone, shiftLabel = "⇩")
}

@Composable
private fun SymbolKeyboard(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onAbc: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
        listOf("!", "\"", "'", ":", ";", "/", "?")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1: numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            rows[0].forEach { key ->
                KeyButton(
                    label = key,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Row 2: symbols
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            rows[1].forEach { key ->
                KeyButton(
                    label = key,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Row 3: more symbols + backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyButton(
                label = "ABC",
                onClick = onAbc,
                modifier = Modifier.weight(1.5f),
                isSpecial = true
            )
            rows[2].forEach { key ->
                KeyButton(
                    label = key,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
            IconKeyButton(
                icon = Icons.Default.Backspace,
                contentDescription = "Backspace",
                onClick = onBackspace,
                modifier = Modifier.weight(1.5f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Row 4: space + done
        BottomRow(
            onSymbols = onAbc,
            symbolLabel = "ABC",
            onSpace = onSpace,
            onDone = onDone
        )
    }
}

@Composable
private fun KeyboardLayout(
    rows: List<List<String>>,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onShift: () -> Unit,
    onSymbols: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit,
    shiftLabel: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            rows[0].forEach { key ->
                KeyButton(
                    label = key,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            rows[1].forEach { key ->
                KeyButton(
                    label = key,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Row 3: shift + letters + backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyButton(
                label = shiftLabel,
                onClick = onShift,
                modifier = Modifier.weight(1.5f),
                isSpecial = true
            )
            rows[2].forEach { key ->
                KeyButton(
                    label = key,
                    onClick = { onKey(key) },
                    modifier = Modifier.weight(1f)
                )
            }
            IconKeyButton(
                icon = Icons.Default.Backspace,
                contentDescription = "Backspace",
                onClick = onBackspace,
                modifier = Modifier.weight(1.5f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Row 4: symbols / space / done
        BottomRow(
            onSymbols = onSymbols,
            symbolLabel = "?123",
            onSpace = onSpace,
            onDone = onDone
        )
    }
}

@Composable
private fun BottomRow(
    onSymbols: () -> Unit,
    symbolLabel: String,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        KeyButton(
            label = symbolLabel,
            onClick = onSymbols,
            modifier = Modifier.weight(1.5f),
            isSpecial = true
        )
        KeyButton(
            label = "space",
            onClick = onSpace,
            modifier = Modifier.weight(5f),
            isSpecial = false,
            isSpace = true
        )
        KeyButton(
            label = "Done",
            onClick = onDone,
            modifier = Modifier.weight(1.5f),
            isSpecial = true
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// KEY BUTTONS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun KeyButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isSpace: Boolean = false
) {
    val backgroundColor = when {
        isSpecial -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        isSpecial -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSpace) {
            Icon(
                Icons.Default.SpaceBar,
                contentDescription = "Space",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label,
                color = textColor,
                fontSize = if (isSpecial) 12.sp else 16.sp,
                fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun IconKeyButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}
