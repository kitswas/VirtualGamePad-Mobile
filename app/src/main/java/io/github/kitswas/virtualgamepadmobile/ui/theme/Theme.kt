package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColorScheme(
    primary = NeonBlue,
    secondary = NeonGreen,
    tertiary = NeonRed,
    outline = Silver,
)

private val LightColorPalette = lightColorScheme(
    primary = GlossyBlue,
    secondary = GlossyGreen,
    tertiary = GlossyRed,
    outline = Gold,
)

enum class ColorScheme {
    LIGHT, DARK, SYSTEM;

    companion object {
        fun fromInt(i: Int): ColorScheme {
            return when (i) {
                0 -> LIGHT
                1 -> DARK
                2 -> SYSTEM
                else -> throw IllegalArgumentException("Invalid ColorScheme value")
            }
        }
    }
}

@Composable
fun VirtualGamePadMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
