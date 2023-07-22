package io.github.kitswas.virtualgamepadmobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.VirtualGamePadMobileTheme
import io.github.kitswas.virtualgamepadmobile.ui.theme.darken
import io.github.kitswas.virtualgamepadmobile.ui.theme.lighten
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


enum class FaceButtonType {
    A, B, X, Y
}

private val faceButtonColourMap = mapOf(
    FaceButtonType.A to Color(0xFF00FF00),
    FaceButtonType.B to Color(0xFFFF0000),
    FaceButtonType.X to Color(0xFF0000FF),
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
    connectionViewModel: ConnectionViewModel?,
) {
    val gameButton = when (type) {
        FaceButtonType.A -> GameButtons.A
        FaceButtonType.B -> GameButtons.B
        FaceButtonType.X -> GameButtons.X
        FaceButtonType.Y -> GameButtons.Y
    }
    OutlinedButton(
        modifier = modifier.size(size).padding(0.dp),
        onClick = {
            if (connectionViewModel != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
                    connectionViewModel.sendGamepadState(gamepadState)
                    gamepadState.ButtonsUp = gamepadState.ButtonsDown
                    gamepadState.ButtonsDown = 0
                    connectionViewModel.sendGamepadState(gamepadState)
                }
            }
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColour,
        ),
    ) {
        Text(
            text = type.name,
            color = foregroundColour,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun FaceButtons(
    modifier: Modifier = Modifier,
    size: Dp = 360.dp,
    gamepadState: GamepadReading,
    connectionViewModel: ConnectionViewModel?,
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
            connectionViewModel = connectionViewModel,
        )
        FaceButton(
            type = FaceButtonType.B,
            modifier = Modifier.align(Alignment.CenterEnd),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            connectionViewModel = connectionViewModel,
        )
        FaceButton(
            type = FaceButtonType.X,
            modifier = Modifier.align(Alignment.CenterStart),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            connectionViewModel = connectionViewModel,
        )
        FaceButton(
            type = FaceButtonType.Y,
            modifier = Modifier.align(Alignment.TopCenter),
            size = 2 * size / 5,
            gamepadState = gamepadState,
            connectionViewModel = connectionViewModel,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun FaceButtonsPreview() {
    VirtualGamePadMobileTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FaceButtons(
                gamepadState = GamepadReading(),
                connectionViewModel = null,
            )
        }
    }
}
