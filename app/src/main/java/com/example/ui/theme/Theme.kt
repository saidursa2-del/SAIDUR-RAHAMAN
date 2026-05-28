package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremiumDarkColorScheme = darkColorScheme(
    primary = ImmersiveEmerald,
    onPrimary = ImmersiveBg,
    primaryContainer = ImmersiveCardBg,
    onPrimaryContainer = ImmersiveTextPrimary,
    secondary = ImmersiveEmeraldLight,
    onSecondary = ImmersiveBg,
    background = ImmersiveBg,
    surface = ImmersiveCardBg,
    onBackground = ImmersiveTextPrimary,
    onSurface = ImmersiveTextPrimary,
    error = ImmersiveRed,
    onError = Color.White
)

private val PremiumLightColorScheme = PremiumDarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce our highly customized brand scheme to avoid generic Android dynamic colors
    val colors = if (darkTheme) {
        PremiumDarkColorScheme
    } else {
        PremiumLightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
