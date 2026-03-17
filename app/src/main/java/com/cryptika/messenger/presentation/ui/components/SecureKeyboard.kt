// presentation/ui/components/SecureKeyboard.kt
package com.cryptika.messenger.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

// Keyboard modes

private enum class KeyboardMode { LETTERS, SYMBOLS, NUMPAD, EMOJI }
private enum class ShiftState  { OFF, SINGLE, LOCKED }

// Public language enum

enum class KeyboardLanguage(val code: String, val nativeName: String) {
    ENGLISH ("EN", "English"),
    GERMAN  ("DE", "Deutsch"),
    SPANISH ("ES", "Español"),
    HINDI   ("HI", "हिन्दी"),
    MARATHI ("MR", "मराठी"),
    SANSKRIT("SA", "संस्कृत"),
    TAMIL   ("TA", "தமிழ்"),
    ARABIC  ("AR", "العربية"),
    KOREAN  ("KO", "한국어"),
    JAPANESE("JA", "日本語"),
    CHINESE ("ZH", "中文")
}

// Per-language layout

private data class LangLayout(
    /** Three rows shown when shift is OFF / primary state. */
    val primaryRows: List<List<String>>,
    /** Three rows shown when shift is ON  / secondary state. */
    val secondaryRows: List<List<String>>,
    val commaKey: String = ",",
    val periodKey: String = ".",
    /** Font scale for complex scripts so characters fit on keys. */
    val fontScale: Float = 1f
)

private val LAYOUTS: Map<KeyboardLanguage, LangLayout> = mapOf(

    KeyboardLanguage.ENGLISH to LangLayout(
        primaryRows = listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("z","x","c","v","b","n","m")
        ),
        secondaryRows = listOf(
            listOf("Q","W","E","R","T","Y","U","I","O","P"),
            listOf("A","S","D","F","G","H","J","K","L"),
            listOf("Z","X","C","V","B","N","M")
        )
    ),

    KeyboardLanguage.GERMAN to LangLayout(
        primaryRows = listOf(
            listOf("q","w","e","r","t","z","u","i","o","p","ü"),
            listOf("a","s","d","f","g","h","j","k","l","ö","ä"),
            listOf("y","x","c","v","b","n","m","ß")
        ),
        secondaryRows = listOf(
            listOf("Q","W","E","R","T","Z","U","I","O","P","Ü"),
            listOf("A","S","D","F","G","H","J","K","L","Ö","Ä"),
            listOf("Y","X","C","V","B","N","M","ẞ")
        ),
        fontScale = 0.90f
    ),

    KeyboardLanguage.SPANISH to LangLayout(
        primaryRows = listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l","ñ"),
            listOf("z","x","c","v","b","n","m")
        ),
        secondaryRows = listOf(
            listOf("Q","W","E","R","T","Y","U","I","O","P"),
            listOf("A","S","D","F","G","H","J","K","L","Ñ"),
            listOf("Z","X","C","V","B","N","M")
        )
    ),

    // Devanagari
    // Primary  = consonant groups (vargas) for easy phonetic typing.
    // Secondary = vowels + vowel signs (matras) + special marks.

    KeyboardLanguage.HINDI to LangLayout(
        primaryRows = listOf(
            listOf("क","ख","ग","घ","ङ","च","छ","ज","झ","ञ"),
            listOf("ट","ठ","ड","ढ","ण","त","थ","द","ध","न"),
            listOf("प","फ","ब","भ","म","य","र","ल","व","ह")
        ),
        secondaryRows = listOf(
            listOf("अ","आ","इ","ई","उ","ऊ","ए","ऐ","ओ","औ"),
            listOf("ा","ि","ी","ु","ू","े","ै","ो","ौ","ं"),
            listOf("्","ः","़","ऋ","ॠ","ॐ","श","ष","स","ऽ")
        ),
        commaKey = ",", periodKey = "।", fontScale = 0.88f
    ),

    KeyboardLanguage.MARATHI to LangLayout(
        primaryRows = listOf(
            listOf("क","ख","ग","घ","ङ","च","छ","ज","झ","ञ"),
            listOf("ट","ठ","ड","ढ","ण","त","थ","द","ध","न"),
            listOf("प","फ","ब","भ","म","य","र","ल","व","ह")
        ),
        secondaryRows = listOf(
            listOf("अ","आ","इ","ई","उ","ऊ","ए","ऐ","ओ","औ"),
            listOf("ा","ि","ी","ु","ू","े","ै","ो","ौ","ं"),
            listOf("्","ः","़","ऋ","ळ","ॐ","श","ष","स","ऽ")
        ),
        commaKey = ",", periodKey = "।", fontScale = 0.88f
    ),

    KeyboardLanguage.SANSKRIT to LangLayout(
        primaryRows = listOf(
            listOf("क","ख","ग","घ","ङ","च","छ","ज","झ","ञ"),
            listOf("ट","ठ","ड","ढ","ण","त","थ","द","ध","न"),
            listOf("प","फ","ब","भ","म","य","र","ल","व","ह")
        ),
        secondaryRows = listOf(
            listOf("अ","आ","इ","ई","उ","ऊ","ए","ऐ","ओ","औ"),
            listOf("ा","ि","ी","ु","ू","े","ै","ो","ौ","ं"),
            listOf("्","ः","ऽ","ऋ","ॠ","ॐ","श","ष","स","॥")
        ),
        commaKey = ",", periodKey = "।", fontScale = 0.88f
    ),

    // Tamil
    // Primary  = consonants.   Secondary = vowels + vowel signs.

    KeyboardLanguage.TAMIL to LangLayout(
        primaryRows = listOf(
            listOf("க","ங","ச","ஞ","ட","ண","த","ந","ப","ம"),
            listOf("ய","ர","ல","வ","ழ","ள","ற","ன","ஜ","ஷ"),
            listOf("ஸ","ஹ","க்ஷ","ஸ்ரீ","ஃ")
        ),
        secondaryRows = listOf(
            listOf("அ","ஆ","இ","ஈ","உ","ஊ","எ","ஏ","ஐ","ஒ"),
            listOf("ஓ","ஔ","ா","ி","ீ","ு","ூ","ெ","ே","ை"),
            listOf("ொ","ோ","ௌ","்","ஂ")
        ),
        commaKey = ",", periodKey = ".", fontScale = 0.82f
    ),

    // Arabic
    // Standard Arabic phonetic rows.  Secondary adds diacritics + extra chars.

    KeyboardLanguage.ARABIC to LangLayout(
        primaryRows = listOf(
            listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح"),
            listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك"),
            listOf("ئ","ء","ؤ","ر","لا","ى","ة","و","ز","ظ")
        ),
        secondaryRows = listOf(
            listOf("~","ْ","ً","ٌ","ٍ","إ","أ","آ","ذ","ط"),
            listOf("ُ","ِ","ّ","ـ","لأ","لإ","لآ","لا","،",";"),
            listOf("؟","«","»","…","÷","×","٪","؛","ي‍","خ‍")
        ),
        commaKey = "،", periodKey = ".", fontScale = 0.86f
    ),

    // Korean (Standard 2-Set Hangul)
    // Primary = basic consonants + vowels.  Secondary = tense consonants.

    KeyboardLanguage.KOREAN to LangLayout(
        primaryRows = listOf(
            listOf("ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ"),
            listOf("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ"),
            listOf("ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ")
        ),
        secondaryRows = listOf(
            listOf("ㅃ","ㅉ","ㄸ","ㄲ","ㅆ","ㅛ","ㅕ","ㅑ","ㅒ","ㅖ"),
            listOf("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ"),
            listOf("ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ")
        ),
        fontScale = 0.88f
    ),

    // Japanese (Hiragana gojuuon grid, 3 rows visible; shift = next 3 rows)

    KeyboardLanguage.JAPANESE to LangLayout(
        primaryRows = listOf(
            listOf("あ","い","う","え","お","か","き","く","け","こ"),
            listOf("さ","し","す","せ","そ","た","ち","つ","て","と"),
            listOf("な","に","ぬ","ね","の","は","ひ","ふ","へ","ほ")
        ),
        secondaryRows = listOf(
            listOf("ま","み","む","め","も","や","ゆ","よ","ら","り"),
            listOf("る","れ","ろ","わ","を","ん","ー","っ","ゃ","ゅ"),
            listOf("ょ","ぁ","ぃ","ぅ","ぇ","ぉ","゛","゜","ヴ","ゐ")
        ),
        commaKey = "、", periodKey = "。", fontScale = 0.86f
    ),

    // Chinese Simplified (200 most-frequent hanzi, two pages of 30 each)

    KeyboardLanguage.CHINESE to LangLayout(
        primaryRows = listOf(
            listOf("的","一","是","不","了","在","人","我","有","他"),
            listOf("这","中","大","来","上","国","个","到","说","们"),
            listOf("地","出","道","也","时","年","得","就","那","要")
        ),
        secondaryRows = listOf(
            listOf("下","以","生","会","自","着","去","之","过","家"),
            listOf("学","对","可","它","如","小","么","行","子","而"),
            listOf("方","法","后","多","然","经","又","与","想","回")
        ),
        commaKey = "，", periodKey = "。", fontScale = 0.90f
    )
)

// Dimensions

private val KEY_HEIGHT    = 50.dp
private val NUM_KEY_H     = 42.dp
private val NUMPAD_KEY_H  = 58.dp
private val KEY_SPACING   = 2.dp
private val KEY_RADIUS    = 6.dp
private val ROW_SPACING   = 3.dp
private val H_PAD         = 2.dp

// PUBLIC COMPOSABLE

@Composable
fun SecureKeyboard(
    visible: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    onToggle: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var mode       by remember { mutableStateOf(KeyboardMode.LETTERS) }
    var shiftState by remember { mutableStateOf(ShiftState.OFF)       }
    var lastShift  by remember { mutableStateOf(0L)                   }
    var language   by remember { mutableStateOf(KeyboardLanguage.ENGLISH) }
    var showPicker by remember { mutableStateOf(false) }

    val tap = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }

    // Language-cycle (short press) / picker (long press)
    val allLangs = KeyboardLanguage.entries
    val cycleLanguage: () -> Unit = {
        tap()
        language = allLangs[(allLangs.indexOf(language) + 1) % allLangs.size]
        shiftState = ShiftState.OFF
    }

    if (showPicker) {
        LanguagePickerDialog(
            current  = language,
            onSelect = { lang -> language = lang; shiftState = ShiftState.OFF; showPicker = false },
            onDismiss = { showPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {

        // Toggle bar
        Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (visible) Icons.Default.KeyboardArrowDown
                                  else         Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = if (visible) "Hide Secure Keyboard" else "Show Secure Keyboard",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Keyboard body
        AnimatedVisibility(
            visible = visible,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Surface(
                tonalElevation   = 6.dp,
                shadowElevation  = 0.dp,
                modifier         = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {

                    when (mode) {

                        // Letter / script keyboard
                        KeyboardMode.LETTERS -> LetterKeys(
                            language   = language,
                            shiftState = shiftState,
                            onChar     = { ch ->
                                tap()
                                onKeyPress(ch)
                                if (shiftState == ShiftState.SINGLE) shiftState = ShiftState.OFF
                            },
                            onShift    = {
                                tap()
                                val now = System.currentTimeMillis()
                                shiftState = when (shiftState) {
                                    ShiftState.OFF    -> { lastShift = now; ShiftState.SINGLE }
                                    ShiftState.SINGLE ->
                                        if (now - lastShift < 400) ShiftState.LOCKED
                                        else { lastShift = now; ShiftState.OFF }
                                    ShiftState.LOCKED -> { lastShift = now; ShiftState.OFF }
                                }
                            },
                            onBackspace     = { tap(); onBackspace() },
                            onSymbols       = { tap(); mode = KeyboardMode.SYMBOLS },
                            onEmoji         = { tap(); mode = KeyboardMode.EMOJI   },
                            onSpace         = { tap(); onKeyPress(" ") },
                            onDone          = { tap(); onDone() },
                            onLangCycle     = cycleLanguage,
                            onLangLongPress = { showPicker = true }
                        )

                        // Symbols keyboard
                        KeyboardMode.SYMBOLS -> SymbolKeys(
                            onKey       = { tap(); onKeyPress(it) },
                            onBackspace = { tap(); onBackspace()  },
                            onAbc       = { tap(); mode = KeyboardMode.LETTERS },
                            onNumpad    = { tap(); mode = KeyboardMode.NUMPAD  },
                            onSpace     = { tap(); onKeyPress(" ") },
                            onDone      = { tap(); onDone() }
                        )

                        // Numpad keyboard
                        KeyboardMode.NUMPAD -> NumpadKeys(
                            onKey       = { tap(); onKeyPress(it) },
                            onBackspace = { tap(); onBackspace()  },
                            onAbc       = { tap(); mode = KeyboardMode.LETTERS },
                            onSymbols   = { tap(); mode = KeyboardMode.SYMBOLS },
                            onEmoji     = { tap(); mode = KeyboardMode.EMOJI   },
                            onSpace     = { tap(); onKeyPress(" ") },
                            onDone      = { tap(); onDone() }
                        )

                        // Emoji keyboard
                        KeyboardMode.EMOJI -> EmojiKeys(
                            onEmoji     = { tap(); onKeyPress(it) },
                            onBackspace = { tap(); onBackspace()  },
                            onAbc       = { tap(); mode = KeyboardMode.LETTERS },
                            onSpace     = { tap(); onKeyPress(" ") },
                            onDone      = { tap(); onDone() }
                        )
                    }
                }
            }
        }
    }
}

// LANGUAGE PICKER DIALOG

@Composable
private fun LanguagePickerDialog(
    current: KeyboardLanguage,
    onSelect: (KeyboardLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape          = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text     = "Select Language",
                    style    = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                KeyboardLanguage.entries.forEach { lang ->
                    val selected = lang == current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { onSelect(lang) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(lang.nativeName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text  = lang.code,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (lang != KeyboardLanguage.entries.last()) {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Cancel") }
            }
        }
    }
}

// LETTER / SCRIPT KEYS: works for every language

@Composable
private fun LetterKeys(
    language: KeyboardLanguage,
    shiftState: ShiftState,
    onChar: (String) -> Unit,
    onShift: () -> Unit,
    onBackspace: () -> Unit,
    onSymbols: () -> Unit,
    onEmoji: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit,
    onLangCycle: () -> Unit,
    onLangLongPress: () -> Unit
) {
    val layout    = LAYOUTS[language] ?: LAYOUTS[KeyboardLanguage.ENGLISH]!!
    val isShifted = shiftState != ShiftState.OFF
    val rows      = if (isShifted) layout.secondaryRows else layout.primaryRows
    val scale     = layout.fontScale
    val isLatin   = language in listOf(
        KeyboardLanguage.ENGLISH, KeyboardLanguage.GERMAN, KeyboardLanguage.SPANISH
    )
    // English follows the standard QWERTY layout: no top numbers row,
    // and row 3 keys span full width without centering spacers.
    val isEnglish = language == KeyboardLanguage.ENGLISH

    Column(
        modifier             = Modifier.fillMaxWidth(),
        verticalArrangement  = Arrangement.spacedBy(ROW_SPACING)
    ) {
        // Numbers row (Latin only, not English: standard QWERTY omits it)
        if (isLatin && !isEnglish) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
                horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
            ) {
                listOf("1","2","3","4","5","6","7","8","9","0").forEach { n ->
                    CharKey(n, { onChar(n) }, Modifier.weight(1f), height = NUM_KEY_H)
                }
            }
        }

        // Row 1
        rows.getOrNull(0)?.filter { it.isNotEmpty() }?.let { row ->
            ScriptRow(row, onChar, scale)
        }

        // Row 2 (centered when < 10 keys)
        rows.getOrNull(1)?.filter { it.isNotEmpty() }?.let { row ->
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
                horizontalArrangement = Arrangement.spacedBy(KEY_SPACING),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (row.size < 10) Spacer(Modifier.weight((10f - row.size) / 2f))
                row.forEach { ch -> CharKey(ch, { onChar(ch) }, Modifier.weight(1f), fontScale = scale) }
                if (row.size < 10) Spacer(Modifier.weight((10f - row.size) / 2f))
            }
        }

        // Row 3: Shift + chars + Backspace
        rows.getOrNull(2)?.filter { it.isNotEmpty() }?.let { row ->
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
                horizontalArrangement = Arrangement.spacedBy(KEY_SPACING),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                ShiftKey(state = shiftState, onClick = onShift, modifier = Modifier.weight(1.5f))
                // English: no spacers, row 3 keys fill the full width between
                // Shift and Backspace (same key width as rows 1 & 2, like Gboard).
                // Other layouts centre their shorter row 3 with half-spacers.
                val pad = if (isEnglish) 0f else (9f - row.size.coerceAtMost(9)) / 2f
                if (pad > 0f) Spacer(Modifier.weight(pad))
                row.take(9).forEach { ch ->
                    CharKey(ch, { onChar(ch) }, Modifier.weight(1f), fontScale = scale)
                }
                if (pad > 0f) Spacer(Modifier.weight(pad))
                BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1.5f))
            }
        }

        // Bottom row: ?123 | , | language | space | . | Done
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey("?123",  onSymbols, Modifier.weight(1.3f))
            CharKey(layout.commaKey, { onChar(layout.commaKey) }, Modifier.weight(0.7f))
            LangKey(
                language    = language,
                onPress     = onLangCycle,
                onLongPress = onLangLongPress,
                modifier    = Modifier.weight(0.9f)
            )
            SpaceKey(
                onClick  = onSpace,
                label    = language.nativeName,
                modifier = Modifier.weight(3.2f)
            )
            CharKey(layout.periodKey, { onChar(layout.periodKey) }, Modifier.weight(0.7f))
            DoneKey(onDone, Modifier.weight(1.1f))
        }
    }
}

/** A full-width row of equally-weighted character keys. */
@Composable
private fun ScriptRow(
    keys: List<String>,
    onChar: (String) -> Unit,
    fontScale: Float
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
        horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
    ) {
        keys.forEach { ch ->
            CharKey(ch, { onChar(ch) }, Modifier.weight(1f), fontScale = fontScale)
        }
    }
}

// SYMBOL KEYBOARD: two pages, bottom row leads to NUMPAD via "1234" button

@Composable
private fun SymbolKeys(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onAbc: () -> Unit,
    onNumpad: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    var page by remember { mutableStateOf(1) }

    val (row1, row2, row3) = if (page == 1) Triple(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/"),
        listOf("*", "\"", "'", ":", ";", "!", "?")
    ) else Triple(
        listOf("~", "`", "|", "\\", "{", "}", "<", ">", "^", "%"),
        listOf("€", "£", "¥", "•", "°", "=", "©", "®", "™", "…"),
        listOf("¶", "§", "«", "»", "¿", "¡")
    )

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            row1.forEach { s -> CharKey(s, { onKey(s) }, Modifier.weight(1f)) }
        }
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            row2.forEach { s -> CharKey(s, { onKey(s) }, Modifier.weight(1f)) }
        }
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey(if (page == 1) "½" else "1/2", { page = if (page == 1) 2 else 1 }, Modifier.weight(1.5f))
            row3.forEach { s -> CharKey(s, { onKey(s) }, Modifier.weight(1f)) }
            val symPad = (7f - row3.size).coerceAtLeast(0f)
            if (symPad > 0f) Spacer(Modifier.weight(symPad))
            BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1.5f))
        }
        // Bottom: ABC | , | 1234(→NUMPAD) | space | . | Done
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey("ABC",  onAbc, Modifier.weight(1.3f))
            CharKey(",", { onKey(",") }, Modifier.weight(0.7f))
            LabelKey("1234", onNumpad, Modifier.weight(0.9f))
            SpaceKey(onSpace, modifier = Modifier.weight(3.2f))
            CharKey(".", { onKey(".") }, Modifier.weight(0.7f))
            DoneKey(onDone, Modifier.weight(1.1f))
        }
    }
}

// NUMPAD: phone-dial style number pad, accessible from Symbols via "1234"

@Composable
private fun NumpadKeys(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onAbc: () -> Unit,
    onSymbols: () -> Unit,
    onEmoji: () -> Unit,
    onSpace: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
    ) {
        // 3×3 digit block
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
                horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
            ) {
                row.forEach { n ->
                    CharKey(n, { onKey(n) }, Modifier.weight(1f), height = NUMPAD_KEY_H)
                }
            }
        }
        // Last digit row: . / 0 / backspace
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            CharKey(".", { onKey(".") }, Modifier.weight(1f), height = NUMPAD_KEY_H)
            CharKey("0", { onKey("0") }, Modifier.weight(1f), height = NUMPAD_KEY_H)
            BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1f))
        }
        // Bottom: ABC | !@# | 😀 | space | Done
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey("ABC", onAbc,    Modifier.weight(1.3f))
            LabelKey("!@#", onSymbols, Modifier.weight(1.0f))
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
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            SpaceKey(onSpace, modifier = Modifier.weight(2.8f))
            DoneKey(onDone, Modifier.weight(1.1f))
        }
    }
}

// EMOJI KEYBOARD

private val emojiData = listOf(
    "😀" to listOf(
        "😀","😃","😄","😁","😆","😅","🤣","😂","🙂","🙃",
        "😉","😊","😇","🥰","😍","🤩","😘","😗","😚","😋",
        "😛","😜","🤪","😝","🤑","🤗","🤭","🤫","🤔","🤐",
        "🤨","😐","😑","😶","😏","😒","🙄","😬","🤥","😌",
        "😔","😪","🤤","😴","😷","🤒","🤕","🤢","🤮","🥵",
        "🥶","🥴","😵","🤯","😎","🥳","😭","😤","😡","🥺"
    ),
    "👋" to listOf(
        "👍","👎","👊","✊","🤛","🤜","👏","🙌","👐","🤲",
        "🤝","🙏","✌️","🤞","🤟","🤘","👌","🤌","👈","👉",
        "👆","👇","☝️","✋","🤚","🖐️","🖖","👋","🤙","💪",
        "🦾","🦿","🦵","🦶","👂","🦻","👃","🧠","👀","👅",
        "👄","💋","🧑","👶","🧒","👦","👧","🧔","👱","👮",
        "🧑‍💻","🧑‍🔬","🧑‍🎨","🧑‍🚀","🧑‍⚕️","🧑‍🍳","🧑‍🏫","🧑‍🌾","🧑‍🔧","🧑‍🎤"
    ),
    "❤️" to listOf(
        "❤️","🧡","💛","💚","💙","💜","🤎","🖤","🤍","💔",
        "❣️","💕","💞","💓","💗","💖","💘","💝","💟","♥️",
        "💯","💢","💥","💫","💦","💨","🕳️","💣","💬","💤",
        "🔥","✨","⭐","🌟","💫","⚡","☄️","💎","🏆","🎯",
        "🎪","🎭","🎨","🎬","🎤","🎧","🎵","🎶","🎹","🎸",
        "🎺","🥁","🎻","🪕","🎮","🕹️","🎲","♟️","🎰","🃏"
    ),
    "🔒" to listOf(
        "🔒","🔓","🔑","🗝️","🛡️","📱","💻","⌨️","🖥️","🖨️",
        "📷","📸","📹","🎥","📞","☎️","📟","📠","📺","📻",
        "⏰","⏱️","⏲️","🕰️","⌚","📅","📆","📌","📍","📎",
        "✏️","📝","📁","📂","📊","📈","📉","🗑️","📦","🎁",
        "🔍","🔎","🔬","🔭","⚗️","🧪","🧫","🧬","💊","💉",
        "🩹","🩺","🩻","🧲","🔋","🔌","💡","🔦","🕯️","🪔"
    ),
    "🌿" to listOf(
        "🌞","🌝","🌛","🌜","⭐","🌟","🌈","☀️","🌤️","⛅",
        "🌧️","⛈️","🌩️","❄️","🌊","🌸","🌺","🌻","🌼","🌷",
        "🌹","🌵","🌴","🍀","🍁","🍂","🍃","🐶","🐱","🐭",
        "🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🦋","🐛",
        "🐝","🐌","🐞","🐜","🦗","🦟","🦂","🐢","🦎","🐍"
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
        // Category tabs
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
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
                    Text(icon, fontSize = 18.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        // Emoji grid
        LazyVerticalGrid(
            columns     = GridCells.Fixed(8),
            modifier    = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement   = Arrangement.spacedBy(2.dp)
        ) {
            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEmoji(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        // Bottom: ABC | space | ⌫ | Done
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = H_PAD),
            horizontalArrangement = Arrangement.spacedBy(KEY_SPACING)
        ) {
            LabelKey("ABC", onAbc, Modifier.weight(1.2f))
            SpaceKey(onSpace, modifier = Modifier.weight(4f))
            BackspaceKey(onDelete = onBackspace, modifier = Modifier.weight(1.2f))
            DoneKey(onDone, Modifier.weight(1.2f))
        }
    }
}

// KEY COMPOSABLES

/** Standard character key (letter, digit, symbol). */
@Composable
private fun CharKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT,
    fontScale: Float = 1f
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val baseSize = if (height < KEY_HEIGHT) 13 else 16
        Text(
            text      = label,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize  = (baseSize * fontScale).sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines  = 1
        )
    }
}

/** Functional key with text label (ABC, ?123, ½, etc.). */
@Composable
private fun LabelKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = KEY_HEIGHT
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = label,
            color     = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize  = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines  = 1
        )
    }
}

/** Language key: shows language code; tap = cycle, long-press = picker. */
@Composable
private fun LangKey(
    language: KeyboardLanguage,
    onPress: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { onPress() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Language,
                contentDescription = "Language",
                tint     = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text      = language.code,
                color     = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize  = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Shift key: three visual states (off / single / caps-lock). */
@Composable
private fun ShiftKey(
    state: ShiftState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when (state) {
        ShiftState.OFF    -> MaterialTheme.colorScheme.secondaryContainer
        ShiftState.SINGLE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ShiftState.LOCKED -> MaterialTheme.colorScheme.primary
    }
    val fg = when (state) {
        ShiftState.OFF    -> MaterialTheme.colorScheme.onSecondaryContainer
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
            text      = if (state == ShiftState.LOCKED) "⇪" else "⇧",
            color     = fg,
            fontSize  = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Backspace with repeat-on-hold (400 ms then 40 ms repeat). */
@Composable
private fun BackspaceKey(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(400L)
            while (pressed) { onDelete(); delay(40L) }
        }
    }

    Box(
        modifier = modifier
            .height(KEY_HEIGHT)
            .clip(RoundedCornerShape(KEY_RADIUS))
            .background(
                if (pressed) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else         MaterialTheme.colorScheme.secondaryContainer
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
            tint     = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Space bar: optionally shows a language name hint. */
@Composable
private fun SpaceKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "space"
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
            text     = label,
            color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

/** Done / Enter key, highlighted in primary colour. */
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
            tint     = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}
