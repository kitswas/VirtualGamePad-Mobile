package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading

@Composable
fun DrawGamepad(
    widthDp: Int,
    heightDp: Int,
    gamepadState: GamepadReading,
    buttonScaleFactor: Float = 1.0f,
) {
    // Assuming Landscape orientation
    val baseDp = heightDp
    val altDp = widthDp

    val deadZonePadding = baseDp / 18

    // First we make a box that will contain the gamepad
    // And put padding around it so that it doesn't touch the edges of the screen
    Surface {
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
                    outerCircleWidth = (baseDp / 8 * buttonScaleFactor).dp,
                    innerCircleRadius = (baseDp / 12 * buttonScaleFactor).dp,
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
                    size = (0.45 * baseDp * buttonScaleFactor).dp,
                    gamepadState = gamepadState,
                )
                Trigger(
                    type = TriggerType.LEFT,
                    size = (baseDp / 6 * buttonScaleFactor).dp,
                    gamepadState = gamepadState,
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd // Origin is top right
            ) {
                FaceButtons(
                    size = (0.45 * baseDp * buttonScaleFactor).dp,
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
                    outerCircleWidth = (baseDp / 8 * buttonScaleFactor).dp,
                    innerCircleRadius = (baseDp / 12 * buttonScaleFactor).dp,
                    gamepadState = gamepadState,
                    type = AnalogStickType.RIGHT,
                )
                Trigger(
                    type = TriggerType.RIGHT,
                    size = (baseDp / 6 * buttonScaleFactor).dp,
                    gamepadState = gamepadState,
                )
            }
            CentralButtons(
                baseDp = baseDp,
                gamepadState = gamepadState,
                scaleFactor = buttonScaleFactor,
            )
        }
    }
}
