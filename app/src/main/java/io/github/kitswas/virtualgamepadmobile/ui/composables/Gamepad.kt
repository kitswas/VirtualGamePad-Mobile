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
import io.github.kitswas.virtualgamepadmobile.data.ButtonAnchor
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig

/**
 * Convert ButtonAnchor to Compose Alignment
 */
private fun ButtonAnchor.toAlignment(): Alignment = when (this) {
    ButtonAnchor.TOP_LEFT -> Alignment.TopStart
    ButtonAnchor.TOP_CENTER -> Alignment.TopCenter
    ButtonAnchor.TOP_RIGHT -> Alignment.TopEnd
    ButtonAnchor.CENTER_LEFT -> Alignment.CenterStart
    ButtonAnchor.CENTER -> Alignment.Center
    ButtonAnchor.CENTER_RIGHT -> Alignment.CenterEnd
    ButtonAnchor.BOTTOM_LEFT -> Alignment.BottomStart
    ButtonAnchor.BOTTOM_CENTER -> Alignment.BottomCenter
    ButtonAnchor.BOTTOM_RIGHT -> Alignment.BottomEnd
}

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
    
    // Helper function to render a button component
    @Composable
    fun RenderComponent(component: ButtonComponent, content: @Composable (ButtonConfig) -> Unit) {
        val config = getConfig(component)
        if (config.visible) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = config.anchor.toAlignment()
            ) {
                content(config)
            }
        }
    }

    // First we make a box that will contain the gamepad
    // And put padding around it so that it doesn't touch the edges of the screen
    Surface {
        Box(
            modifier = Modifier
                .padding(deadZonePadding.dp)
                .fillMaxSize()
        ) {
            // Left Analog Stick
            RenderComponent(ButtonComponent.LEFT_ANALOG_STICK) { config ->
                AnalogStick(
                    modifier = Modifier.offset(
                        x = (config.offsetX * baseDp).dp,
                        y = (config.offsetY * baseDp).dp
                    ),
                    outerCircleWidth = (baseDp / 8 * config.scale).dp,
                    innerCircleRadius = (baseDp / 12 * config.scale).dp,
                    gamepadState = gamepadState,
                    type = AnalogStickType.LEFT,
                )
            }

            // Right Analog Stick
            RenderComponent(ButtonComponent.RIGHT_ANALOG_STICK) { config ->
                AnalogStick(
                    modifier = Modifier.offset(
                        x = (config.offsetX * baseDp).dp,
                        y = (config.offsetY * baseDp).dp
                    ),
                    outerCircleWidth = (baseDp / 8 * config.scale).dp,
                    innerCircleRadius = (baseDp / 12 * config.scale).dp,
                    gamepadState = gamepadState,
                    type = AnalogStickType.RIGHT,
                )
            }

            // D-Pad
            RenderComponent(ButtonComponent.DPAD) { config ->
                Dpad(
                    modifier = Modifier.offset(
                        x = (config.offsetX * baseDp).dp,
                        y = (config.offsetY * baseDp).dp
                    ),
                    size = (0.45 * baseDp * config.scale).dp,
                    gamepadState = gamepadState,
                )
            }

            // Face Buttons
            RenderComponent(ButtonComponent.FACE_BUTTONS) { config ->
                FaceButtons(
                    modifier = Modifier.offset(
                        x = (config.offsetX * baseDp).dp,
                        y = (config.offsetY * baseDp).dp
                    ),
                    size = (0.45 * baseDp * config.scale).dp,
                    gamepadState = gamepadState,
                )
            }

            // Left Trigger
            RenderComponent(ButtonComponent.LEFT_TRIGGER) { config ->
                Trigger(
                    modifier = Modifier.offset(
                        x = (config.offsetX * baseDp).dp,
                        y = (config.offsetY * baseDp).dp
                    ),
                    type = TriggerType.LEFT,
                    size = (baseDp / 6 * config.scale).dp,
                    gamepadState = gamepadState,
                )
            }

            // Right Trigger
            RenderComponent(ButtonComponent.RIGHT_TRIGGER) { config ->
                Trigger(
                    modifier = Modifier.offset(
                        x = (config.offsetX * baseDp).dp,
                        y = (config.offsetY * baseDp).dp
                    ),
                    type = TriggerType.RIGHT,
                    size = (baseDp / 6 * config.scale).dp,
                    gamepadState = gamepadState,
                )
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
