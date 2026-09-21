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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
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

    val label = when (type) {
        TriggerType.LEFT -> stringResource(R.string.button_lt)
        TriggerType.RIGHT -> stringResource(R.string.button_rt)
    }

    // --- 2D HUD Aesthetic Properties ---
    val baseColor = Color(0xFFF5F5F5) // Clean White/Light Gray
    val darkSurfaceColor = Color(0xFF1E1E24)

    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) baseColor else darkSurfaceColor,
        label = "bgColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isPressed) darkSurfaceColor else baseColor,
        label = "textColor"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(size) // Locked to perfect circle size
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            }
            .clip(CircleShape) // Changed to CircleShape
            .background(animatedBgColor)
            .border(
                width = 1.dp,
                color = if (isPressed) baseColor else baseColor.copy(alpha = 0.2f),
                shape = CircleShape // Changed to CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disables default ripple
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = animatedTextColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun TriggerPreview() {
    Row {
        Trigger(type = TriggerType.LEFT, size = 64.dp, gamepadState = GamepadReading())
        Trigger(type = TriggerType.RIGHT, size = 64.dp, gamepadState = GamepadReading())
    }
}
