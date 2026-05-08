package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils
import kotlin.math.sqrt

/**
 * Configuration for a single button within the [DirectionalButtons] group.
 * @param gameButton The associated [GameButtons] enum value.
 * @param content Composable that renders the button visuals based on its pressed state.
 */
data class DirectionalButtonConfig(
    val gameButton: GameButtons,
    val content: @Composable (isPressed: Boolean) -> Unit
)

/**
 * A unified component for Dpad and FaceButtons that handles sliding gestures
 * and simultaneous activation by pressing the gaps between buttons.
 */
@Composable
fun DirectionalButtons(
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    top: DirectionalButtonConfig,
    bottom: DirectionalButtonConfig,
    left: DirectionalButtonConfig,
    right: DirectionalButtonConfig,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    
    // Calculate layout dimensions in pixels
    val sizePx = with(density) { size.toPx() }
    val buttonSizePx = 2 * sizePx / 5
    val halfSize = sizePx / 2f
    val buttonRadiusPx = buttonSizePx / 2f
    
    // Calculate centers for hit detection
    val centers = remember(sizePx, buttonSizePx) {
        mapOf(
            top.gameButton to Offset(halfSize, buttonRadiusPx),
            bottom.gameButton to Offset(halfSize, sizePx - buttonRadiusPx),
            left.gameButton to Offset(buttonRadiusPx, halfSize),
            right.gameButton to Offset(sizePx - buttonRadiusPx, halfSize)
        )
    }

    // Hit radius is expanded to allow gap-pressing (1.5x visual radius)
    val hitRadius = buttonRadiusPx * 1.5f

    // Tracks which buttons are pressed by each unique pointer (finger)
    val pressedButtonsByPointer = remember { mutableStateMapOf<PointerId, Set<GameButtons>>() }

    // Aggregate of all currently pressed buttons across all pointers
    val currentPressedButtons = remember(pressedButtonsByPointer.size, pressedButtonsByPointer.values.sumOf { it.size }) {
        pressedButtonsByPointer.values.flatten().toSet()
    }

    // Synchronize the local pressed state with the global gamepadState and trigger haptics
    LaunchedEffect(currentPressedButtons) {
        val allButtons = listOf(top.gameButton, bottom.gameButton, left.gameButton, right.gameButton)
        allButtons.forEach { button ->
            val isDown = button in currentPressedButtons
            val wasDown = (gamepadState.ButtonsDown and button.value) != 0
            
            if (isDown && !wasDown) {
                Log.d("DirectionalButtons", "Pressed ${button.name}")
                HapticUtils.performButtonPressFeedback(view)
                gamepadState.ButtonsDown = gamepadState.ButtonsDown or button.value
            } else if (!isDown && wasDown) {
                Log.d("DirectionalButtons", "Released ${button.name}")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and button.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or button.value
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                val pointerPos = change.position
                                // Determine which buttons this pointer is currently over
                                val pressed = centers.filter { (_, center) ->
                                    val dist = sqrt(
                                        (pointerPos.x - center.x) * (pointerPos.x - center.x) +
                                        (pointerPos.y - center.y) * (pointerPos.y - center.y)
                                    )
                                    dist <= hitRadius
                                }.keys
                                pressedButtonsByPointer[change.id] = pressed
                            } else {
                                // Pointer released
                                pressedButtonsByPointer.remove(change.id)
                            }
                        }
                    }
                }
            }
    ) {
        // Visual buttons are placed according to the original layout logic
        Box(modifier = Modifier.align(Alignment.TopCenter)) { 
            top.content(top.gameButton in currentPressedButtons) 
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) { 
            bottom.content(bottom.gameButton in currentPressedButtons) 
        }
        Box(modifier = Modifier.align(Alignment.CenterStart)) { 
            left.content(left.gameButton in currentPressedButtons) 
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) { 
            right.content(right.gameButton in currentPressedButtons) 
        }
    }
}
