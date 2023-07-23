package io.github.kitswas.virtualgamepadmobile

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.theme.lighten
import kotlin.math.roundToInt

enum class AnalogStickType {
    LEFT, RIGHT
}

@Composable
fun AnalogStick(
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.outline,
    ringWidth: Dp = 4.dp,
    outerCircleColor: Color = lighten(MaterialTheme.colorScheme.primary, 0.2f),
    outerCircleWidth: Dp = 4.dp,
    innerCircleColor: Color = darken(MaterialTheme.colorScheme.primary, 0.2f),
    innerCircleRadius: Dp = 32.dp,
    gamepadState: GamepadReading,
    type: AnalogStickType,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    )
    {
        // First draw the glow ring
        Circle(
            colour = ringColor,
            modifier = Modifier
                .size((innerCircleRadius + outerCircleWidth + ringWidth) * 2),
            contentAlignment = Alignment.Center
        ) {
            // Then draw the outer circle
            Circle(
                modifier = Modifier
                    .size((innerCircleRadius + outerCircleWidth) * 2),
                contentAlignment = Alignment.Center,
                colour = outerCircleColor,
            ) {
                var offsetX by remember { mutableStateOf(0f) }
                var offsetY by remember { mutableStateOf(0f) }
                var scaledOffsetX by remember { mutableStateOf(0f) }
                var scaledOffsetY by remember { mutableStateOf(0f) }

                // Then draw the inner circle
                Circle(
                    colour = innerCircleColor,
                    modifier = Modifier
                        .size(innerCircleRadius * 2)
                        .offset {
                            IntOffset(
                                (scaledOffsetX * outerCircleWidth.toPx()).roundToInt(),
                                (scaledOffsetY * outerCircleWidth.toPx()).roundToInt(),
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    offsetX = 0f
                                    offsetY = 0f
                                    scaledOffsetX = 0f
                                    scaledOffsetY = 0f
                                    when (type) {
                                        AnalogStickType.LEFT -> {
                                            gamepadState.LeftThumbstickX = scaledOffsetX
                                            gamepadState.LeftThumbstickY = scaledOffsetY
                                        }

                                        AnalogStickType.RIGHT -> {
                                            gamepadState.RightThumbstickX = scaledOffsetX
                                            gamepadState.RightThumbstickY = scaledOffsetY
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                    // scale and clamp between -1 and 1
                                    scaledOffsetX = (offsetX / outerCircleWidth.toPx()).coerceIn(-1f, 1f)
                                    scaledOffsetY = (offsetY / outerCircleWidth.toPx()).coerceIn(-1f, 1f)
                                    when (type) {
                                        AnalogStickType.LEFT -> {
                                            gamepadState.LeftThumbstickX = scaledOffsetX
                                            gamepadState.LeftThumbstickY = scaledOffsetY
                                        }

                                        AnalogStickType.RIGHT -> {
                                            gamepadState.RightThumbstickX = scaledOffsetX
                                            gamepadState.RightThumbstickY = scaledOffsetY
                                        }
                                    }
                                }
                            )
                        },
//                    onClick = {},
                ) {

                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun AnalogStickPreview() {
    AnalogStick(
        gamepadState = GamepadReading(),
        type = AnalogStickType.LEFT,
    )
}
