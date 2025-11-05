package io.github.kitswas.virtualgamepadmobile.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs
import io.github.kitswas.virtualgamepadmobile.ui.composables.ButtonConfigEditor
import kotlinx.coroutines.runBlocking

const val gamepadCustomizationLogTag = "GamepadCustomizationScreen"

@Composable
fun GamepadCustomizationScreen(
    onNavigateBack: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val buttonConfigs by settingsRepository.buttonConfigs.collectAsState(initial = defaultButtonConfigs)
    var modifiedConfigs by remember { mutableStateOf<Map<ButtonComponent, ButtonConfig>?>(null) }

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

            // Scrollable button configuration content
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Customize individual button visibility, size, and position",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                ButtonComponent.entries.forEach { component ->
                    val config = if (modifiedConfigs != null) {
                        modifiedConfigs!![component] ?: ButtonConfig.default(component)
                    } else {
                        buttonConfigs[component] ?: ButtonConfig.default(component)
                    }

                    ButtonConfigEditor(
                        component = component,
                        config = config,
                        onConfigChange = { newConfig ->
                            val currentConfigs = modifiedConfigs ?: buttonConfigs
                            modifiedConfigs = currentConfigs + (component to newConfig)
                        }
                    )
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
                    modifiedConfigs = null
                    runBlocking {
                        settingsRepository.setAllButtonConfigs(defaultButtonConfigs)
                    }
                    Log.i(gamepadCustomizationLogTag, "Button configs reset to defaults")
                }) {
                    Text("Reset")
                }

                Button(onClick = {
                    modifiedConfigs?.let { configs ->
                        runBlocking {
                            try {
                                settingsRepository.setAllButtonConfigs(configs)
                                Log.i(gamepadCustomizationLogTag, "Button configs saved")
                            } catch (e: Exception) {
                                Log.e(gamepadCustomizationLogTag, "Error saving button configs", e)
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

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun GamepadCustomizationScreenPreview() {
    PreviewBase {
        GamepadCustomizationScreen(
            onNavigateBack = {},
            settingsRepository = SettingsRepository(LocalContext.current)
        )
    }
}
