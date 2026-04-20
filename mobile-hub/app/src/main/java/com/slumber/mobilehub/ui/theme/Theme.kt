package com.slumber.mobilehub.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val SlumberDarkScheme = darkColorScheme(
    primary = GlowMint,
    onPrimary = DeepNight,
    secondary = SignalBlue,
    onSecondary = DeepNight,
    tertiary = Ember,
    background = DeepNight,
    onBackground = Cloud,
    surface = Slate,
    onSurface = Cloud,
    onSurfaceVariant = Mist,
    surfaceVariant = Aurora,
    outline = GlowMint.copy(alpha = 0.45f)
)

private val SlumberLightScheme = lightColorScheme(
    primary = ColorTokens.lightPrimary,
    onPrimary = Cloud,
    secondary = SignalBlue,
    onSecondary = DeepNight,
    tertiary = Ember,
    background = Cloud,
    onBackground = DeepNight,
    surface = ColorTokens.lightSurface,
    onSurface = DeepNight,
    onSurfaceVariant = ColorTokens.lightOnSurfaceVariant,
    surfaceVariant = ColorTokens.lightSurfaceVariant,
    outline = ColorTokens.lightOutline
)

@Composable
fun SlumberMobileHubTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) SlumberDarkScheme else SlumberLightScheme,
        typography = Typography,
        content = content
    )
}

private object ColorTokens {
    val lightPrimary = Color(0xFF0F6D67)
    val lightSurface = Color(0xFFFFFFFF)
    val lightSurfaceVariant = Color(0xFFDDEBF3)
    val lightOnSurfaceVariant = Color(0xFF506678)
    val lightOutline = Color(0xFF7AA3B6)
}
