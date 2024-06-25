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

    val darkColorPalette = darkColorScheme(
        primary = getColorFromBaseColor(baseColor, true),
        outline = Silver,
    )

    val lightColorPalette = lightColorScheme(
        primary = getColorFromBaseColor(baseColor, false),
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
