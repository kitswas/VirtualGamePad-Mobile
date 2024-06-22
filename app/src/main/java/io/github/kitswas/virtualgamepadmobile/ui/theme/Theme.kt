package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.settingsDataStore
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

@Composable
fun ColorSchemePicker(
    default: ColorScheme,
    modifier: Modifier = Modifier, onColorSchemeSelected: (ColorScheme) -> Unit = { _ -> }
) {

    ListItemPicker(
        modifier = modifier,
        list = ColorScheme.entries.asIterable(),
        default = default,
        label = "Color Scheme",
        onItemSelected = onColorSchemeSelected
    )
}

@Preview(showBackground = true)
@Composable
fun ColorSchemePickerPreview() {
    VirtualGamePadMobileTheme {
        ColorSchemePicker(ColorScheme.SYSTEM, Modifier.padding(16.dp))
    }
}

@Composable
fun VirtualGamePadMobileTheme(
    darkMode: ColorScheme = ColorScheme.SYSTEM,
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
