package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs
import io.github.kitswas.virtualgamepadmobile.ui.composables.ButtonConfigEditor
import io.github.kitswas.virtualgamepadmobile.ui.composables.DrawGamepad
import io.github.kitswas.virtualgamepadmobile.ui.composables.ResponsiveGrid
import kotlinx.coroutines.runBlocking

private const val logTag = "GamepadCustomizationScreen"

@Composable
fun GamepadCustomizationScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val buttonConfigs by settingsRepository.buttonConfigs.collectAsState(initial = defaultButtonConfigs)
    var modifiedConfigs by remember { mutableStateOf<Map<ButtonComponent, ButtonConfig>?>(null) }
    var showPreview by remember { mutableStateOf(false) }


    // Get the current configs to preview (modified or saved)
    val currentConfigs = modifiedConfigs ?: buttonConfigs

    // Handle back button to close preview
    BackHandler(enabled = showPreview) {
        showPreview = false
    }

    if (showPreview) {
        // Full-screen preview overlay
        GamepadPreview(buttonConfigs = currentConfigs)
        return
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed title at the top
            Text(
                "Gamepad Customization",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Scrollable button configuration content with responsive grid
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Customize individual button visibility, size, and position",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                val minCardWidth = 420.dp
                // Responsive grid that dynamically calculates columns and batches items
                ResponsiveGrid(
                    items = ButtonComponent.entries.toList(),
                    minItemWidth = minCardWidth,
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp,
                ) { component ->
                    val config = if (modifiedConfigs != null) {
                        modifiedConfigs!![component] ?: ButtonConfig.default(component)
                    } else {
                        buttonConfigs[component] ?: ButtonConfig.default(component)
                    }

                    val onConfigChange = remember(component) {
                        { newConfig: ButtonConfig ->
                            val currentConfigs = modifiedConfigs ?: buttonConfigs
                            modifiedConfigs = currentConfigs + (component to newConfig)
                        }
                    }

                    ButtonConfigEditor(
                        component = component,
                        config = config,
                        onConfigChange = onConfigChange,
                        modifier = Modifier.widthIn(min = minCardWidth)
                    )
                }
            }

            // Fixed buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Preview button
                Button(
                    onClick = { showPreview = true },
                ) {
                    Text("Preview Gamepad")
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        modifiedConfigs = null
                        runBlocking {
                            settingsRepository.setAllButtonConfigs(defaultButtonConfigs)
                        }
                        Log.i(logTag, "Button configs reset to defaults")
                    }) {
                        Text("Reset")
                    }

                    Button(onClick = {
                        modifiedConfigs?.let { configs ->
                            runBlocking {
                                try {
                                    settingsRepository.setAllButtonConfigs(configs)
                                    Log.i(logTag, "Button configs saved")
                                } catch (e: Exception) {
                                    Log.e(
                                        logTag, "Error saving button configs", e
                                    )
                                }
                            }
                        }
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
}

/**
 * Full-screen gamepad preview overlay
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GamepadPreview(
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    val gamepadState = remember { GamepadReading() }

    // Draw the gamepad with current configuration
    DrawGamepad(
        widthDp = screenWidth,
        heightDp = screenHeight,
        gamepadState = gamepadState,
        buttonConfigs = buttonConfigs
    )
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@MultiDevicePreview
@Composable
fun GamepadCustomizationScreenPreview() {
    PreviewBase {
        GamepadCustomizationScreen(
            onNavigateBack = {},
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}
