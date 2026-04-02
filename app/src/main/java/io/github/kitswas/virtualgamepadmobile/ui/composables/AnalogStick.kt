package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.theme.lighten
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class AnalogStickType {
    LEFT, RIGHT
}

private class AnalogStickUiState {
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var visualX by mutableFloatStateOf(0f)
    var visualY by mutableFloatStateOf(0f)
    var isPressed by mutableStateOf(false)
}

private fun updateThumbstickValues(
    type: AnalogStickType,
    gamepadState: GamepadReading,
    x: Float,
    y: Float,
) {
    when (type) {
        AnalogStickType.LEFT -> {
            gamepadState.LeftThumbstickX = x
            gamepadState.LeftThumbstickY = y
        }

        AnalogStickType.RIGHT -> {
            gamepadState.RightThumbstickX = x
            gamepadState.RightThumbstickY = y
        }
    }
}

private fun thumbstickButtonMask(type: AnalogStickType): Int = when (type) {
    AnalogStickType.LEFT -> GameButtons.LeftThumbstick.value
    AnalogStickType.RIGHT -> GameButtons.RightThumbstick.value
}

private fun resetAnalogStick(
    state: AnalogStickUiState,
    type: AnalogStickType,
    gamepadState: GamepadReading,
) {
    state.offsetX = 0f
    state.offsetY = 0f
    state.visualX = 0f
    state.visualY = 0f
    updateThumbstickValues(type, gamepadState, 0f, 0f)
}

private fun handleAnalogDrag(
    state: AnalogStickUiState,
    dragX: Float,
    dragY: Float,
    maxOffset: Float,
    type: AnalogStickType,
    gamepadState: GamepadReading,
    view: android.view.View,
) {
    state.offsetX += dragX
    state.offsetY += dragY

    val magnitude = sqrt(state.offsetX * state.offsetX + state.offsetY * state.offsetY)
    val normalizedDistance = (magnitude / maxOffset).coerceIn(0f, 1f)
    if (normalizedDistance > 0.3f) {
        HapticUtils.performAnalogMovementFeedback(view, normalizedDistance)
    }

    if (magnitude <= 0f) {
        return
    }

    val scaleFactor = if (magnitude > maxOffset) maxOffset / magnitude else 1f
    state.visualX = state.offsetX * scaleFactor
    state.visualY = state.offsetY * scaleFactor
    updateThumbstickValues(type, gamepadState, state.visualX / maxOffset, state.visualY / maxOffset)
}

private fun toggleThumbstickButton(
    state: AnalogStickUiState,
    type: AnalogStickType,
    gamepadState: GamepadReading,
    view: android.view.View,
) {
    val mask = thumbstickButtonMask(type)
    if (!state.isPressed) {
        state.isPressed = true
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or mask
        HapticUtils.performButtonPressFeedback(view)
        return
    }

    state.isPressed = false
    gamepadState.ButtonsDown = gamepadState.ButtonsDown and mask.inv()
    gamepadState.ButtonsUp = gamepadState.ButtonsUp or mask
    HapticUtils.performGestureEndFeedback(view)
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
        modifier = modifier.testTag("AnalogStick_${type.name}"),
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
                val state = remember { AnalogStickUiState() }

                // Calculate maximum offset once
                val maxOffset = with(density) {
                    (innerCircleRadius + outerCircleWidth).toPx()
                }

                // Then draw the inner circle
                Circle(
                    colour = innerCircleColor,
                    modifier = Modifier
                        .testTag("AnalogStick_${type.name}_Handle")
                        .size(innerCircleRadius * 2)
                        .offset {
                            IntOffset(
                                state.visualX.roundToInt(),
                                state.visualY.roundToInt()
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    HapticUtils.performGestureStartFeedback(view)
                                },
                                onDragEnd = {
                                    resetAnalogStick(state, type, gamepadState)
                                    HapticUtils.performGestureEndFeedback(view)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    handleAnalogDrag(
                                        state = state,
                                        dragX = dragAmount.x,
                                        dragY = dragAmount.y,
                                        maxOffset = maxOffset,
                                        type = type,
                                        gamepadState = gamepadState,
                                        view = view,
                                    )
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { _ ->
                                    toggleThumbstickButton(state, type, gamepadState, view)
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
