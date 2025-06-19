package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.theme.lighten
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class AnalogStickType {
    LEFT, RIGHT
}

/**
 * Converts a circular position (x,y with radius=1) to square coordinates
 * This allows diagonal movement to reach (1,1) instead of (0.71,0.71)
 */
private fun circleToSquare(x: Float, y: Float): Pair<Float, Float> {
    // Fast path for common cases
    if (x == 0f && y == 0f) return Pair(0f, 0f)

    val magnitude = sqrt(x * x + y * y)
    if (magnitude == 0f) return Pair(0f, 0f)

    // Normalize coordinates
    val nx = x / magnitude
    val ny = y / magnitude

    // Calculate scaling factor - simplified to avoid multiple abs calls
    val scale = if (abs(nx) > abs(ny)) 1f / abs(nx) else 1f / abs(ny)

    // Apply magnitude limits and return
    val clampedMagnitude = min(magnitude, 1f)
    return Pair(
        nx * scale * clampedMagnitude,
        ny * scale * clampedMagnitude
    )
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
    val density = LocalDensity.current
    val view = LocalView.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
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
                // Raw offset values (more efficient than using a data class)
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                // Calculate maximum offset once
                val maxOffset = with(density) {
                    (innerCircleRadius + outerCircleWidth).toPx()
                }

                // Then draw the inner circle
                Circle(
                    colour = innerCircleColor,
                    modifier = Modifier
                        .size(innerCircleRadius * 2)
                        .offset {
                            val magnitude = sqrt(offsetX * offsetX + offsetY * offsetY)
                            val scaleFactor =
                                if (magnitude > maxOffset) maxOffset / magnitude else 1f
                            val visualX = offsetX * scaleFactor
                            val visualY = offsetY * scaleFactor

                            IntOffset(
                                visualX.roundToInt(),
                                visualY.roundToInt()
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    // Reset position
                                    offsetX = 0f
                                    offsetY = 0f

                                    // Update gamepad state
                                    when (type) {
                                        AnalogStickType.LEFT -> {
                                            gamepadState.LeftThumbstickX = 0f
                                            gamepadState.LeftThumbstickY = 0f
                                        }

                                        AnalogStickType.RIGHT -> {
                                            gamepadState.RightThumbstickX = 0f
                                            gamepadState.RightThumbstickY = 0f
                                        }
                                    }
                                    HapticUtils.performGestureEndFeedback(view)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()

                                    // Update raw position
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y

                                    // Calculate magnitude for normalized position
                                    val magnitude = sqrt(offsetX * offsetX + offsetY * offsetY)
                                    // Calculate normalized distance (0-1 range) for haptic intensity
                                    val normalizedDistance =
                                        (magnitude / maxOffset).coerceIn(0f, 1f)

                                    if (normalizedDistance > 0.3f) {
                                        // Subtle movement feedback for better tactile experience
                                        HapticUtils.performAnalogMovementFeedback(
                                            view,
                                            normalizedDistance
                                        )
                                    }

                                    // Only update UI when magnitude > 0
                                    if (magnitude > 0f) {
                                        // Normalize with max offset
                                        val normalizedX = (offsetX / maxOffset).coerceIn(-1f, 1f)
                                        val normalizedY = (offsetY / maxOffset).coerceIn(-1f, 1f)

                                        // Calculate circle-to-square mapping only when updating gamepad state
                                        val (squareX, squareY) = circleToSquare(
                                            normalizedX,
                                            normalizedY
                                        )

                                        // Update gamepad state
                                        when (type) {
                                            AnalogStickType.LEFT -> {
                                                gamepadState.LeftThumbstickX = squareX
                                                gamepadState.LeftThumbstickY = squareY
                                            }

                                            AnalogStickType.RIGHT -> {
                                                gamepadState.RightThumbstickX = squareX
                                                gamepadState.RightThumbstickY = squareY
                                            }
                                        }
                                    }
                                }
                            )
                        }
                ) {}
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
