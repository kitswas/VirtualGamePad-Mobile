package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.os.Parcelable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.BaseColor
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize

const val logTag = "SettingsScreen"

@Parcelize
private data class SettingsChanges(
    var colorScheme: ColorScheme? = null,
    var baseColor: BaseColor? = null,
    var pollingDelay: Int? = null
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val settingsChanges by rememberSaveable { mutableStateOf(SettingsChanges()) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val colorScheme by settingsRepository.colorScheme.collectAsState(initial = defaultColorScheme)
            val baseColor by settingsRepository.baseColor.collectAsState(initial = defaultBaseColor)
            val pollingDelay by settingsRepository.pollingDelay.collectAsState(initial = defaultPollingDelay)

            Text("Settings", style = MaterialTheme.typography.titleLarge)

            ColorSchemePicker(default = colorScheme) {
                settingsChanges.colorScheme = it
            }

            ListItemPicker(
                list = BaseColor.entries.asIterable(),
                default = baseColor,
                label = "Theme Color",
                onItemSelected = {
                    settingsChanges.baseColor = it
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
                        onValueChange = {
                            settingsChanges.pollingDelay = it
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
                                    style = MaterialTheme.typography.bodyLarge
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
                    settingsChanges.pollingDelay = null
                    settingsChanges.colorScheme = null
                    settingsChanges.baseColor = null
                    runBlocking { settingsRepository.resetAllSettings() }
                    Log.i(logTag, "Settings reset to defaults")
                }) {
                    Text("Reset")
                }

                Button(onClick = {
                    var changesSaved = 0
                    runBlocking {
                        try {
                            settingsChanges.colorScheme?.let { settingsRepository.setColorScheme(it); ++changesSaved }
                            settingsChanges.baseColor?.let { settingsRepository.setBaseColor(it); ++changesSaved }
                            settingsChanges.pollingDelay?.let {
                                settingsRepository.setPollingDelay(
                                    it
                                ); ++changesSaved
                            }
                        } catch (e: Exception) {
                            Log.e(logTag, "Error saving settings", e)
                        }
                    }
                    Log.i(logTag, "Saved settings: $changesSaved")
                    onNavigateBack()
                }) {
                    Text("Save")
                }
                Button(onClick = onNavigateBack) {
                    Text("Cancel")
                }
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
        SettingsScreen(
            onNavigateBack = {},
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}
