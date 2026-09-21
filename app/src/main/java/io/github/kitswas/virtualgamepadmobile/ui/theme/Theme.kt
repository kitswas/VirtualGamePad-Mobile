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

// Fixed monochrome scheme (DESIGN.MD): grayscale only, dark always.
// Status is conveyed by shape + text, never by hue.
private fun monoDarkScheme(): MaterialColorScheme = darkColorScheme(
    primary = PristineWhite,
    onPrimary = PureBlack,
    secondary = SubduedSilver,
    onSecondary = PureBlack,
    tertiary = SubduedSilver,
    onTertiary = PureBlack,
    background = DeepSurface,
    onBackground = PristineWhite,
    surface = DeepSurface,
    onSurface = PristineWhite,
    surfaceVariant = SlateGray,
    onSurfaceVariant = SubduedSilver,
    primaryContainer = SlateGray,
    onPrimaryContainer = PristineWhite,
    secondaryContainer = SlateGray,
    onSecondaryContainer = PristineWhite,
    tertiaryContainer = SlateGray,
    onTertiaryContainer = PristineWhite,
    error = PristineWhite,
    onError = PureBlack,
    errorContainer = SlateGray,
    onErrorContainer = PristineWhite,
    outline = SubduedSilver,
    outlineVariant = SlateGray,
    scrim = PureBlack,
)

private fun createDarkColorPalette(baseColor: BaseColor): MaterialColorScheme {
    val darkColorPrimary = getColorFromBaseColor(baseColor, true)
    val onDarkColorPrimary = contrasting(darkColorPrimary)
    val darkColorSecondary = darken(shift(darkColorPrimary, -45), 0.1f)
    val darkColorTertiary = darken(shift(darkColorPrimary, -75), 0.25f)

    return darkColorScheme(
        primary = darkColorPrimary,
        secondary = darkColorSecondary,
        tertiary = darkColorTertiary,
        onPrimary = onDarkColorPrimary,
        onSecondary = lighten(onDarkColorPrimary, 0.1f),
        onTertiary = lighten(onDarkColorPrimary, 0.2f),
        primaryContainer = darken(darkColorPrimary, 0.6f),
        secondaryContainer = darken(darkColorSecondary, 0.6f),
        tertiaryContainer = darken(darkColorTertiary, 0.6f),
        onPrimaryContainer = lighten(darkColorPrimary, 0.6f),
        onSecondaryContainer = lighten(darkColorSecondary, 0.6f),
        onTertiaryContainer = lighten(darkColorTertiary, 0.6f),
        outline = Silver,
    )
}

private fun createLightColorPalette(baseColor: BaseColor): MaterialColorScheme {
    val lightColorPrimary = getColorFromBaseColor(baseColor, false)
    val onLightColorPrimary = contrasting(lightColorPrimary)
    val lightColorSecondary = lighten(shift(lightColorPrimary, 45), 0.1f)
    val lightColorTertiary = lighten(shift(lightColorPrimary, 75), 0.25f)

    return lightColorScheme(
        primary = lightColorPrimary,
        secondary = lightColorSecondary,
        tertiary = lightColorTertiary,
        onPrimary = onLightColorPrimary,
        onSecondary = darken(onLightColorPrimary, 0.1f),
        onTertiary = darken(onLightColorPrimary, 0.2f),
        primaryContainer = lighten(lightColorPrimary, 0.6f),
        secondaryContainer = lighten(lightColorSecondary, 0.6f),
        tertiaryContainer = lighten(lightColorTertiary, 0.6f),
        onPrimaryContainer = darken(lightColorPrimary, 0.6f),
        onSecondaryContainer = darken(lightColorSecondary, 0.6f),
        onTertiaryContainer = darken(lightColorTertiary, 0.6f),
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

    val colorScheme = monoDarkScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
