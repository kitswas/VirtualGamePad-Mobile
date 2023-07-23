package io.github.kitswas.virtualgamepadmobile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    val pollingDelay = 10L // in milliseconds
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

    DrawGamepad(widthDp, heightDp, gamepadState)

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

@Composable
private fun DrawGamepad(
    widthDp: Float,
    heightDp: Float,
    gamepadState: GamepadReading,
) {
    val foregroundColour = MaterialTheme.colorScheme.primary
    val backgroundColour = darken(MaterialTheme.colorScheme.primary, 0.8f)

    // Assuming Landscape orientation
    val baseDp = heightDp
    val altDp = widthDp

    val deadZonePadding = baseDp / 18

    // First we make a box that will contain the gamepad
    // And put padding around it so that it doesn't touch the edges of the screen
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopStart // Origin is top left
    ) {
        AnalogStick(
            outerCircleWidth = (baseDp / 16).dp,
            innerCircleRadius = (baseDp / 8).dp,
            gamepadState = gamepadState,
            type = AnalogStickType.LEFT,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomStart // Origin is bottom left
    ) {
        Dpad(
            modifier = Modifier.offset(
                x = (baseDp / 3).dp,
                y = 0.dp
            ),
            size = (2 * baseDp / 5).dp,
            gamepadState = gamepadState,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopEnd // Origin is top right
    ) {
        FaceButtons(
            size = (2 * baseDp / 5).dp,
            gamepadState = gamepadState,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
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
            gamepadState = gamepadState,
            type = AnalogStickType.RIGHT,
        )
    }
    Box(
        modifier = Modifier
            .padding(deadZonePadding.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Origin is top center
    ) {
        for ((gameButton, text, offsetX) in listOf(
            Triple(GameButtons.LeftShoulder, "LSHLDR", -(baseDp / 4).dp),
            Triple(GameButtons.RightShoulder, "RSHLDR", (baseDp / 4).dp),
        )) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            // See https://stackoverflow.com/a/69157877/8659747
            if (isPressed) {
                Log.d(gameButton.name, "Pressed")
                gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
                //Use if + DisposableEffect to wait for the press action is completed
                DisposableEffect(Unit) {
                    onDispose {
                        Log.d(gameButton.name, "Released")
                        gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                        gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
                    }
                }
            }
            Button(
                modifier = Modifier
                    .offset(
                        x = offsetX,
                        y = 0.dp
                    ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                onClick = { },
                interactionSource = interactionSource,
            ) {
                Text(text)
            }
        }

        val screenIcon = ImageVector.vectorResource(R.drawable.screenicon)
        val menuIcon = Icons.Default.Menu
        for ((gameButton, icon, offsetX) in listOf(
            Triple(GameButtons.View, screenIcon, -(baseDp / 4).dp),
            Triple(GameButtons.Menu, menuIcon, (baseDp / 4).dp),
        )) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            // See https://stackoverflow.com/a/69157877/8659747
            if (isPressed) {
                Log.d(gameButton.name, "Pressed")
                gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
                //Use if + DisposableEffect to wait for the press action is completed
                DisposableEffect(Unit) {
                    onDispose {
                        Log.d(gameButton.name, "Released")
                        gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                        gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
                    }
                }
            }
            OutlinedIconButton(
                modifier = Modifier
                    .size((baseDp / 8).dp)
                    .offset(
                        x = offsetX,
                        y = (baseDp / 4).dp
                    ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                onClick = { },
                interactionSource = interactionSource,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "${gameButton.name} Button",
                    modifier = Modifier
                        .size((baseDp / 12).dp),
                    tint = foregroundColour
                )
            }
        }
    }
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
