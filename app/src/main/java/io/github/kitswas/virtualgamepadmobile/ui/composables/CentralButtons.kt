package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.kitswas.VGP_Data_Exchange.GameButtons
import io.github.kitswas.VGP_Data_Exchange.GamepadReading
import io.github.kitswas.virtualgamepadmobile.R
import io.github.kitswas.virtualgamepadmobile.data.ButtonAnchor
import io.github.kitswas.virtualgamepadmobile.data.ButtonComponent
import io.github.kitswas.virtualgamepadmobile.data.ButtonConfig
import io.github.kitswas.virtualgamepadmobile.ui.utils.HapticUtils

/**
 * Convert ButtonAnchor to Compose Alignment
 */
private fun ButtonAnchor.toAlignment(): Alignment = when (this) {
    ButtonAnchor.TOP_LEFT -> Alignment.TopStart
    ButtonAnchor.TOP_CENTER -> Alignment.TopCenter
    ButtonAnchor.TOP_RIGHT -> Alignment.TopEnd
    ButtonAnchor.CENTER_LEFT -> Alignment.CenterStart
    ButtonAnchor.CENTER -> Alignment.Center
    ButtonAnchor.CENTER_RIGHT -> Alignment.CenterEnd
    ButtonAnchor.BOTTOM_LEFT -> Alignment.BottomStart
    ButtonAnchor.BOTTOM_CENTER -> Alignment.BottomCenter
    ButtonAnchor.BOTTOM_RIGHT -> Alignment.BottomEnd
}

enum class ShoulderButtonType {
    LEFT, RIGHT
}

enum class MenuButtonType {
    VIEW, MENU
}

@Composable
fun ShoulderButton(
    type: ShoulderButtonType,
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
) {
    val view = LocalView.current
    val gameButton = when (type) {
        ShoulderButtonType.LEFT -> GameButtons.LeftShoulder
        ShoulderButtonType.RIGHT -> GameButtons.RightShoulder
    }
    val text = when (type) {
        ShoulderButtonType.LEFT -> "LSHLDR"
        ShoulderButtonType.RIGHT -> "RSHLDR"
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        Log.d(gameButton.name, "Pressed")
        HapticUtils.performButtonPressFeedback(view)
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        //Use if + DisposableEffect to wait for the press action is completed
        DisposableEffect(Unit) {
            onDispose {
                Log.d(gameButton.name, "Released")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown =
                    gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    Button(
        modifier = modifier
            .heightIn(min = size)
            .widthIn(min = size * 1.5f),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = { },
        interactionSource = interactionSource,
    ) {
        Text(text)
    }
}

@Composable
fun MenuButton(
    type: MenuButtonType,
    modifier: Modifier = Modifier,
    size: Dp,
    gamepadState: GamepadReading,
) {
    val view = LocalView.current
    val gameButton = when (type) {
        MenuButtonType.VIEW -> GameButtons.View
        MenuButtonType.MENU -> GameButtons.Menu
    }
    val iconPainter = when (type) {
        MenuButtonType.VIEW -> null
        MenuButtonType.MENU -> painterResource(R.drawable.ic_menu)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // See https://stackoverflow.com/a/69157877/8659747
    if (isPressed) {
        Log.d(gameButton.name, "Pressed")
        HapticUtils.performButtonPressFeedback(view)
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        //Use if + DisposableEffect to wait for the press action is completed
        DisposableEffect(Unit) {
            onDispose {
                Log.d(gameButton.name, "Released")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown =
                    gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    OutlinedIconButton(
        modifier = modifier.size(size),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = { },
        interactionSource = interactionSource,
    ) {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = "${gameButton.name} Button",
                modifier = Modifier.size(size / 2),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.screenicon),
                contentDescription = "${gameButton.name} Button",
                modifier = Modifier.size(size / 2),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * The central buttons section containing shoulder buttons (L/R bumpers) and menu buttons (View/Menu) for the gamepad.
 */
@Composable
fun CentralButtons(
    modifier: Modifier = Modifier,
    baseDp: Int,
    gamepadState: GamepadReading,
    buttonConfigs: Map<ButtonComponent, ButtonConfig>,
) {
    // Helper function to get config for a component
    fun getConfig(component: ButtonComponent) =
        buttonConfigs[component] ?: ButtonConfig.default(component)

    // Helper function to render a button component with its anchor
    @Composable
    fun RenderButton(component: ButtonComponent, content: @Composable (ButtonConfig) -> Unit) {
        val config = getConfig(component)
        if (config.visible) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = config.anchor.toAlignment()
            ) {
                content(config)
            }
        }
    }

    // Left Shoulder button
    RenderButton(ButtonComponent.LEFT_SHOULDER) { config ->
        ShoulderButton(
            type = ShoulderButtonType.LEFT,
            modifier = Modifier.offset(
                x = (config.offsetX * baseDp).dp,
                y = (config.offsetY * baseDp).dp
            ),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
        )
    }

    // Right Shoulder button
    RenderButton(ButtonComponent.RIGHT_SHOULDER) { config ->
        ShoulderButton(
            type = ShoulderButtonType.RIGHT,
            modifier = Modifier.offset(
                x = (config.offsetX * baseDp).dp,
                y = (config.offsetY * baseDp).dp
            ),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
        )
    }

    // Select button (View)
    RenderButton(ButtonComponent.SELECT_BUTTON) { config ->
        MenuButton(
            type = MenuButtonType.VIEW,
            modifier = Modifier.offset(
                x = (config.offsetX * baseDp).dp,
                y = (config.offsetY * baseDp).dp
            ),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
        )
    }

    // Start button (Menu)
    RenderButton(ButtonComponent.START_BUTTON) { config ->
        MenuButton(
            type = MenuButtonType.MENU,
            modifier = Modifier.offset(
                x = (config.offsetX * baseDp).dp,
                y = (config.offsetY * baseDp).dp
            ),
            size = (baseDp / 8 * config.scale).dp,
            gamepadState = gamepadState,
        )
    }
}

@Preview(showBackground = false)
@Composable
fun CentralButtonsPreview() {
    CentralButtons(
        baseDp = 400,
        gamepadState = GamepadReading(),
        buttonConfigs = io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs,
    )
}
