package io.github.kitswas.virtualgamepadmobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun resetGamepadState(gamepadState: GamepadReading) {
    gamepadState.ButtonsUp = 0
    gamepadState.ButtonsDown = 0
    gamepadState.LeftThumbstickX = 0F
    gamepadState.LeftThumbstickY = 0F
    gamepadState.RightThumbstickX = 0F
    gamepadState.RightThumbstickY = 0F
    gamepadState.LeftTrigger = 0F
    gamepadState.RightTrigger = 0F
}

@Composable
fun GamePad(connectionViewModel: ConnectionViewModel?) {
    val gamepadState by remember { mutableStateOf(GamepadReading()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (gameButton in GameButtons.values()) {
            Button(
                onClick = {
                    if (connectionViewModel != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
                            connectionViewModel.sendGamepadState(gamepadState)
                            gamepadState.ButtonsUp = gamepadState.ButtonsDown
                            gamepadState.ButtonsDown = 0
                            connectionViewModel.sendGamepadState(gamepadState)
                        }
                    }
                }
            ) {
                Text(gameButton.name)
            }
        }
    }

    val activity = LocalContext.current.findActivity()
    // disconnect on back press
    androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
        .addObserver(androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY
                && activity?.isChangingConfigurations != true // ignore screen rotation
            ) {
                if (connectionViewModel != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // unset all keys before disconnecting
                        gamepadState.ButtonsUp = gamepadState.ButtonsDown
                        gamepadState.ButtonsDown = 0
                        connectionViewModel.sendGamepadState(gamepadState)
                        connectionViewModel.disconnect()
                    }
                }
            }
        })
}

@Preview(
    showBackground = true,
)
@Composable
fun GamePadPreview() {
    GamePad(null)
}
