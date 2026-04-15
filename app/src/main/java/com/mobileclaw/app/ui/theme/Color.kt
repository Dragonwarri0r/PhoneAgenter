package com.mobileclaw.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val AtriumSurface = Color(0xFFF5F7F9)
val AtriumSurfaceLow = Color(0xFFEEF1F3)
val AtriumSurfaceLowest = Color(0xFFFFFFFF)
val AtriumPrimary = Color(0xFF0057BD)
val AtriumPrimaryContainer = Color(0xFF6E9FFF)
val AtriumOnSurface = Color(0xFF2C2F31)
val AtriumOnSurfaceVariant = Color(0xFF5C6166)
val AtriumOutlineVariant = Color(0xFFABADAF)
val AtriumError = Color(0xFFB9384E)
val AtriumErrorContainer = Color(0xFFF5D8DE)
val AtriumSuccess = Color(0xFF0E7A5F)
val AtriumSuccessContainer = Color(0xFFD8F2EA)
val AtriumGlass = Color(0xCCFFFFFF)

fun atriumPrimaryBrush(): Brush = Brush.linearGradient(
    colors = listOf(AtriumPrimary, AtriumPrimaryContainer),
)
