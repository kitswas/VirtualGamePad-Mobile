package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme


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
