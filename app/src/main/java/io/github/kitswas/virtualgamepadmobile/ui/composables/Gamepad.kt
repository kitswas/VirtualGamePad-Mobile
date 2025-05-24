package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

@Composable
fun DrawGamepad(
    widthDp: Int,
    heightDp: Int,
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
            .fillMaxSize()
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart // Origin is top left
        ) {
            AnalogStick(
                outerCircleWidth = (baseDp / 8).dp,
                innerCircleRadius = (baseDp / 12).dp,
                gamepadState = gamepadState,
                type = AnalogStickType.LEFT,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart // Origin is bottom left
        ) {
            Dpad(
                modifier = Modifier.offset(
                    x = (baseDp / 3).dp, y = 0.dp
                ),
                size = (0.45 * baseDp).dp,
                gamepadState = gamepadState,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd // Origin is top right
        ) {
            FaceButtons(
                size = (0.45 * baseDp).dp,
                gamepadState = gamepadState,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd // Origin is bottom right
        ) {
            AnalogStick(
                modifier = Modifier.offset(
                    x = -(baseDp / 4).dp, y = 0.dp
                ),
                outerCircleWidth = (baseDp / 8).dp,
                innerCircleRadius = (baseDp / 12).dp,
                gamepadState = gamepadState,
                type = AnalogStickType.RIGHT,
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
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
                            gamepadState.ButtonsDown =
                                gamepadState.ButtonsDown and gameButton.value.inv()
                            gamepadState.ButtonsUp =
                                gamepadState.ButtonsUp or gameButton.value
                        }
                    }
                }
                Button(
                    modifier = Modifier.offset(
                        x = offsetX, y = 0.dp
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
                            gamepadState.ButtonsDown =
                                gamepadState.ButtonsDown and gameButton.value.inv()
                            gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
                        }
                    }
                }
                OutlinedIconButton(
                    modifier = Modifier
                        .size((baseDp / 8).dp)
                        .offset(
                            x = offsetX, y = (baseDp / 4).dp
                        ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    onClick = { },
                    interactionSource = interactionSource,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "${gameButton.name} Button",
                        modifier = Modifier.size((baseDp / 12).dp),
                        tint = foregroundColour
                    )
                }
            }
        }
    }
}
