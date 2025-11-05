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
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig

@Composable
fun DrawGamepad(
    widthDp: Int,
    heightDp: Int,
    gamepadState: GamepadReading,
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
) {
    // Assuming Landscape orientation
    val baseDp = heightDp
    val altDp = widthDp

    val deadZonePadding = baseDp / 18

    // Helper function to get config for a component
    fun getConfig(component: ButtonComponent) = buttonConfigs[component] ?: ButtonConfig.default(component)

    // First we make a box that will contain the gamepad
    // And put padding around it so that it doesn't touch the edges of the screen
    Surface {
        Box(
            modifier = Modifier
                .padding(deadZonePadding.dp)
                .fillMaxSize()
        ) {

            // Left Analog Stick
            val leftStickConfig = getConfig(ButtonComponent.LEFT_ANALOG_STICK)
            if (leftStickConfig.visible) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopStart // Origin is top left
                ) {
                    AnalogStick(
                        modifier = Modifier.offset(
                            x = leftStickConfig.offsetX.dp,
                            y = leftStickConfig.offsetY.dp
                        ),
                        outerCircleWidth = (baseDp / 8 * leftStickConfig.scale).dp,
                        innerCircleRadius = (baseDp / 12 * leftStickConfig.scale).dp,
                        gamepadState = gamepadState,
                        type = AnalogStickType.LEFT,
                    )
                }
            }

            // D-Pad and Left Trigger
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomStart // Origin is bottom left
            ) {
                val dpadConfig = getConfig(ButtonComponent.DPAD)
                if (dpadConfig.visible) {
                    Dpad(
                        modifier = Modifier.offset(
                            x = (baseDp / 3).dp + dpadConfig.offsetX.dp,
                            y = dpadConfig.offsetY.dp
                        ),
                        size = (0.45 * baseDp * dpadConfig.scale).dp,
                        gamepadState = gamepadState,
                    )
                }

                val leftTriggerConfig = getConfig(ButtonComponent.LEFT_TRIGGER)
                if (leftTriggerConfig.visible) {
                    Trigger(
                        modifier = Modifier.offset(
                            x = leftTriggerConfig.offsetX.dp,
                            y = leftTriggerConfig.offsetY.dp
                        ),
                        type = TriggerType.LEFT,
                        size = (baseDp / 6 * leftTriggerConfig.scale).dp,
                        gamepadState = gamepadState,
                    )
                }
            }

            // Face Buttons
            val faceButtonsConfig = getConfig(ButtonComponent.FACE_BUTTONS)
            if (faceButtonsConfig.visible) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd // Origin is top right
                ) {
                    FaceButtons(
                        modifier = Modifier.offset(
                            x = faceButtonsConfig.offsetX.dp,
                            y = faceButtonsConfig.offsetY.dp
                        ),
                        size = (0.45 * baseDp * faceButtonsConfig.scale).dp,
                        gamepadState = gamepadState,
                    )
                }
            }

            // Right Analog Stick and Right Trigger
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd // Origin is bottom right
            ) {
                val rightStickConfig = getConfig(ButtonComponent.RIGHT_ANALOG_STICK)
                if (rightStickConfig.visible) {
                    AnalogStick(
                        modifier = Modifier.offset(
                            x = -(baseDp / 4).dp + rightStickConfig.offsetX.dp,
                            y = rightStickConfig.offsetY.dp
                        ),
                        outerCircleWidth = (baseDp / 8 * rightStickConfig.scale).dp,
                        innerCircleRadius = (baseDp / 12 * rightStickConfig.scale).dp,
                        gamepadState = gamepadState,
                        type = AnalogStickType.RIGHT,
                    )
                }

                val rightTriggerConfig = getConfig(ButtonComponent.RIGHT_TRIGGER)
                if (rightTriggerConfig.visible) {
                    Trigger(
                        modifier = Modifier.offset(
                            x = rightTriggerConfig.offsetX.dp,
                            y = rightTriggerConfig.offsetY.dp
                        ),
                        type = TriggerType.RIGHT,
                        size = (baseDp / 6 * rightTriggerConfig.scale).dp,
                        gamepadState = gamepadState,
                    )
                }
            }

            // Central Buttons (LB, RB, Select, Start)
            CentralButtons(
                baseDp = baseDp,
                gamepadState = gamepadState,
                buttonConfigs = buttonConfigs,
            )
        }
    }
}
