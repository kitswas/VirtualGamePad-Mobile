package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils

enum class DpadButtonType {
    UP, DOWN, LEFT, RIGHT
}

@Composable
fun DpadButton(
    type: DpadButtonType,
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    isExternallyPressed: Boolean = false // NEW: Allows diagonal hitboxes to trigger visuals
) {
    val view = LocalView.current
    
    val rotation = when (type) {
        DpadButtonType.UP -> -90f
        DpadButtonType.DOWN -> 90f
        DpadButtonType.LEFT -> 180f
        DpadButtonType.RIGHT -> 0f
    }
    
    val gameButton = when (type) {
        DpadButtonType.UP -> GameButtons.DPadUp
        DpadButtonType.DOWN -> GameButtons.DPadDown
        DpadButtonType.LEFT -> GameButtons.DPadLeft
        DpadButtonType.RIGHT -> GameButtons.DPadRight
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isDirectlyPressed by interactionSource.collectIsPressedAsState()

    // Combine direct touches with diagonal touches for the visual animations
    val isVisuallyPressed = isDirectlyPressed || isExternallyPressed

    // Physical Hardware Logic (Only fires if you directly touch THIS specific button)
    if (isDirectlyPressed) {
        Log.d("DPadButton ${type.name}", "Pressed")
        HapticUtils.performButtonPressFeedback(view)
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        DisposableEffect(Unit) {
            onDispose {
                Log.d("DPadButton ${type.name}", "Released")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    // --- 2D HUD Aesthetic Properties ---
    val baseColor = Color(0xFFF5F5F5) 
    val darkSurfaceColor = Color(0xFF1E1E24)

    // Visual Animations (Reacts to BOTH direct touches and diagonal external presses)
    val animatedBgColor by animateColorAsState(
        targetValue = if (isVisuallyPressed) baseColor else darkSurfaceColor,
        label = "bgColor"
    )
    val animatedIconColor by animateColorAsState(
        targetValue = if (isVisuallyPressed) darkSurfaceColor else baseColor,
        label = "iconColor"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isVisuallyPressed) 0.85f else 1f,
        label = "buttonScale"
    )

    val armShape = when (type) {
        DpadButtonType.UP -> RoundedCornerShape(topStartPercent = 30, topEndPercent = 30, bottomStartPercent = 0, bottomEndPercent = 0)
        DpadButtonType.DOWN -> RoundedCornerShape(bottomStartPercent = 30, bottomEndPercent = 30, topStartPercent = 0, topEndPercent = 0)
        DpadButtonType.LEFT -> RoundedCornerShape(topStartPercent = 30, bottomStartPercent = 30, topEndPercent = 0, bottomEndPercent = 0)
        DpadButtonType.RIGHT -> RoundedCornerShape(topEndPercent = 30, bottomEndPercent = 30, topStartPercent = 0, bottomStartPercent = 0)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .clip(armShape)
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (isVisuallyPressed) baseColor else baseColor.copy(alpha = 0.2f),
                shape = armShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play_arrow),
            contentDescription = stringResource(R.string.content_desc_dpad_button, type.name),
            modifier = Modifier
                .rotate(rotation)
                .size(size * 0.6f),
            tint = animatedIconColor
        )
    }
}

@Composable
fun DiagonalHitbox(
    type1: DpadButtonType,
    type2: DpadButtonType,
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
    onPressedChange: (Boolean) -> Unit // NEW: Reports state up to parent
) {
    val view = LocalView.current
    
    val gameButton1 = when (type1) {
        DpadButtonType.UP -> GameButtons.DPadUp
        DpadButtonType.DOWN -> GameButtons.DPadDown
        DpadButtonType.LEFT -> GameButtons.DPadLeft
        DpadButtonType.RIGHT -> GameButtons.DPadRight
    }
    
    val gameButton2 = when (type2) {
        DpadButtonType.UP -> GameButtons.DPadUp
        DpadButtonType.DOWN -> GameButtons.DPadDown
        DpadButtonType.LEFT -> GameButtons.DPadLeft
        DpadButtonType.RIGHT -> GameButtons.DPadRight
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Notify parent to trigger visual updates on the arms
    LaunchedEffect(isPressed) {
        onPressedChange(isPressed)
    }

    if (isPressed) {
        Log.d("DPadDiagonal", "Pressed ${type1.name} + ${type2.name}")
        HapticUtils.performButtonPressFeedback(view)
        
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton1.value or gameButton2.value
        
        DisposableEffect(Unit) {
            onDispose {
                Log.d("DPadDiagonal", "Released ${type1.name} + ${type2.name}")
                HapticUtils.performButtonReleaseFeedback(view)
                
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton1.value.inv() and gameButton2.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton1.value or gameButton2.value
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = {}
            )
    )
}

/**
 * A directional pad with up, down, left, right, and 4 invisible diagonal hitboxes.
 */
@Composable
fun Dpad(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
) {
    val buttonSize = size / 3 
    
    // State Hoisting: Track which diagonal corners are being pressed
    var upLeftPressed by remember { mutableStateOf(false) }
    var upRightPressed by remember { mutableStateOf(false) }
    var downLeftPressed by remember { mutableStateOf(false) }
    var downRightPressed by remember { mutableStateOf(false) }

    // Logic to determine if an arm should animate based on adjacent corners
    val isUpExternallyPressed = upLeftPressed || upRightPressed
    val isDownExternallyPressed = downLeftPressed || downRightPressed
    val isLeftExternallyPressed = upLeftPressed || downLeftPressed
    val isRightExternallyPressed = upRightPressed || downRightPressed
    
    Box(
        modifier = modifier.size(size), 
        contentAlignment = Alignment.Center
    ) {
        // --- 1. CENTER PIVOT ---
        Box(
            modifier = Modifier
                .size(buttonSize)
                .background(Color(0xFF1E1E24))
                .border(
                    width = 1.dp,
                    color = Color(0xFFF5F5F5).copy(alpha = 0.2f)
                )
        )

        // --- 2. THE 4 VISIBLE DIRECTIONAL ARMS ---
        DpadButton(
            type = DpadButtonType.UP,
            modifier = Modifier.align(Alignment.TopCenter),
            size = buttonSize,
            gamepadState = gamepadState,
            isExternallyPressed = isUpExternallyPressed
        )
        DpadButton(
            type = DpadButtonType.DOWN,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = buttonSize,
            gamepadState = gamepadState,
            isExternallyPressed = isDownExternallyPressed
        )
        DpadButton(
            type = DpadButtonType.LEFT,
            modifier = Modifier.align(Alignment.CenterStart),
            size = buttonSize,
            gamepadState = gamepadState,
            isExternallyPressed = isLeftExternallyPressed
        )
        DpadButton(
            type = DpadButtonType.RIGHT,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = buttonSize,
            gamepadState = gamepadState,
            isExternallyPressed = isRightExternallyPressed
        )

        // --- 3. THE 4 INVISIBLE DIAGONAL HITBOXES ---
        DiagonalHitbox(
            type1 = DpadButtonType.UP,
            type2 = DpadButtonType.LEFT,
            modifier = Modifier.align(Alignment.TopStart),
            size = buttonSize,
            gamepadState = gamepadState,
            onPressedChange = { upLeftPressed = it }
        )
        DiagonalHitbox(
            type1 = DpadButtonType.UP,
            type2 = DpadButtonType.RIGHT,
            modifier = Modifier.align(Alignment.TopEnd),
            size = buttonSize,
            gamepadState = gamepadState,
            onPressedChange = { upRightPressed = it }
        )
        DiagonalHitbox(
            type1 = DpadButtonType.DOWN,
            type2 = DpadButtonType.LEFT,
            modifier = Modifier.align(Alignment.BottomStart),
            size = buttonSize,
            gamepadState = gamepadState,
            onPressedChange = { downLeftPressed = it }
        )
        DiagonalHitbox(
            type1 = DpadButtonType.DOWN,
            type2 = DpadButtonType.RIGHT,
            modifier = Modifier.align(Alignment.BottomEnd),
            size = buttonSize,
            gamepadState = gamepadState,
            onPressedChange = { downRightPressed = it }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun DpadPreview() {
    Dpad(
        gamepadState = GamepadReading(),
    )
}
