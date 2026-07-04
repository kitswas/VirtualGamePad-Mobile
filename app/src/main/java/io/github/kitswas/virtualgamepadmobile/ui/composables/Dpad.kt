package io.github.kitswas.virtualgamepadmobile.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
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
    isPressed: Boolean = false,
) {
    val rotation = when (type) {
        DpadButtonType.UP -> -90f
        DpadButtonType.DOWN -> 90f
        DpadButtonType.LEFT -> 180f
        DpadButtonType.RIGHT -> 0f
    }

    OutlinedIconButton(
        modifier = modifier
            .size(size)
            .padding(0.dp),
        onClick = {},
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = if (isPressed) foregroundColour else backgroundColour,
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play_arrow),
            contentDescription = stringResource(R.string.content_desc_dpad_button, type.name),
            modifier = Modifier
                .rotate(rotation)
                .size(size),
            tint = if (isPressed) backgroundColour else foregroundColour
        )
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
    allowMultipress: Boolean = false,
) {
    val buttonSize = 2 * size / 5
    DirectionalButtons(
        modifier = modifier,
        size = size,
        gamepadState = gamepadState,
        allowMultipress = allowMultipress,
        top = DirectionalButtonConfig(GameButtons.DPadUp) { isPressed ->
            DpadButton(DpadButtonType.UP, size = buttonSize, isPressed = isPressed)
        },
        bottom = DirectionalButtonConfig(GameButtons.DPadDown) { isPressed ->
            DpadButton(DpadButtonType.DOWN, size = buttonSize, isPressed = isPressed)
        },
        left = DirectionalButtonConfig(GameButtons.DPadLeft) { isPressed ->
            DpadButton(DpadButtonType.LEFT, size = buttonSize, isPressed = isPressed)
        },
        right = DirectionalButtonConfig(GameButtons.DPadRight) { isPressed ->
            DpadButton(DpadButtonType.RIGHT, size = buttonSize, isPressed = isPressed)
        }
    )
}

@Preview(showBackground = false)
@Composable
fun DpadPreview() {
    Dpad(
        gamepadState = GamepadReading(),
    )
}
