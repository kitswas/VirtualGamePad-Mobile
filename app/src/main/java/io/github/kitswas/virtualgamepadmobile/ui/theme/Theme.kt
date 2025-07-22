package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.kitswas.virtualgamepadmobile.data.BaseColor
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.data.getColorFromBaseColor

@Composable
fun VirtualGamePadMobileTheme(
    darkMode: ColorScheme,
    baseColor: BaseColor,
    content: @Composable () -> Unit
) {

    val darkTheme: Boolean = when (darkMode) {
        ColorScheme.LIGHT -> false
        ColorScheme.DARK -> true
        ColorScheme.SYSTEM -> isSystemInDarkTheme()
    }

    val darkColorPrimary = getColorFromBaseColor(baseColor, true)
    val lightColorPrimary = getColorFromBaseColor(baseColor, false)
    val onDarkColorPrimary = contrasting(darkColorPrimary)
    val onLightColorPrimary = contrasting(lightColorPrimary)

    val darkColorPalette = darkColorScheme(
        primary = darkColorPrimary,
        secondary = darken(shift(darkColorPrimary, -45), 0.1f),
        tertiary = darken(shift(darkColorPrimary, -90), 0.2f),
        onPrimary = onDarkColorPrimary,
        onSecondary = lighten(onDarkColorPrimary, 0.1f),
        onTertiary = lighten(onDarkColorPrimary, 0.2f),
        outline = Silver,
    )

    val lightColorPalette = lightColorScheme(
        primary = lightColorPrimary,
        secondary = lighten(shift(lightColorPrimary, 45), 0.1f),
        tertiary = lighten(shift(lightColorPrimary, 90), 0.2f),
        onPrimary = onLightColorPrimary,
        onSecondary = darken(onLightColorPrimary, 0.1f),
        onTertiary = darken(onLightColorPrimary, 0.2f),
        outline = Gold,
    )

    val colorScheme = if (darkTheme) {
        darkColorPalette
    } else {
        lightColorPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
