package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.kitswas.virtualgamepadmobile.data.BaseColor
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.data.getColorFromBaseColor
import androidx.compose.material3.ColorScheme as MaterialColorScheme

/**
 * Creates a dark mode color palette for the given base color.
 */
private fun createDarkColorPalette(baseColor: BaseColor): MaterialColorScheme {
    val darkColorPrimary = getColorFromBaseColor(baseColor, true)
    val onDarkColorPrimary = contrasting(darkColorPrimary)

    return darkColorScheme(
        primary = darkColorPrimary,
        secondary = darken(shift(darkColorPrimary, -45), 0.1f),
        tertiary = darken(shift(darkColorPrimary, -90), 0.2f),
        onPrimary = onDarkColorPrimary,
        onSecondary = lighten(onDarkColorPrimary, 0.1f),
        onTertiary = lighten(onDarkColorPrimary, 0.2f),
        outline = Silver,
    )
}

/**
 * Creates a light mode color palette for the given base color.
 */
private fun createLightColorPalette(baseColor: BaseColor): MaterialColorScheme {
    val lightColorPrimary = getColorFromBaseColor(baseColor, false)
    val onLightColorPrimary = contrasting(lightColorPrimary)

    return lightColorScheme(
        primary = lightColorPrimary,
        secondary = lighten(shift(lightColorPrimary, 45), 0.1f),
        tertiary = lighten(shift(lightColorPrimary, 90), 0.2f),
        onPrimary = onLightColorPrimary,
        onSecondary = darken(onLightColorPrimary, 0.1f),
        onTertiary = darken(onLightColorPrimary, 0.2f),
        outline = Gold,
    )
}

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

    val colorScheme = if (darkTheme) {
        createDarkColorPalette(baseColor)
    } else {
        createLightColorPalette(baseColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
