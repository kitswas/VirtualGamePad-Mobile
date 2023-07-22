package io.github.kitswas.virtualgamepadmobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
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
    gamepadState: GamepadReading,
) {
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
    OutlinedIconButton(
        modifier = modifier.size(size).padding(0.dp),
        onClick = {
            gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        },
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = backgroundColour,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Dpad Button ${type.name}",
            modifier = Modifier
                .rotate(rotation)
                .size(size),
            tint = foregroundColour
        )
    }
}

@Composable
fun Dpad(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        DpadButton(
            type = DpadButtonType.UP,
            modifier = Modifier.align(Alignment.TopCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
        DpadButton(
            type = DpadButtonType.DOWN,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
        DpadButton(
            type = DpadButtonType.LEFT,
            modifier = Modifier.align(Alignment.CenterStart),
            size = 2 * size / 5,
            gamepadState = gamepadState,
        )
        DpadButton(
            type = DpadButtonType.RIGHT,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = 2 * size / 5,
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
