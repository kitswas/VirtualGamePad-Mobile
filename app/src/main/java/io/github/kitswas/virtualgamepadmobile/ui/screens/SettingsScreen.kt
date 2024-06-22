package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.ui.theme.ColorSchemePicker
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import kotlinx.coroutines.*

@Composable
fun SettingsScreen(
    navController: NavController = rememberNavController(), settingsRepository: SettingsRepository
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Store pending settings modifications in a queue
        val saveJobsQueue = remember { mutableListOf<suspend CoroutineScope.() -> Unit>() }
        val colorScheme = settingsRepository.colorScheme.collectAsState(ColorScheme.SYSTEM).value

        Text("Settings Screen", style = MaterialTheme.typography.titleLarge)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp), contentPadding = PaddingValues(16.dp)
        ) {
            // Add dropdown for color scheme
            item {
                ColorSchemePicker(default = colorScheme) {
                    saveJobsQueue.add {
                        launch(Dispatchers.IO) {
                            settingsRepository.setColorScheme(it)
                        }
                    }
                }
            }

        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { TODO() }) {
                Text("Reset")
            }

            Button(onClick = {
                val success = runBlocking {
                    saveJobsQueue.forEach { it() }
                    saveJobsQueue.clear()
                    true
                }
                Log.i("SettingsScreen", "Saved settings: $success")
            }) {
                Text("Save")
            }

            Button(onClick = {
                saveJobsQueue.clear()
                navController.popBackStack()
            }) {
                Text("Cancel")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VirtualGamePadMobileTheme {
        SettingsScreen(settingsRepository = SettingsRepository(LocalContext.current))
    }
}
