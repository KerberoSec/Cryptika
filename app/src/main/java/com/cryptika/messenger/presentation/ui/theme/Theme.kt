// presentation/ui/theme/Theme.kt
package com.cryptika.messenger.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// COLOR PALETTE: Deep Obsidian & Bold Stealth Cobalt / Glacier Cyan
// High contrast, professional security application theme
val PitchBlack = Color(0xFF07090E)
val DeepObsidian = Color(0xFF0E131F)
val ElevatedObsidian = Color(0xFF161E2E)
val BorderSubtle = Color(0xFF26334D)

val BoldElectricCyan = Color(0xFF00E5FF)
val BoldElectricCobalt = Color(0xFF2563EB)
val CyanContainer = Color(0xFF0B253A)
val OnCyanContainer = Color(0xFFBAE6FD)

val SecureGreen = Color(0xFF10B981)
val SecureGreenDark = Color(0xFF059669)
val SecureGreenContainer = Color(0xFF064E3B)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFF43F5E)
val RelayYellow = Color(0xFFFACC15)
val P2PGreen = Color(0xFF10B981)
val DisconnectedGray = Color(0xFF64748B)

private val DarkColorScheme = darkColorScheme(
    primary = BoldElectricCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = CyanContainer,
    onPrimaryContainer = OnCyanContainer,
    secondary = BoldElectricCobalt,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF93C5FD),
    background = PitchBlack,
    onBackground = Color(0xFFF8FAFC),
    surface = DeepObsidian,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = ElevatedObsidian,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = DangerRed,
    onError = Color.White,
    errorContainer = Color(0xFF4C0519),
    onErrorContainer = Color(0xFFFFD1DC),
    outline = BorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    background = Color(0xFF0A0D14),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111726),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1B2337),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = DangerRed,
    onError = Color.White,
    outline = Color(0xFF334155)
)

@Composable
fun CryptikaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            bodyMedium = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            bodySmall = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp
            ),
            titleLarge = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            labelSmall = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        ),
        content = content
    )
}
