package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.ui.theme.Shapes
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils

enum class TriggerType {
    LEFT, RIGHT
}

@Composable
fun Trigger(
    type: TriggerType,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    gamepadState: GamepadReading,
) {
    val view = LocalView.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    DisposableEffect(isPressed) {
        if (isPressed) {
            Log.d("TriggerButton", "Trigger ${type.name} pressed")
            when (type) {
                TriggerType.LEFT -> gamepadState.LeftTrigger = 1f
                TriggerType.RIGHT -> gamepadState.RightTrigger = 1f
            }
            HapticUtils.performButtonPressFeedback(view)
        }
        onDispose {
            Log.d("TriggerButton", "Trigger ${type.name} released")
            when (type) {
                TriggerType.LEFT -> gamepadState.LeftTrigger = 0f
                TriggerType.RIGHT -> gamepadState.RightTrigger = 0f
            }
            HapticUtils.performButtonReleaseFeedback(view)
        }
    }
    val text = when (type) {
        TriggerType.LEFT -> "LT"
        TriggerType.RIGHT -> "RT"
    }

    Box(
        modifier = modifier
            .size(size)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {},
            modifier = Modifier.fillMaxSize(),
            shape = Shapes.small,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            interactionSource = interactionSource,
        ) {
            Text(text)
        }
    }
}
