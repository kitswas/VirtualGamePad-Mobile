package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    outerCircleColor: Color = darken(MaterialTheme.colorScheme.primary, 0.8f),
    outerCircleWidth: Dp = 4.dp,
    innerCircleColor: Color = lighten(MaterialTheme.colorScheme.primary, 0.2f),
    innerCircleRadius: Dp = 32.dp,
    gamepadState: GamepadReading,
    type: AnalogStickType,
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        // First draw the glow ring
        Circle(
            colour = ringColor,
            modifier = Modifier.size((innerCircleRadius + outerCircleWidth + ringWidth) * 2),
            contentAlignment = Alignment.Center
        ) {
            // Then draw the outer circle
            Circle(
                modifier = Modifier.size((innerCircleRadius + outerCircleWidth) * 2),
                contentAlignment = Alignment.Center,
                colour = outerCircleColor,
            ) {
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }
                var visualOffsetX by remember { mutableFloatStateOf(0f) }
                var visualOffsetY by remember { mutableFloatStateOf(0f) }
                var scaledOffsetX: Byte by remember { mutableStateOf(0) }
                var scaledOffsetY: Byte by remember { mutableStateOf(0) }

                // Then draw the inner circle
                Circle(
                    colour = innerCircleColor,
                    modifier = Modifier
                        .size(innerCircleRadius * 2)
                        .offset {
                            IntOffset(
                                (visualOffsetX * outerCircleWidth.toPx()).roundToInt(),
                                (visualOffsetY * outerCircleWidth.toPx()).roundToInt(),
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(onDragEnd = {
                                offsetX = 0f
                                offsetY = 0f
                                visualOffsetX = 0f
                                visualOffsetY = 0f
                                scaledOffsetX = 0
                                scaledOffsetY = 0
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
                            }, onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                                // Calculate visual offset for display (between -1 and 1)
                                visualOffsetX = (offsetX / outerCircleWidth.toPx()).coerceIn(-1f, 1f)
                                visualOffsetY = (offsetY / outerCircleWidth.toPx()).coerceIn(-1f, 1f)

                                // scale and clamp between -1 and 1, and shift to [0, 200]
                                scaledOffsetX = ((visualOffsetX * 100).toInt() + 100).toByte()
                                scaledOffsetY = ((visualOffsetY * 100).toInt() + 100).toByte()
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
                            })
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
