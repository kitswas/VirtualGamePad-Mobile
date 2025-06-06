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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import io.github.kitswas.virtualgamepadmobile.ui.composables.SpinBox
import io.github.kitswas.virtualgamepadmobile.ui.theme.Typography
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue

const val logTag = "SettingsScreen"

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
        // Store pending settings modifications in a synchronized queue
        val saveJobsQueue = rememberSaveable {
            ConcurrentLinkedQueue(mutableListOf<suspend () -> Unit>())
        }
        val colorScheme by settingsRepository.colorScheme.collectAsState(initial = defaultColorScheme)
        val baseColor by settingsRepository.baseColor.collectAsState(initial = defaultBaseColor)
        val pollingDelay by settingsRepository.pollingDelay.collectAsState(initial = defaultPollingDelay)

        Text("Settings", style = MaterialTheme.typography.titleLarge)

        ColorSchemePicker(default = colorScheme) {
            val job = suspend {
                settingsRepository.setColorScheme(it)
            }
            saveJobsQueue.add(job)
        }

        ListItemPicker(
            list = BaseColor.entries.asIterable(),
            default = baseColor,
            label = "Theme Color",
            onItemSelected = {
                val job = suspend {
                    settingsRepository.setBaseColor(it)
                }
                saveJobsQueue.add(job)
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
                SpinBox(
                    value = pollingDelay,
                    onValueChange = { newValue ->
                        val job = suspend {
                            settingsRepository.setPollingDelay(newValue)
                        }
                        saveJobsQueue.add(job)
                    },
                    label = "Polling Interval (ms)",
                    minValue = 20,
                    maxValue = 200,
                    step = 10
                )

                var toolTipState = rememberTooltipState()
                var scope = rememberCoroutineScope()
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip(shadowElevation = 10.dp) {
                            Text(
                                "Adjust according to your reflexes\nLower is faster",
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
                Log.i(logTag, "Settings reset to defaults")
            }) {
                Text("Reset")
            }

            Button(onClick = {
                val success = runBlocking {
                    try {
                        saveJobsQueue.forEach {
                            it.invoke()
                        }
                        saveJobsQueue.clear()
                        true
                    } catch (e: Exception) {
                        Log.e(logTag, "Error saving settings", e)
                        false
                    }
                }
                Log.i(logTag, "Saved settings: $success")
                navController.popBackStack()
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
