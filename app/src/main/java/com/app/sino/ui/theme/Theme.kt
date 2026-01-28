package com.app.sino.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SinoPrimary,
    onPrimary = SinoBlack,
    secondary = SinoPrimaryLight,
    onSecondary = SinoBlack,
    tertiary = SinoSurfaceVariant,
    background = SinoBackground,
    onBackground = SinoTextPrimary,
    surface = SinoSurface,
    onSurface = SinoTextPrimary,
    surfaceVariant = SinoSurfaceVariant,
    onSurfaceVariant = SinoTextSecondary,
    outline = GlassBorder
)

// Minimalist Apple Light Scheme (keeping it just in case, but favoring Dark)
private val LightColorScheme = lightColorScheme(
    primary = SinoPrimaryDark,
    onPrimary = SinoWhite,
    secondary = SinoPrimary,
    onSecondary = SinoWhite,
    tertiary = SinoSurface,
    background = Color(0xFFF2F2F7), // Apple System Gray 6
    onBackground = Color(0xFF1C1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF3A3A3C),
    outline = Color(0xFFD1D1D6)
)

@Composable
fun SINOTheme(
    darkTheme: Boolean = true, // Force dark theme by default for that 'Premium' feel
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}