package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils

enum class DpadButtonType {
    UP, DOWN, LEFT, RIGHT
}

@Composable
fun DpadButton(
    type: DpadButtonType,
    modifier: Modifier = Modifier,
    foregroundColour: Color = MaterialTheme.colorScheme.primary,
    backgroundColour: Color = darken(MaterialTheme.colorScheme.primary, 0.8f),
    size: Dp,
    gamepadState: GamepadReading,
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
    // See TouchZoneController.kt: a shared MultiTouchController resolves every
    // finger against every D-pad direction's bounds, instead of each direction
    // using its own clickable. This allows e.g. UP and RIGHT to be held at the
    // same time by different fingers, and lets a finger slide from one
    // direction straight into an adjacent one without lifting.
    val isPressed = LocalMultiTouchController.current?.pressedZones?.get("dpad_${type.name}") ?: false

    val pressedBackground = lerp(backgroundColour, foregroundColour, 0.35f)
    val animatedBackground by animateColorAsState(
        targetValue = if (isPressed) pressedBackground else backgroundColour,
        label = "dpadButtonBackground",
    )

    Surface(
        modifier = modifier
            .size(size)
            .padding(0.dp)
            .touchZone(
                id = "dpad_${type.name}",
                onPress = {
                    Log.d("DPadButton ${type.name}", "Pressed")
                    HapticUtils.performButtonPressFeedback(view)
                    gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
                },
                onRelease = {
                    Log.d("DPadButton ${type.name}", "Released")
                    HapticUtils.performButtonReleaseFeedback(view)
                    gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                    gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
                },
            ),
        shape = MaterialTheme.shapes.small,
        color = animatedBackground,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Icon(
                painter = painterResource(R.drawable.ic_play_arrow),
                contentDescription = stringResource(R.string.content_desc_dpad_button, type.name),
                modifier = Modifier
                    .rotate(rotation)
                    .size(size),
                tint = foregroundColour
            )
        }
    }
}

/**
 * A directional pad with up, down, left, and right buttons.
 */
@Composable
fun Dpad(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
) {
    val buttonSize = 2 * size / 5
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        DpadButton(
            type = DpadButtonType.UP,
            modifier = Modifier.align(Alignment.TopCenter),
            size = buttonSize,
            gamepadState = gamepadState,
        )
        DpadButton(
            type = DpadButtonType.DOWN,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = buttonSize,
            gamepadState = gamepadState,
        )
        DpadButton(
            type = DpadButtonType.LEFT,
            modifier = Modifier.align(Alignment.CenterStart),
            size = buttonSize,
            gamepadState = gamepadState,
        )
        DpadButton(
            type = DpadButtonType.RIGHT,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = buttonSize,
            gamepadState = gamepadState,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun DpadPreview() {
    Dpad(
        gamepadState = GamepadReading(),
    )
}
