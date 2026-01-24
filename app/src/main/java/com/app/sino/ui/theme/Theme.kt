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

private val DarkColorScheme = darkColorScheme(
    primary = SinoWhite,
    onPrimary = SinoBlack,
    secondary = SinoMediumGrey,
    onSecondary = SinoWhite,
    tertiary = SinoLightGrey,
    background = SinoBackground,
    onBackground = SinoWhite,
    surface = SinoBlack,
    onSurface = SinoWhite,
    onSurfaceVariant = SinoLightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = SinoBlack,
    onPrimary = SinoWhite,
    secondary = SinoMediumGrey,
    onSecondary = SinoWhite,
    tertiary = SinoLightGrey,
    background = SinoWhite,
    onBackground = SinoBlack,
    surface = SinoWhite,
    onSurface = SinoBlack,
    onSurfaceVariant = SinoMediumGrey
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