package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.BaseColor
import io.github.kitswas.virtualgamepadmobile.data.ColorScheme
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultBaseColor
import io.github.kitswas.virtualgamepadmobile.data.defaultColorScheme
import io.github.kitswas.virtualgamepadmobile.data.defaultFullScreenEnabled
import io.github.kitswas.virtualgamepadmobile.data.defaultHapticFeedbackEnabled
import io.github.kitswas.virtualgamepadmobile.data.defaultPollingDelay
import io.github.kitswas.virtualgamepadmobile.data.defaultSaveConnectionCredentials
import io.github.kitswas.virtualgamepadmobile.ui.composables.ColorSchemePicker
import io.github.kitswas.virtualgamepadmobile.ui.composables.ListItemPicker
import io.github.kitswas.virtualgamepadmobile.ui.composables.SpinBox
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.parcelize.Parcelize

private const val logTag = "SettingsScreen"

@Parcelize
private data class SettingsChanges(
    val colorScheme: ColorScheme? = null,
    val baseColor: BaseColor? = null,
    val pollingDelay: Int? = null,
    val hapticFeedbackEnabled: Boolean? = null,
    val saveConnectionCredentials: Boolean? = null,
    val fullScreenEnabled: Boolean? = null
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToGamepadCustomization: () -> Unit,
    settingsRepository: SettingsRepository
) {
    var settingsChanges by rememberSaveable { mutableStateOf(SettingsChanges()) }

    Scaffold { paddingValues ->
        val colorScheme by settingsRepository.colorScheme.collectAsState(initial = defaultColorScheme)
        val baseColor by settingsRepository.baseColor.collectAsState(initial = defaultBaseColor)
        val pollingDelay by settingsRepository.pollingDelay.collectAsState(initial = defaultPollingDelay)
        val hapticEnabled by settingsRepository.hapticFeedbackEnabled.collectAsState(initial = defaultHapticFeedbackEnabled)
        val saveCredentials by settingsRepository.saveConnectionCredentials.collectAsState(initial = defaultSaveConnectionCredentials)
        val fullScreenEnabled by settingsRepository.fullScreenEnabled.collectAsState(initial = defaultFullScreenEnabled)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed title at the top
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Scrollable settings content
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ColorSchemePicker(selectedItem = settingsChanges.colorScheme ?: colorScheme) {
                    settingsChanges = settingsChanges.copy(colorScheme = it)
                }

                ListItemPicker(
                    list = BaseColor.entries.asIterable(),
                    selectedItem = settingsChanges.baseColor ?: baseColor,
                    label = stringResource(R.string.settings_theme_color),
                    formattedDisplay = { item ->
                        Text(text = stringResource(item.nameRes))
                    },
                    onItemSelected = {
                        settingsChanges = settingsChanges.copy(baseColor = it)
                    })

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SpinBox(
                        value = settingsChanges.pollingDelay ?: pollingDelay,
                        onValueChange = {
                            settingsChanges = settingsChanges.copy(pollingDelay = it)
                        },
                        label = stringResource(R.string.settings_polling_interval),
                        minValue = 20,
                        maxValue = 200,
                        step = 10
                    )

                    val toolTipState = rememberTooltipState()
                    val scope = rememberCoroutineScope()
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Start
                        ),
                        tooltip = {
                            PlainTooltip(shadowElevation = 10.dp) {
                                Text(
                                    stringResource(R.string.settings_polling_interval_desc),
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
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.settings_polling_interval_info),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        stringResource(R.string.settings_haptic_feedback),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Switch(
                        checked = settingsChanges.hapticFeedbackEnabled ?: hapticEnabled,
                        onCheckedChange = {
                            settingsChanges = settingsChanges.copy(hapticFeedbackEnabled = it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        stringResource(R.string.settings_full_screen),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Switch(
                        checked = settingsChanges.fullScreenEnabled ?: fullScreenEnabled,
                        onCheckedChange = {
                            settingsChanges = settingsChanges.copy(fullScreenEnabled = it)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        stringResource(R.string.settings_save_connection_credentials),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Switch(
                        checked = settingsChanges.saveConnectionCredentials ?: saveCredentials,
                        onCheckedChange = {
                            settingsChanges = settingsChanges.copy(saveConnectionCredentials = it)
                        }
                    )
                }

                Button(
                    onClick = onNavigateToGamepadCustomization,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.settings_customize_layout))
                }

            }

            // Fixed buttons at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    settingsChanges = SettingsChanges()
                    runBlocking { settingsRepository.resetAllSettings() }
                    Log.i(logTag, "Settings reset to defaults")
                }) {
                    Text(stringResource(R.string.reset))
                }

                Button(onClick = {
                    var changesSaved = 0
                    runBlocking {
                        try {
                            settingsChanges.colorScheme?.let {
                                settingsRepository.setColorScheme(
                                    it
                                ); ++changesSaved
                            }
                            settingsChanges.baseColor?.let { settingsRepository.setBaseColor(it); ++changesSaved }
                            settingsChanges.pollingDelay?.let {
                                settingsRepository.setPollingDelay(
                                    it
                                ); ++changesSaved
                            }
                            settingsChanges.hapticFeedbackEnabled?.let {
                                settingsRepository.setHapticFeedbackEnabled(
                                    it
                                ); ++changesSaved
                            }
                            settingsChanges.saveConnectionCredentials?.let {
                                settingsRepository.setSaveConnectionCredentials(
                                    it
                                ); ++changesSaved
                            }
                            settingsChanges.fullScreenEnabled?.let {
                                settingsRepository.setFullScreenEnabled(
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
                    Text(stringResource(R.string.save))
                }
                Button(onClick = onNavigateBack) {
                    Text(stringResource(R.string.cancel))
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
            onNavigateToGamepadCustomization = {},
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}
