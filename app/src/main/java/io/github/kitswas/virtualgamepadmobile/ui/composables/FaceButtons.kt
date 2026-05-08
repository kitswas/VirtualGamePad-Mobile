package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    isPressed: Boolean = false,
) {
    val label = when (type) {
        FaceButtonType.A -> stringResource(R.string.button_a)
        FaceButtonType.B -> stringResource(R.string.button_b)
        FaceButtonType.X -> stringResource(R.string.button_x)
        FaceButtonType.Y -> stringResource(R.string.button_y)
    }

    OutlinedButton(
        modifier = modifier
            .size(size)
            .padding(0.dp),
        onClick = {},
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isPressed) foregroundColour else backgroundColour,
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(
            text = label,
            color = if (isPressed) backgroundColour else foregroundColour,
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
    val buttonSize = 2 * size / 5
    DirectionalButtons(
        modifier = modifier,
        size = size,
        gamepadState = gamepadState,
        top = DirectionalButtonConfig(GameButtons.Y) { isPressed ->
            FaceButton(FaceButtonType.Y, size = buttonSize, isPressed = isPressed)
        },
        bottom = DirectionalButtonConfig(GameButtons.A) { isPressed ->
            FaceButton(FaceButtonType.A, size = buttonSize, isPressed = isPressed)
        },
        left = DirectionalButtonConfig(GameButtons.X) { isPressed ->
            FaceButton(FaceButtonType.X, size = buttonSize, isPressed = isPressed)
        },
        right = DirectionalButtonConfig(GameButtons.B) { isPressed ->
            FaceButton(FaceButtonType.B, size = buttonSize, isPressed = isPressed)
        }
    )
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
