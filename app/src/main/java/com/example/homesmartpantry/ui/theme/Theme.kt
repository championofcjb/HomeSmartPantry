package com.example.homesmartpantry.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = BluePrimary.copy(alpha = 0.12f),
    onPrimaryContainer = BluePrimaryDark,
    secondary = GreenSecondary,
    secondaryContainer = GreenSecondary.copy(alpha = 0.12f),
    tertiary = YellowWarning,
    tertiaryContainer = YellowWarning.copy(alpha = 0.12f),
    background = WhiteBackground,
    onBackground = TextPrimary,
    surface = WhiteBackground,
    onSurface = TextPrimary,
    surfaceVariant = GrayDivider.copy(alpha = 0.3f),
    onSurfaceVariant = TextSecondary,
    error = RedError,
    onError = TextOnPrimary,
    errorContainer = RedError.copy(alpha = 0.12f),
    outline = GrayDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = DarkBackground,
    primaryContainer = BluePrimary.copy(alpha = 0.2f),
    onPrimaryContainer = DarkBluePrimary,
    secondary = GreenSecondary,
    secondaryContainer = GreenSecondary.copy(alpha = 0.15f),
    tertiary = YellowWarning,
    tertiaryContainer = YellowWarning.copy(alpha = 0.15f),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    error = RedError,
    onError = TextOnPrimary,
    errorContainer = RedError.copy(alpha = 0.2f),
    outline = GrayDivider.copy(alpha = 0.2f)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
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
        shapes = AppShapes,
        content = content
    )
}
