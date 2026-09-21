package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
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
import io.github.kitswas.virtualgamepadmobile.data.StickResponseMode
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils
import kotlin.math.abs
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
    var hasExitedDeadzone by mutableStateOf(false)
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
    state.hasExitedDeadzone = false
    updateThumbstickValues(type, gamepadState, 0f, 0f)
}

private fun applyAnalogOffset(
    state: AnalogStickUiState,
    offsetX: Float,
    offsetY: Float,
    maxOffset: Float,
    type: AnalogStickType,
    gamepadState: GamepadReading,
    view: android.view.View,
    responseMode: StickResponseMode,
) {
    state.offsetX = offsetX
    state.offsetY = offsetY

    val magnitude = sqrt(offsetX * offsetX + offsetY * offsetY)
    val normalizedDistance = (magnitude / maxOffset).coerceIn(0f, 1f)

    // Minimalist Haptics: Only trigger once when crossing the 15% deadzone threshold
    if (normalizedDistance > 0.15f && !state.hasExitedDeadzone) {
        HapticUtils.performButtonPressFeedback(view)
        state.hasExitedDeadzone = true
    } else if (normalizedDistance <= 0.15f) {
        state.hasExitedDeadzone = false
    }

    if (magnitude <= 0f) {
        state.visualX = 0f
        state.visualY = 0f
        updateThumbstickValues(type, gamepadState, 0f, 0f)
        return
    }

    val scaleFactor = if (magnitude > maxOffset) maxOffset / magnitude else 1f
    state.visualX = offsetX * scaleFactor
    state.visualY = offsetY * scaleFactor

    // Apply the selected response curve to the magnitude, then scale both axes uniformly
    // so direction is preserved while output magnitude is shaped by the response mode.
    val normX = state.visualX / maxOffset
    val normY = state.visualY / maxOffset
    val magnitudeNorm = sqrt(normX * normX + normY * normY)
    val outputMagnitude = responseMode.map(magnitudeNorm)
    val outputRatio = if (magnitudeNorm > 0f) outputMagnitude / magnitudeNorm else 0f

    updateThumbstickValues(type, gamepadState, normX * outputRatio, normY * outputRatio)
}

private fun handleAnalogDrag(
    state: AnalogStickUiState,
    dragX: Float,
    dragY: Float,
    maxOffset: Float,
    type: AnalogStickType,
    gamepadState: GamepadReading,
    view: android.view.View,
    responseMode: StickResponseMode,
) {
    applyAnalogOffset(
        state = state,
        offsetX = state.offsetX + dragX,
        offsetY = state.offsetY + dragY,
        maxOffset = maxOffset,
        type = type,
        gamepadState = gamepadState,
        view = view,
        responseMode = responseMode,
    )
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

/**
 * Single gesture handler used for the whole-area analog stick in game mode.
 *
 * Unlike `detectDragGestures` combined with `detectTapGestures` (which fight over the
 * same events because they each hook the pointer stream independently) this handler owns
 * the entire gesture: it grabs on all downs across the whole ring, keeps a long-press
 * timer running for the L3/R3 click, and only consumes pointer changes once the finger
 * has actually dragged past touch-slop.
 */
private suspend fun PointerInputScope.detectStickGesture(
    onGrabStart: (x: Float, y: Float) -> Unit,
    onAbsoluteMove: (x: Float, y: Float) -> Unit,
    onDragBy: (dx: Float, dy: Float) -> Unit,
    onLongPress: () -> Unit,
    onEnd: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downId = down.id
        onGrabStart(down.position.x, down.position.y)

        var previousX = down.position.x
        var previousY = down.position.y
        var movedOverSlop = false
        val slop = viewConfiguration.touchSlop

        // Wait for either a drag past the slop radius or the long-press timer to
        // expire. `withTimeoutOrNull` is a member of this restricted scope whose
        // block re-receives it, so awaitPointerEvent() stays legal inside: no
        // nested coroutineScope/launch required. It returns `true` when a drag
        // won, `false` when the finger lifted first, and `null` on timeout.
        val dragStarted = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            var crossedSlop = false
            while (!crossedSlop) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == downId } ?: break
                if (!change.pressed) {
                    change.consume()
                    break
                }
                val x = change.position.x
                val y = change.position.y
                val dx = x - down.position.x
                val dy = y - down.position.y
                if (sqrt(dx * dx + dy * dy) > slop) {
                    // Dragging won: snap the knob directly under the current finger
                    // position (not an incremental delta, which would leave it behind
                    // by the slop-tracking distance), then keep tracking incrementally.
                    movedOverSlop = true
                    onAbsoluteMove(x, y)
                    previousX = x
                    previousY = y
                    return@withTimeoutOrNull true
                } else {
                    previousX = x
                    previousY = y
                }
            }
            // Reaching here means the finger lifted before either event (`false`).
            false
        }

        // `null` means the timer expired while the finger stayed inside the slop
        // radius: that's the L3/R3 click. Everything else ends the gesture.
        if (dragStarted == null) {
            onLongPress()
            movedOverSlop = true
        }

        // If the gesture is still alive (drag won, or long-press just fired),
        // keep feeding movement until the finger lifts.
        if (dragStarted == null || dragStarted == true) {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == downId } ?: break
                if (!change.pressed) {
                    change.consume()
                    break
                }
                if (movedOverSlop) {
                    change.consume()
                    onDragBy(change.position.x - previousX, change.position.y - previousY)
                }
                previousX = change.position.x
                previousY = change.position.y
            }
        }

        onEnd()
    }
}

@Composable
fun AnalogStick(
    modifier: Modifier = Modifier,
    // Repurposed your existing parameters to match the new 2D HUD style without breaking API
    ringColor: Color = Color.White.copy(alpha = 0.28f),
    ringWidth: Dp = 2.dp,
    outerCircleColor: Color = Color.White.copy(alpha = 0.06f),
    outerCircleWidth: Dp = 4.dp, // Still used for sizing calculations
    innerCircleColor: Color = Color.White, // Flat, high-contrast inner thumbstick
    innerCircleRadius: Dp = 32.dp, // Radius of the ring/base (drives footprint + travel)
    knobScale: Float = 1f, // Scales the knob only, leaving the ring and travel untouched
    gamepadState: GamepadReading,
    type: AnalogStickType,
    responseMode: StickResponseMode = StickResponseMode.default,
    // Grab the whole ring instead of just the knob (disabled in the editor so the
    // component drag overlay keeps working).
    wholeAreaInteractive: Boolean = false,
    // Gyroscope control for this stick.
    gyroOn: Boolean = false,
    gyroX: Float = 0f,
    gyroY: Float = 0f,
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val state = remember { AnalogStickUiState() }

    // Calculate maximum offset once
    val maxOffset = with(density) {
        (innerCircleRadius + outerCircleWidth).toPx()
    }
    val componentSizePx = with(density) {
        (innerCircleRadius + outerCircleWidth + ringWidth).toPx()
    }
    val innerRadiusPx = with(density) {
        (innerCircleRadius * knobScale).toPx()
    }

    // 2D Animation States
    val gyroActive = gyroOn && abs(gyroX) + abs(gyroY) > 0.15f
    val isActive = state.isPressed || state.hasExitedDeadzone || gyroActive
    val stickScale by animateFloatAsState(targetValue = if (isActive) 0.9f else 1f, label = "stickScale")
    val stickAlpha by animateFloatAsState(targetValue = if (isActive) 1f else 0.65f, label = "stickAlpha")

    // Smooth knob position, driven by touch when interactive and by the gyro otherwise.
    // Touch movement is applied directly (no animation) so the knob tracks the finger
    // exactly; only the gyro keeps a spring smoothing so tilt turns feel fluid.
    val targetVisualX = if (gyroOn) gyroX * maxOffset else state.visualX
    val targetVisualY = if (gyroOn) gyroY * maxOffset else state.visualY
    val visualAnimationSpec: androidx.compose.animation.core.AnimationSpec<Float> = if (gyroOn) {
        spring(dampingRatio = 0.4f, stiffness = 300f)
    } else {
        tween(durationMillis = 0)
    }
    val visualX by animateFloatAsState(targetValue = targetVisualX, animationSpec = visualAnimationSpec, label = "stickVisualX")
    val visualY by animateFloatAsState(targetValue = targetVisualY, animationSpec = visualAnimationSpec, label = "stickVisualY")

    // Whole-area interactive gesture (game mode): owns grab + drag + long-press so the
    // inner knob is just as grabbable as the outer ring.
    val wholeAreaGestureModifier: Modifier = if (wholeAreaInteractive && !gyroOn) {
        Modifier.pointerInput(Unit) {
            detectStickGesture(
                // A plain press must NEVER move the knob: only actual drags past
                // touch-slop reposition it (via onAbsoluteMove/onDragBy below).
                onGrabStart = { _, _ -> },
                onAbsoluteMove = { posX, posY ->
                    val dx = posX - componentSizePx / 2f
                    val dy = posY - componentSizePx / 2f
                    val magnitude = sqrt(dx * dx + dy * dy)
                    val clamp = if (magnitude > maxOffset) maxOffset / magnitude else 1f
                    applyAnalogOffset(
                        state = state,
                        offsetX = dx * clamp,
                        offsetY = dy * clamp,
                        maxOffset = maxOffset,
                        type = type,
                        gamepadState = gamepadState,
                        view = view,
                        responseMode = responseMode,
                    )
                },
                onDragBy = { dragX, dragY ->
                    handleAnalogDrag(
                        state = state,
                        dragX = dragX,
                        dragY = dragY,
                        maxOffset = maxOffset,
                        type = type,
                        gamepadState = gamepadState,
                        view = view,
                        responseMode = responseMode,
                    )
                },
                onLongPress = {
                    toggleThumbstickButton(state, type, gamepadState, view)
                },
                onEnd = {
                    resetAnalogStick(state, type, gamepadState)
                    HapticUtils.performGestureEndFeedback(view)
                }
            )
        }
    } else {
        Modifier
    }

    // Knob drag: only used when the ring is not interactive (editor preview)
    val knobDragModifier: Modifier = if (!wholeAreaInteractive && !gyroOn) {
        Modifier.pointerInput(Unit) {
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
                        responseMode = responseMode,
                    )
                }
            )
        }
    } else {
        Modifier
    }

    // L3/R3 click: only on the knob when the whole-area gesture is not active
    // (editor preview, or gyro-driven sticks), so it never steals the down event
    // from the ring's drag handler.
    val knobTapModifier: Modifier = if (!wholeAreaInteractive || gyroOn) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { _ ->
                    toggleThumbstickButton(state, type, gamepadState, view)
                }
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier.testTag("AnalogStick_${type.name}").then(wholeAreaGestureModifier),
        contentAlignment = Alignment.Center
    ) {
        // Flat, HUD-style outer boundary ring (replaces nested filled circles)
        Box(
            modifier = Modifier
                .size((innerCircleRadius + outerCircleWidth + ringWidth) * 2)
                .border(width = ringWidth, color = ringColor, shape = CircleShape)
                .background(outerCircleColor, CircleShape)
        )

        // Flat, vibrant inner thumbstick
        Box(
            modifier = Modifier
                .testTag("AnalogStick_${type.name}_Handle")
                .size((innerCircleRadius * knobScale) * 2)
                .offset {
                    IntOffset(
                        visualX.roundToInt(),
                        visualY.roundToInt()
                    )
                }
                .graphicsLayer {
                    scaleX = stickScale
                    scaleY = stickScale
                    alpha = stickAlpha
                }
                .then(knobDragModifier)
                .then(knobTapModifier)
                .background(color = innerCircleColor, shape = CircleShape)
                .border(
                    width = 1.dp,
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun AnalogStickPreview() {
    AnalogStick(
        gamepadState = GamepadReading(),
        type = AnalogStickType.LEFT,
    )
}