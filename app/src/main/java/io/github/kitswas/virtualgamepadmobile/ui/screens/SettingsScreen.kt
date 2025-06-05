package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.kitswas.virtualgamepadmobile.data.BaseColor
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultBaseColor
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme
import io.github.kitswas.virtualgamepadmobile.data.defaultPollingDelay
import io.github.kitswas.virtualgamepadmobile.ui.composables.ColorSchemePicker
import io.github.kitswas.virtualgamepadmobile.ui.composables.ListItemPicker
import io.github.kitswas.virtualgamepadmobile.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
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
        val colorScheme = settingsRepository.colorScheme.collectAsState(defaultColorScheme).value
        val baseColor = settingsRepository.baseColor.collectAsState(defaultBaseColor).value
        val currentPollingDelay =
            settingsRepository.pollingDelay.collectAsState(defaultPollingDelay).value

        Text("Settings", style = MaterialTheme.typography.titleLarge)

        ColorSchemePicker(default = colorScheme) {
            saveJobsQueue.add {
                launch(Dispatchers.IO) {
                    settingsRepository.setColorScheme(it)
                }
            }
        }

        ListItemPicker(
            list = BaseColor.entries.asIterable(),
            default = baseColor,
            label = "Theme Color",
            onItemSelected = {
                saveJobsQueue.add {
                    launch(Dispatchers.IO) {
                        settingsRepository.setBaseColor(it)
                    }
                }
            })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                ListItemPicker(
                    list = 20..200 step 10,
                    default = currentPollingDelay,
                    label = "Polling Interval (ms)",
                    onItemSelected = {
                        saveJobsQueue.add {
                            launch(Dispatchers.IO) {
                                settingsRepository.setPollingDelay(it)
                            }
                        }
                    }
                )

                var toolTipState = rememberTooltipState() // Set initialIsVisible=true for testing
                var scope = rememberCoroutineScope()
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(shadowElevation = 10.dp) {
                            Text(
                                "Adjust according to your reflexes\nLower means faster",
                                style = Typography.bodyLarge
                            )
                        }
                    },
                    state = toolTipState
                ) {
                    IconButton(onClick = {
                        scope.launch { toolTipState.show() }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Information about polling interval"
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                saveJobsQueue.clear()
                runBlocking { settingsRepository.resetAllSettings() }
            }) {
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


@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun SettingsScreenPreview() {
    PreviewBase {
        SettingsScreen(settingsRepository = SettingsRepository(LocalContext.current))
    }
}