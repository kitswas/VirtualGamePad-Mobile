package io.github.kitswas.virtualgamepadmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp

@Composable
fun MainMenu(
    onNavigateToConnectScreen: () -> Unit,
    onNavigateToSettingsScreen: () -> Unit,
    onNavigateToAboutScreen: () -> Unit,
    onExit: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = onNavigateToConnectScreen, shape = CircleShape) {
                Text(text = "Start")
            }
            Button(onClick = onNavigateToSettingsScreen, shape = CircleShape) {
                Text(text = "Settings")
            }
            Button(onClick = onNavigateToAboutScreen, shape = CircleShape) {
                Text(text = "About")
            }
            Button(onClick = onExit, shape = CircleShape) {
                Text(text = "Exit")
            }
        }
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun MainMenuPreview() {
    PreviewBase {
        MainMenu(
            onNavigateToConnectScreen = {},
            onNavigateToSettingsScreen = {},
            onNavigateToAboutScreen = {},
            onExit = {}
        )
    }
}
