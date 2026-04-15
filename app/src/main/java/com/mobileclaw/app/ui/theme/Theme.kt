package com.mobileclaw.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MobileClawColorScheme: ColorScheme = lightColorScheme(
    primary = AtriumPrimary,
    onPrimary = AtriumSurfaceLowest,
    primaryContainer = AtriumPrimaryContainer,
    onPrimaryContainer = AtriumSurfaceLowest,
    secondary = AtriumPrimaryContainer,
    onSecondary = AtriumSurfaceLowest,
    background = AtriumSurface,
    onBackground = AtriumOnSurface,
    surface = AtriumSurface,
    onSurface = AtriumOnSurface,
    surfaceContainerLow = AtriumSurfaceLow,
    surfaceContainerLowest = AtriumSurfaceLowest,
    surfaceContainerHigh = Color(0xFFE5EAF0),
    surfaceContainerHighest = Color(0xFFDDE4EC),
    onSurfaceVariant = AtriumOnSurfaceVariant,
    outlineVariant = AtriumOutlineVariant.copy(alpha = 0.15f),
    error = AtriumError,
    errorContainer = AtriumErrorContainer,
    tertiary = AtriumSuccess,
    tertiaryContainer = AtriumSuccessContainer,
    surfaceTint = AtriumPrimary.copy(alpha = 0.1f),
)

@Composable
fun MobileClawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MobileClawColorScheme,
        typography = MobileClawTypography,
        shapes = MobileClawShapes,
        content = content,
    )
}
