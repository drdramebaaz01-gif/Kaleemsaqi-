package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DkOrange,
    onPrimary = DkWhite,
    primaryContainer = DkOrangeDark,
    onPrimaryContainer = DkWhite,
    secondary = DkOrangeLight,
    onSecondary = DkBlack,
    background = DkDarkBackground,
    onBackground = DkWhite,
    surface = DkDarkSurface,
    onSurface = DkWhite,
    surfaceVariant = DkDarkGrey,
    onSurfaceVariant = DkLightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = DkOrange,
    onPrimary = DkWhite,
    primaryContainer = DkOrangeSubtle,
    onPrimaryContainer = DkOrangeDark,
    secondary = DkBlack,
    onSecondary = DkWhite,
    background = DkLightGrey,
    onBackground = DkBlack,
    surface = DkWhite,
    onSurface = DkBlack,
    surfaceVariant = DkLightGrey,
    onSurfaceVariant = DkDarkGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
