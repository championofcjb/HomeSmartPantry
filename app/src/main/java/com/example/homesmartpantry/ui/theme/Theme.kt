package com.example.homesmartpantry.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = GreenPrimaryLight,
    secondary = OrangeAccent,
    onSecondary = TextOnPrimary,
    secondaryContainer = OrangeAccentLight,
    tertiary = ExpiringOrange,
    background = WarmWhite,
    onBackground = TextPrimary,
    surface = WarmWhite,
    onSurface = TextPrimary,
    surfaceVariant = WarmWhiteDark,
    onSurfaceVariant = TextSecondary,
    error = ExpiredRed,
    onError = TextOnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = TextPrimary,
    primaryContainer = GreenPrimary,
    secondary = DarkOrangeAccent,
    onSecondary = TextPrimary,
    secondaryContainer = OrangeAccent,
    tertiary = ExpiringOrange,
    background = DarkBackground,
    onBackground = TextOnPrimary,
    surface = DarkSurface,
    onSurface = TextOnPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextSecondary,
    error = ExpiredRed,
    onError = TextOnPrimary
)

@Composable
fun HomeSmartPantryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
