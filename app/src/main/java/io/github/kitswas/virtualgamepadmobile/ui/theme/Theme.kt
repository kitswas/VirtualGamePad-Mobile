package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import io.github.kitswas.virtualgamepadmobile.ui.composables.ListItemPicker

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
fun ColorSchemePicker(
    modifier: Modifier = Modifier, onColorSchemeSelected: (ColorScheme) -> Unit = { _ -> }
) {

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)

    ListItemPicker(
        modifier = modifier,
        list = ColorScheme.values().asIterable(),
        default = ColorScheme.fromInt(
            sharedPreferences.getInt(
                "color_scheme",
                ColorScheme.SYSTEM.ordinal
            )
        ),
        label = "Color Scheme",
        onItemSelected = onColorSchemeSelected
    )
}

@Preview(showBackground = true)
@Composable
fun ColorSchemePickerPreview() {
    VirtualGamePadMobileTheme {
        ColorSchemePicker(Modifier.padding(16.dp))
    }
}

@Composable
fun VirtualGamePadMobileTheme(
    content: @Composable () -> Unit
) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)

    val darkMode: ColorScheme =
        ColorScheme.fromInt(sharedPreferences.getInt("color_scheme", ColorScheme.SYSTEM.ordinal))
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
