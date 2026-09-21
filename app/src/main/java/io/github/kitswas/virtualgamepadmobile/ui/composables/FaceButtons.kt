package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.PreviewBase
import io.github.kitswas.virtualgamepadmobile.ui.theme.faceButtonTextStyle
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils

enum class FaceButtonType {
    A, B, X, Y
}

// Flat, highly vibrant colors tailored for a dark 2D HUD aesthetic
private val faceButtonColourMap = mapOf(
    FaceButtonType.A to Color(0xFF00FF66), // Vibrant Neon Green
    FaceButtonType.B to Color(0xFFFF003C), // Vibrant Neon Red
    FaceButtonType.X to Color(0xFF00E5FF), // Vibrant Cyan
    FaceButtonType.Y to Color(0xFFFFD600), // Vibrant Yellow
)

@Composable
fun FaceButton(
    type: FaceButtonType,
    modifier: Modifier = Modifier,
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // --- Original Logic Intact ---
    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        Log.d("FaceButton ${type.name}", "Pressed")
        HapticUtils.performButtonPressFeedback(view)
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        //Use if + DisposableEffect to wait for the press action is completed
        DisposableEffect(Unit) {
            onDispose {
                Log.d("FaceButton ${type.name}", "Released")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    // --- 2D HUD Aesthetic Properties ---
    val baseColor = faceButtonColourMap[type]!!
    val darkSurfaceColor = Color(0xFF1E1E24) // Flat dark background

    // Animations: Invert colors and scale down slightly when pressed
    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) baseColor else darkSurfaceColor,
        label = "bgColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isPressed) darkSurfaceColor else baseColor,
        label = "textColor"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .clip(CircleShape)
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (isPressed) baseColor else baseColor.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disables standard Android ripple for a clean HUD look
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = animatedTextColor,
            textAlign = TextAlign.Center,
            style = faceButtonTextStyle(size),
        )
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
            size = size * 2 / 5, // Flipped the order here
            gamepadState = gamepadState,
        )
        FaceButton(
            type = FaceButtonType.B,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = size * 2 / 5, // Flipped the order here
            gamepadState = gamepadState,
        )
        FaceButton(
            type = FaceButtonType.X,
            modifier = Modifier.align(Alignment.CenterStart),
            size = size * 2 / 5, // Flipped the order here
            gamepadState = gamepadState,
        )
        FaceButton(
            type = FaceButtonType.Y,
            modifier = Modifier.align(Alignment.TopCenter),
            size = size * 2 / 5, // Flipped the order here
            gamepadState = gamepadState,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun FaceButtonsPreview() {
    PreviewBase {
        FaceButtons(
            gamepadState = GamepadReading(),
        )
    }
}
