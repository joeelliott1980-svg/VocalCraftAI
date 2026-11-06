package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StudioColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004E58),
    onPrimaryContainer = Color(0xFF99F5FF),
    secondary = VioletAccent,
    onSecondary = Color(0xFF380D75),
    secondaryContainer = Color(0xFF50199B),
    onSecondaryContainer = Color(0xFFEEDBFF),
    tertiary = MagentaVocal,
    onTertiary = Color.White,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioCardBorder,
    error = Color(0xFFFF5449),
    onError = Color.White
)

@Composable
fun VocalCraftTheme(
    darkTheme: Boolean = true, // Default to sleek studio dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
