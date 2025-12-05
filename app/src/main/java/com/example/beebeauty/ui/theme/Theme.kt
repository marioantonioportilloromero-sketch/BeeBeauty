package com.example.beebeauty.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ColorPrincipal,
    secondary = ColorAcento,
    tertiary = ColorSecundario,
    background = ColorFondo,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ColorTexto,
    onSurface = ColorTexto
)

@Composable
fun BeeBeautyCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}