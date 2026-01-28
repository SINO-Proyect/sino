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
    onPrimary = SinoWhite,
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

private val LightColorScheme = lightColorScheme(
    primary = SinoPrimaryDark,
    onPrimary = SinoWhite,
    secondary = SinoPrimary,
    onSecondary = SinoWhite,
    tertiary = SinoSurface,
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFE5E7EB)
)

@Composable
fun SINOTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for strictly B&W
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor check removed to force our theme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}