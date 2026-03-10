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

// ══════════════════════════════════════════════════════════════════════════════
// COLOR PALETTE — Security-focused, clean dark/light
// ══════════════════════════════════════════════════════════════════════════════
val SecureGreen = Color(0xFF00C853)
val SecureGreenDark = Color(0xFF00E676)
val SecureGreenContainer = Color(0xFF003916)
val WarningAmber = Color(0xFFFFB300)
val DangerRed = Color(0xFFD50000)
val RelayYellow = Color(0xFFFDD835)
val P2PGreen = Color(0xFF43A047)
val DisconnectedGray = Color(0xFF757575)

private val DarkColorScheme = darkColorScheme(
    primary = SecureGreenDark,
    onPrimary = Color.Black,
    primaryContainer = SecureGreenContainer,
    onPrimaryContainer = SecureGreenDark,
    secondary = Color(0xFF90CAF9),
    onSecondary = Color.Black,
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    outline = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00695C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00352D),
    secondary = Color(0xFF1976D2),
    onSecondary = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    error = Color(0xFFB00020),
    outline = Color(0xFFBDBDBD)
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
