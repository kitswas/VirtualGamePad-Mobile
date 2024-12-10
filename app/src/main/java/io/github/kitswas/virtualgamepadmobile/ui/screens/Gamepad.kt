package io.github.kitswas.virtualgamepadmobile.ui.screens

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.data.PreviewHeightDp
import io.github.kitswas.virtualgamepadmobile.data.PreviewWidthDp
import io.github.kitswas.virtualgamepadmobile.network.ConnectionViewModel
import io.github.kitswas.virtualgamepadmobile.ui.composables.DrawGamepad
import io.github.kitswas.virtualgamepadmobile.ui.utils.findActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GamePad(
    connectionViewModel: ConnectionViewModel?,
    navController: NavHostController = rememberNavController(),
) {
    val gamepadState by remember { mutableStateOf(GamepadReading()) }

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

    val pollingDelay = 100L // in milliseconds
    val startAfter = 100L // in milliseconds
    val lastError = remember { mutableStateOf<Throwable?>(null) }
    // Send gamepad state every pollingDelay milliseconds
    LaunchedEffect(gamepadState) {
        delay(startAfter)
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("GamePad", throwable.message ?: "Unknown connection error")
            lastError.value = throwable
        }
        while (true) {
            if (connectionViewModel != null) {
                CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                    connectionViewModel.sendGamepadState(gamepadState)
                    gamepadState.ButtonsUp = 0
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                lastError.value.let { throwable ->
                    if (throwable is java.net.SocketException) {
                        Toast.makeText(
                            activity,
                            "Disconnected: ${throwable.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        connectionViewModel?.disconnect()
                        navController.popBackStack()
                    }
                    lastError.value = null
                }
            }
            delay(pollingDelay)
        }
    }


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
