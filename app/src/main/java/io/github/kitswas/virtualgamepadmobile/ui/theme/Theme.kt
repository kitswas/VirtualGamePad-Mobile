package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme

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

@Composable
fun VirtualGamePadMobileTheme(
    darkMode: ColorScheme = defaultColorScheme,
    content: @Composable () -> Unit
) {

    val darkTheme: Boolean = when (darkMode) {
        ColorScheme.LIGHT -> false
        ColorScheme.DARK -> true
        ColorScheme.SYSTEM -> isSystemInDarkTheme()
    }

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
