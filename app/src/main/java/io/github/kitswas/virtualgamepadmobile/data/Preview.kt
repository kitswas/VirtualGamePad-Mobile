package io.github.kitswas.virtualgamepadmobile.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme

// The width and height of the preview surface in dp
const val PREVIEW_WIDTH_DP = 960
const val PREVIEW_HEIGHT_DP = 540

@Composable
fun PreviewBase(content: @Composable () -> Unit) {
    VirtualGamePadMobileTheme(darkMode = defaultColorScheme, baseColor = defaultBaseColor) {
        Surface(color = MaterialTheme.colorScheme.background) { content() }
    }
}
