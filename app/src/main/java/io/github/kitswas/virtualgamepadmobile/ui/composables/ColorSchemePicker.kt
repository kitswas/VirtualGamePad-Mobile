package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase

@Composable
fun ColorSchemePicker(
    default: ColorScheme,
    modifier: Modifier = Modifier,
    onColorSchemeSelected: (ColorScheme) -> Unit = { _ -> }
) {
    ListItemPicker(
        modifier = modifier,
        list = ColorScheme.entries.asIterable(),
        default = default,
        label = stringResource(R.string.settings_color_scheme),
        formattedDisplay = { item ->
            Text(text = stringResource(item.nameRes))
        },
        onItemSelected = onColorSchemeSelected
    )
}

@Preview(showBackground = true)
@Composable
fun ColorSchemePickerPreview() {
    PreviewBase {
        ColorSchemePicker(ColorScheme.SYSTEM, Modifier.padding(16.dp))
    }
}
