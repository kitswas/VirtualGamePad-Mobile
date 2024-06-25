package io.github.kitswas.virtualgamepadmobile.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme

const val PreviewWidthDp = 640
const val PreviewHeightDp = 360

@Composable
fun PreviewBase(content: @Composable () -> Unit) {
    VirtualGamePadMobileTheme(darkMode = defaultColorScheme, baseColor = defaultBaseColor) {
        Surface(color = MaterialTheme.colorScheme.background) { content() }
    }
}
