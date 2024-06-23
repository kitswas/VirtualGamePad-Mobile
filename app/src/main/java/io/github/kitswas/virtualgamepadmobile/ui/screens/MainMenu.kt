package io.github.kitswas.virtualgamepadmobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import kotlin.system.exitProcess

@Composable
fun MainMenu(
    navController: NavHostController = rememberNavController()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            navController.navigate(
                "connect_screen"
            )
        }, shape = CircleShape) {
            Text(text = "Start")
        }
        Button(onClick = {
            navController.navigate(
                "settings_screen"
            )
        }, shape = CircleShape) {
            Text(text = "Settings")
        }
        Button(onClick = { exitProcess(0) }, shape = CircleShape) {
            Text(text = "Exit")
        }
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun MainMenuPreview() {
    PreviewBase { MainMenu() }
}
