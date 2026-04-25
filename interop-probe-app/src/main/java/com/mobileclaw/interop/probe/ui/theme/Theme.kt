package com.mobileclaw.interop.probe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ProbeBlue,
    onPrimary = ProbeBlueLight,
    secondary = ProbeTeal,
    tertiary = ProbeAmber,
)

private val DarkColors = darkColorScheme(
    primary = ProbeBlueLight,
    secondary = ProbeTeal,
    tertiary = ProbeSand,
)

@Composable
fun ProbeTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = ProbeTypography,
        content = content,
    )
}
