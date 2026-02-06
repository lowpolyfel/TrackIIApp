package com.ttelectronics.trackiiapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TTBlue,
    secondary = TTBlueDark,
    tertiary = TTAccent,
    background = TTBlueTint,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TTTextPrimary,
    onBackground = TTTextPrimary,
    onSurface = TTTextPrimary
)

@Composable
fun TrackIIAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
