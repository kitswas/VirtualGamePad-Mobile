package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.theme.faceButtonTextStyle
import io.github.kitswas.virtualgamepadmobile.ui.theme.lighten
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils


enum class FaceButtonType {
    A, B, X, Y
}

private val faceButtonColourMap = mapOf(
    FaceButtonType.A to Color(0xFF00FF00),
    FaceButtonType.B to Color(0xFFFF0000),
    FaceButtonType.X to Color(0xFF0055FF),
    FaceButtonType.Y to Color(0xFFFFFF00),
)

@Composable
fun FaceButton(
    type: FaceButtonType,
    modifier: Modifier = Modifier,
    foregroundColour: Color = lighten(faceButtonColourMap[type]!!, 0.2f),
    backgroundColour: Color = darken(faceButtonColourMap[type]!!, 0.8f),
    size: Dp,
    gamepadState: GamepadReading,
) {
    val view = LocalView.current
    val gameButton = when (type) {
        FaceButtonType.A -> GameButtons.A
        FaceButtonType.B -> GameButtons.B
        FaceButtonType.X -> GameButtons.X
        FaceButtonType.Y -> GameButtons.Y
    }
    val label = when (type) {
        FaceButtonType.A -> stringResource(R.string.button_a)
        FaceButtonType.B -> stringResource(R.string.button_b)
        FaceButtonType.X -> stringResource(R.string.button_x)
        FaceButtonType.Y -> stringResource(R.string.button_y)
    }

    // Registers this button's on-screen bounds with the shared MultiTouchController
    // (see TouchZoneController.kt) instead of using a per-button clickable/
    // interactionSource. A single controller resolving every finger against every
    // button's bounds is what allows several buttons to be held at once (up to
    // MAX_TRACKED_TOUCH_POINTS) and lets a finger "roll" from one button directly
    // into another without lifting off the screen.
    val isPressed = LocalMultiTouchController.current?.pressedZones?.get("face_${type.name}") ?: false

    val pressedBackground = lighten(backgroundColour, 0.18f)
    val animatedBackground by animateColorAsState(
        targetValue = if (isPressed) pressedBackground else backgroundColour,
        label = "faceButtonBackground",
    )

    Surface(
        modifier = modifier
            .size(size)
            .padding(0.dp)
            .touchZone(
                id = "face_${type.name}",
                onPress = {
                    Log.d("FaceButton ${type.name}", "Pressed")
                    HapticUtils.performButtonPressFeedback(view)
                    gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
                },
                onRelease = {
                    Log.d("FaceButton ${type.name}", "Released")
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
            Text(
                text = label,
                color = foregroundColour,
                textAlign = TextAlign.Center,
                style = faceButtonTextStyle(size),
            )
        }
    }
}

/**
 * The A, B, X, Y buttons on a gamepad, also known as the face buttons.
 */
@Composable
fun FaceButtons(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        FaceButton(
            type = FaceButtonType.A,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
        FaceButton(
            type = FaceButtonType.B,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
        FaceButton(
            type = FaceButtonType.X,
            modifier = Modifier.align(Alignment.CenterStart),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
        FaceButton(
            type = FaceButtonType.Y,
            modifier = Modifier.align(Alignment.TopCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun FaceButtonsPreview() {
    PreviewBase {
        FaceButtons(
            gamepadState = GamepadReading(),
        )
    }
}
