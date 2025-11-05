package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.data.SettingsRepository
import io.github.kitswas.virtualgamepadmobile.data.defaultButtonSize
import io.github.kitswas.virtualgamepadmobile.data.defaultPollingDelay
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.ui.composables.DrawGamepad
import io.github.kitswas.virtualgamepadmobile.ui.utils.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val tag = "GamePadScreen"

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun GamePad(
    connectionViewModel: ConnectionViewModel?,
    onNavigateBack: () -> Unit,
) {
    val gamepadState by remember { mutableStateOf(GamepadReading()) }
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val pollingDelay =
        settingsRepository.pollingDelay.collectAsState(defaultPollingDelay).value.toLong()
    val buttonSize =
        settingsRepository.buttonSize.collectAsState(defaultButtonSize).value

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    val isStopping = remember { mutableStateOf(false) }

    DrawGamepad(screenWidth, screenHeight, gamepadState, buttonSize.scaleFactor)

    val activity = LocalContext.current.findActivity()
    // disconnect on back press
    androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        .addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY
                && activity?.isChangingConfigurations != true // ignore screen rotation
            ) {
                if (connectionViewModel != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            isStopping.value = true
                            // unset all keys before disconnecting
                            gamepadState.ButtonsUp = gamepadState.ButtonsDown
                            gamepadState.ButtonsDown = 0
                            gamepadState.LeftThumbstickX = 0F
                            gamepadState.LeftThumbstickY = 0F
                            gamepadState.RightThumbstickX = 0F
                            gamepadState.RightThumbstickY = 0F
                            gamepadState.LeftTrigger = 0F
                            gamepadState.RightTrigger = 0F
                            connectionViewModel.enqueueGamepadState(gamepadState)
                            connectionViewModel.disconnect()
                            Log.d(tag, "Disconnected")
                        } catch (e: Exception) {
                            Log.d(tag, "Error during disconnect: ${e.message}")
                        }
                    }
                }
            }
        })

    // Monitor connection state and handle errors
    val connectionState by connectionViewModel?.uiState?.collectAsState()
        ?: remember { mutableStateOf(null) }

    // Use a more focused effect that reacts to the specific connection state properties
    LaunchedEffect(connectionState?.connected, connectionState?.error) {
        connectionState?.let { state ->
            if (!state.connected) {
                // Show toast with error if available, otherwise generic message
                val message = if (state.error != null) {
                    "Connection lost: ${state.error}"
                } else {
                    "Connection lost"
                }

                Log.d(tag, message)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()

                onNavigateBack()
            }
        }
    }

    val startAfter = 100L // in milliseconds

    // Send gamepad state updates periodically
    LaunchedEffect(gamepadState, pollingDelay) {
        delay(startAfter)

        // Start sending updates
        while (connectionViewModel != null && !isStopping.value) {
            // Queue the update in the ViewModel
            connectionViewModel.enqueueGamepadState(gamepadState)

            // Reset ButtonsUp after each update
            gamepadState.ButtonsUp = 0

            // Wait before next update
            delay(pollingDelay)
        }
    }
}

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Design Preview (Light)",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
)
@Preview(
    name = "Design Preview (Dark)",
    device = "spec:width=${PreviewWidthDp}dp,height=${PreviewHeightDp}dp,orientation=landscape,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Phone - Landscape (Light)",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Preview(
    name = "Phone - Landscape (Dark)",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Tablet (Light)",
    device = TABLET,
    showSystemUi = true
)
@Preview(
    name = "Tablet (Dark)",
    device = TABLET,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "Desktop (Light)",
    device = DESKTOP,
    showSystemUi = true
)
@Preview(
    name = "Desktop (Dark)",
    device = DESKTOP,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class MultiDevicePreview

@MultiDevicePreview
@Composable
fun GamePadPreview() {
    PreviewBase {
        GamePad(null) {}
    }
}
