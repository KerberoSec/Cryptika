// presentation/ui/components/SecureKeyboard.kt
package com.cryptika.messenger.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Mode & Shift State ─────────────────────────────────────────────────────

private enum class KeyboardMode { LETTERS, SYMBOLS_1, SYMBOLS_2, EMOJI }
private enum class ShiftState { OFF, SINGLE, LOCKED }

// ─── Dimensions ──────────────────────────────────────────────────────────────

private val KEY_HEIGHT = 46.dp
private val NUM_KEY_HEIGHT = 38.dp
private val KEY_SPACING = 4.dp
private val KEY_RADIUS = 6.dp
private val ROW_SPACING = 4.dp

// ═════════════════════════════════════════════════════════════════════════════
// PUBLIC COMPOSABLE
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun SecureKeyboard(
    visible: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var mode by remember { mutableStateOf(KeyboardMode.LETTERS) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var lastShiftTap by remember { mutableStateOf(0L) }

    val tap: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // ── Toggle bar ───────────────────────────────────────────────────────
        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (visible) Icons.Default.KeyboardArrowDown
                    else Icons.Default.KeyboardArrowUp,
                    contentDescription = if (visible) "Hide keyboard" else "Show keyboard",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (visible) "Hide Secure Keyboard" else "Show Secure Keyboard",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ── Keyboard body ────────────────────────────────────────────────────
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
                        .padding(horizontal = 0.dp, vertical = 4.dp)
                        .imePadding()
                ) {
                    when (mode) {
                        KeyboardMode.LETTERS -> LetterKeys(
                            shiftState = shiftState,
                            onChar = { ch ->
                                tap()
                                val isLetter = ch.length == 1 && ch[0].isLetter()
                                if (isLetter) {
                                    when (shiftState) {
                                        ShiftState.OFF -> onKeyPress(ch.lowercase())
                                        ShiftState.SINGLE -> {
                                            onKeyPress(ch.uppercase())
                                            shiftState = ShiftState.OFF
                                        }
                                        ShiftState.LOCKED -> onKeyPress(ch.uppercase())
                                    }
                                } else {
                                    onKeyPress(ch)
                                }
                            },
                            onShift = {
                                tap()
                                val now = System.currentTimeMillis()
                                shiftState = when (shiftState) {
                                    ShiftState.OFF -> {
                                        lastShiftTap = now
                                        ShiftState.SINGLE
                                    }
                                    ShiftState.SINGLE -> {
                                        if (now - lastShiftTap < 400) ShiftState.LOCKED
                                        else { lastShiftTap = now; ShiftState.OFF }
                                    }
                                    ShiftState.LOCKED -> {
                                        lastShiftTap = now
                                        ShiftState.OFF
                                    }
                                }
                            },
                            onBackspace = { tap(); onBackspace() },
                            onSymbols = { tap(); mode = KeyboardMode.SYMBOLS_1 },
                            onEmoji = { tap(); mode = KeyboardMode.EMOJI },
                            onSpace = { tap(); onKeyPress(" ") },
                            onDone = { tap(); onDone() }
                        )

                        KeyboardMode.SYMBOLS_1 -> SymbolKeys(
                            page = 1,
                            onKey = { tap(); onKeyPress(it) },
                            onBackspace = { tap(); onBackspace() },
                            onAbc = { tap(); mode = KeyboardMode.LETTERS },
                            onPage = { tap(); mode = KeyboardMode.SYMBOLS_2 },
                            onEmoji = { tap(); mode = KeyboardMode.EMOJI },
                            onSpace = { tap(); onKeyPress(" ") },
                            onDone = { tap(); onDone() }
                        )

                        KeyboardMode.SYMBOLS_2 -> SymbolKeys(
                            page = 2,
                            onKey = { tap(); onKeyPress(it) },
                            onBackspace = { tap(); onBackspace() },
                            onAbc = { tap(); mode = KeyboardMode.LETTERS },
                            onPage = { tap(); mode = KeyboardMode.SYMBOLS_1 },
                            onEmoji = { tap(); mode = KeyboardMode.EMOJI },
                            onSpace = { tap(); onKeyPress(" ") },
                            onDone = { tap(); onDone() }
                        )

                        KeyboardMode.EMOJI -> EmojiKeys(
                            onEmoji = { tap(); onKeyPress(it) },
                            onBackspace = { tap(); onBackspace() },
                            onAbc = { tap(); mode = KeyboardMode.LETTERS },
                            onSpace = { tap(); onKeyPress(" ") },
                            onDone = { tap(); onDone() }
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// LETTER KEYBOARD  (number row + QWERTY + shift/caps-lock)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun LetterKeys(
    shiftState: ShiftState,
    onChar: (String) -> Unit,
    onShift: () -> Unit,
    onBackspace: () -> Unit,
    onSymbols: () -> Unit,
    onEmoji: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    val upper = shiftState != ShiftState.OFF
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
    ) {
        // ── Number row ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { n ->
                CharKey(n, onClick = { onChar(n) }, modifier = Modifier.weight(1f), height = NUM_KEY_HEIGHT)
            }
        }

        // ── QWERTY row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            row1.forEach { ch ->
                val display = if (upper) ch.uppercase() else ch
                CharKey(display, onClick = { onChar(ch) }, modifier = Modifier.weight(1f))
            }
        }

        // ── ASDF row (centered) ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            row2.forEach { ch ->
                val display = if (upper) ch.uppercase() else ch
                CharKey(display, onClick = { onChar(ch) }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // ── Shift + ZXCV + Backspace ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            ShiftKey(state = shiftState, onClick = onShift, modifier = Modifier.weight(1.5f))
            row3.forEach { ch ->
                val display = if (upper) ch.uppercase() else ch
                CharKey(display, onClick = { onChar(ch) }, modifier = Modifier.weight(1f))
            }
            BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1.5f))
        }

        // ── Bottom: ?123 | , | emoji | space | . | Done ─────────────────────
        BottomRow(
            leftLabel = "?123",
            onLeft = onSymbols,
            onComma = { onChar(",") },
            onEmoji = onEmoji,
            onSpace = onSpace,
            onPeriod = { onChar(".") },
            onDone = onDone
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SYMBOL KEYBOARD  (two pages, common symbols on page 1)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun SymbolKeys(
    page: Int,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onAbc: () -> Unit,
    onPage: () -> Unit,
    onEmoji: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    val rows = if (page == 1) Triple(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")"),
        listOf("*", "\"", "'", ":", ";", "!", "?")
    ) else Triple(
        listOf("~", "`", "|", "\\", "{", "}", "<", ">", "^", "%"),
        listOf("€", "£", "¥", "•", "°", "=", "/", "©", "®"),
        listOf("™", "¶", "§", "…", "«", "»", "¿")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            rows.first.forEach { s ->
                CharKey(s, onClick = { onKey(s) }, modifier = Modifier.weight(1f))
            }
        }

        // Row 2 (centered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            rows.second.forEach { s ->
                CharKey(s, onClick = { onKey(s) }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3: page toggle + symbols + backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey(
                label = if (page == 1) "½" else "1/2",
                onClick = onPage,
                modifier = Modifier.weight(1.5f)
            )
            rows.third.forEach { s ->
                CharKey(s, onClick = { onKey(s) }, modifier = Modifier.weight(1f))
            }
            BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1.5f))
        }

        // Bottom row
        BottomRow(
            leftLabel = "ABC",
            onLeft = onAbc,
            onComma = { onKey(",") },
            onEmoji = onEmoji,
            onSpace = onSpace,
            onPeriod = { onKey(".") },
            onDone = onDone
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// EMOJI KEYBOARD  (icon tabs + grid + full bottom bar)
// ═════════════════════════════════════════════════════════════════════════════

private val emojiData = listOf(
    "😀" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
        "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😋",
        "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐",
        "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥", "😌",
        "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🥵",
        "🥶", "🥴", "😵", "🤯", "😎", "🥳", "😭", "😤", "😡", "🥺"
    ),
    "👋" to listOf(
        "👍", "👎", "👊", "✊", "🤛", "🤜", "👏", "🙌", "👐", "🤲",
        "🤝", "🙏", "✌️", "🤞", "🤟", "🤘", "👌", "🤌", "👈", "👉",
        "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "💪",
        "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🫀", "🫁",
        "🦷", "🦴", "👀", "👁️", "👅", "👄", "💋", "🧑", "👶", "🧒"
    ),
    "❤️" to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🤎", "🖤", "🤍", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "♥️",
        "💯", "💢", "💥", "💫", "💦", "💨", "🕳️", "💣", "💬", "💤",
        "🔥", "✨", "⭐", "🌟", "💫", "⚡", "☄️", "💎", "🏆", "🎯",
        "🎪", "🎭", "🎨", "🎬", "🎤", "🎧", "🎵", "🎶", "🎹", "🎸"
    ),
    "🔒" to listOf(
        "🔒", "🔓", "🔑", "🗝️", "🛡️", "📱", "💻", "⌨️", "🖥️", "🖨️",
        "📷", "📸", "📹", "🎥", "📞", "☎️", "📟", "📠", "📺", "📻",
        "🎵", "🎶", "🎤", "🎧", "📢", "📣", "📯", "🔔", "🔕", "🎼",
        "⏰", "⏱️", "⏲️", "🕰️", "⌚", "📅", "📆", "📌", "📍", "📎",
        "✏️", "📝", "📁", "📂", "📊", "📈", "📉", "🗑️", "📦", "🎁"
    ),
    "🌿" to listOf(
        "🌞", "🌝", "🌛", "🌜", "⭐", "🌟", "🌈", "☀️", "🌤️", "⛅",
        "🌧️", "⛈️", "🌩️", "❄️", "🌊", "🌸", "🌺", "🌻", "🌼", "🌷",
        "🌹", "🌵", "🌴", "🍀", "🍁", "🍂", "🍃", "🐶", "🐱", "🐭",
        "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷",
        "🐸", "🐵", "🐔", "🐧", "🐦", "🦅", "🦋", "🐛", "🐌", "🐝"
    )
)

@Composable
private fun EmojiKeys(
    onEmoji: (String) -> Unit,
    onBackspace: () -> Unit,
    onAbc: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    val icons = remember { emojiData.map { it.first } }
    var selectedIndex by remember { mutableStateOf(0) }
    val emojis = remember(selectedIndex) { emojiData[selectedIndex].second }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Category tabs (emoji icons) ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            icons.forEachIndexed { idx, icon ->
                val selected = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.secondaryContainer
                            else Color.Transparent
                        )
                        .clickable { selectedIndex = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Emoji grid ───────────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEmoji(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Bottom bar: ABC | space | backspace | Done ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey(label = "ABC", onClick = onAbc, modifier = Modifier.weight(1.2f))
            SpaceKey(onClick = onSpace, modifier = Modifier.weight(4f))
            BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1.2f))
            DoneKey(onClick = onDone, modifier = Modifier.weight(1.2f))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// SHARED BOTTOM ROW
// ═════════════════════════════════════════════════════════════════════════════

@Composable
private fun BottomRow(
    leftLabel: String,
    onLeft: () -> Unit,
    onComma: () -> Unit,
    onEmoji: () -> Unit,
    onSpace: () -> Unit,
    onPeriod: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
    ) {
        LabelKey(label = leftLabel, onClick = onLeft, modifier = Modifier.weight(1.2f))
        CharKey(",", onClick = onComma, modifier = Modifier.weight(0.7f))
        Box(
            modifier = Modifier
                .weight(0.8f)
                .height(KEY_HEIGHT)
                .clip(RoundedCornerShape(KEY_RADIUS))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onEmoji),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.EmojiEmotions,
                contentDescription = "Emoji",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        SpaceKey(onClick = onSpace, modifier = Modifier.weight(3.5f))
        CharKey(".", onClick = onPeriod, modifier = Modifier.weight(0.7f))
        DoneKey(onClick = onDone, modifier = Modifier.weight(1.1f))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// KEY COMPOSABLES
// ═════════════════════════════════════════════════════════════════════════════

/** Regular character key (letter, number, symbol). */
@Composable
private fun CharKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = if (height < KEY_HEIGHT) 14.sp else 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/** Special key with text label (ABC, ?123, ½, etc.). */
@Composable
private fun LabelKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/** Shift key with three visual states: off → single → caps-lock. */
@Composable
private fun ShiftKey(
    state: ShiftState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when (state) {
        ShiftState.OFF -> MaterialTheme.colorScheme.secondaryContainer
        ShiftState.SINGLE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ShiftState.LOCKED -> MaterialTheme.colorScheme.primary
    }
    val fg = when (state) {
        ShiftState.OFF -> MaterialTheme.colorScheme.onSecondaryContainer
        ShiftState.SINGLE -> MaterialTheme.colorScheme.primary
        ShiftState.LOCKED -> MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (state == ShiftState.LOCKED) "⇪" else "⇧",
            color = fg,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Backspace key with repeat-on-hold (hold > 400 ms starts repeating). */
@Composable
private fun BackspaceKey(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(400L)
            while (pressed) {
                onDelete()
                delay(40L)
            }
        }
    }

    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(
                if (pressed) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onDelete()
                    pressed = true
                    waitForUpOrCancellation()
                    pressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Space bar showing "space" label. */
@Composable
private fun SpaceKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "space",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 13.sp,
            letterSpacing = 1.sp
        )
    }
}

/** Done / Enter key – highlighted in primary color. */
@Composable
private fun DoneKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.KeyboardReturn,
            contentDescription = "Done",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}
