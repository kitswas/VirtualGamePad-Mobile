package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.ui.composables.DrawGamepad
import io.github.kitswas.virtualgamepadmobile.ui.utils.findActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GamePad(connectionViewModel: ConnectionViewModel?) {
    val gamepadState by remember { mutableStateOf(GamepadReading()) }

    val pollingDelay = 100L // in milliseconds
    val startAfter = 100L // in milliseconds
    // Send gamepad state every pollingDelay milliseconds
    LaunchedEffect(gamepadState) {
        delay(startAfter)
        while (true) {
            if (connectionViewModel != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    connectionViewModel.sendGamepadState(gamepadState)
                    gamepadState.ButtonsUp = 0
                }
            }
            delay(pollingDelay)
        }
    }

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp

    DrawGamepad(screenWidth, screenHeight, gamepadState)

    val activity = LocalContext.current.findActivity()
    // disconnect on back press
    androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        .addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY
                && activity?.isChangingConfigurations != true // ignore screen rotation
            ) {
                if (connectionViewModel != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // unset all keys before disconnecting
                        gamepadState.ButtonsUp = gamepadState.ButtonsDown
                        gamepadState.ButtonsDown = 0
                        gamepadState.LeftThumbstickX = 0F
                        gamepadState.LeftThumbstickY = 0F
                        gamepadState.RightThumbstickX = 0F
                        gamepadState.RightThumbstickY = 0F
                        gamepadState.LeftTrigger = 0F
                        gamepadState.RightTrigger = 0F
                        connectionViewModel.sendGamepadState(gamepadState)
                        connectionViewModel.disconnect()
                        Log.d("GamePad", "Disconnected")
                    }
                }
            }
        })
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun GamePadPreview() {
    PreviewBase {
        GamePad(null)
    }
}

@Preview(
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GamePadPreviewNight() {
    PreviewBase {
        GamePad(null)
    }
}

private fun resetGamepadState(gamepadState: GamepadReading) {
    gamepadState.ButtonsUp = 0
    gamepadState.ButtonsDown = 0
    gamepadState.LeftThumbstickX = 0F
    gamepadState.LeftThumbstickY = 0F
    gamepadState.RightThumbstickX = 0F
    gamepadState.LeftTrigger = 0F
    gamepadState.RightThumbstickY = 0F
    gamepadState.RightTrigger = 0F
}
