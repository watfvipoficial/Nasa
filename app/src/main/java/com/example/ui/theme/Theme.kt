package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NasaDarkColorScheme = darkColorScheme(
    primary = NasaCyanPrimary,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = NasaCyanLight,
    secondary = NasaAmberSecondary,
    onSecondary = Color(0xFF422C00),
    secondaryContainer = Color(0xFF5F4100),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = NasaLaserRed,
    onTertiary = Color.White,
    background = NasaDeepNavy,
    onBackground = NasaTextPrimary,
    surface = NasaSurfaceDark,
    onSurface = NasaTextPrimary,
    surfaceVariant = Color(0xFF152238),
    onSurfaceVariant = NasaTextSecondary,
    outline = NasaBorderCyan
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NasaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
