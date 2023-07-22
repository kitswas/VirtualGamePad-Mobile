package io.github.kitswas.virtualgamepadmobile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Locks the screen orientation to the given orientation.
 * @see <a href="https://stackoverflow.com/a/69231996/8659747"> StackOverflow Answer <a/>
 */
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(orientation) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun GamePad(widthDp: Float, heightDp: Float, connectionViewModel: ConnectionViewModel?) {
    val gamepadState by remember { mutableStateOf(GamepadReading()) }
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

//    // Scale the gamepad to fit the screen
//    val configuration = LocalConfiguration.current
//    val baseDp = when (configuration.orientation) {
//        Configuration.ORIENTATION_LANDSCAPE -> {
//            heightDp
//        }
//
//        else -> {
//            widthDp
//        }
//    }
    // Assuming Landscape orientation
    val baseDp = heightDp
    val altDp = widthDp

    val deadZonePadding = baseDp / 18

    // First we make a box that will contain the gamepad
    // And put padding around it so that it doesn't touch the edges of the screen
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
//            .background(Color.Magenta)
            .fillMaxSize(),
        contentAlignment = Alignment.TopStart // Origin is top left
    ) {
        AnalogStick(
            outerCircleWidth = (baseDp / 16).dp,
            innerCircleRadius = (baseDp / 8).dp,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
//            .background(Color.Magenta)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomStart // Origin is bottom left
    ) {
        Dpad(
            size = (2 * baseDp / 5).dp,
            modifier = Modifier.offset(
                x = (baseDp / 3).dp,
                y = 0.dp
            ),
            gamepadState = gamepadState,
            connectionViewModel = connectionViewModel,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
//            .background(Color.Magenta)
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd // Origin is top right
    ) {
        FaceButtons(
            size = (2 * baseDp / 5).dp,
            gamepadState = gamepadState,
            connectionViewModel = connectionViewModel,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
//            .background(Color.Magenta)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd // Origin is bottom right
    ) {
        AnalogStick(
            modifier = Modifier.offset(
                x = -(baseDp / 3).dp,
                y = 0.dp
            ),
            outerCircleWidth = (baseDp / 16).dp,
            innerCircleRadius = (baseDp / 8).dp,
        )
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
                        Log.d("GamePad", "Disconnected")
                    }
                }
            }
        })
}

const val PreviewWidthDp = 900
const val PreviewHeightDp = 400

@Preview(
    showBackground = true,
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
)
@Composable
fun GamePadPreview() {
    VirtualGamePadMobileTheme {
        GamePad(PreviewWidthDp.toFloat(), PreviewHeightDp.toFloat(), null)
    }
}

@Preview(
    showBackground = false,
    widthDp = PreviewWidthDp,
    heightDp = PreviewHeightDp,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun GamePadPreviewNight() {
    VirtualGamePadMobileTheme {
        GamePad(PreviewWidthDp.toFloat(), PreviewHeightDp.toFloat(), null)
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
