package io.github.kitswas.virtualgamepadmobile.ui.composables

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
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

enum class ShoulderButtonType { LEFT, RIGHT }
enum class MenuButtonType { VIEW, MENU }

// HUD Aesthetic Colors
private val hudBaseColor = Color(0xFFF5F5F5)
private val hudDarkSurface = Color(0xFF1E1E24)

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
        ShoulderButtonType.LEFT -> "LB" // Shortened to LB for better fit in a circle, change to your preference
        ShoulderButtonType.RIGHT -> "RB" // Shortened to RB
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    if (isPressed) {
        Log.d(gameButton.name, "Pressed")
        HapticUtils.performButtonPressFeedback(view)
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        DisposableEffect(Unit) {
            onDispose {
                Log.d(gameButton.name, "Released")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    // Animations
    val animatedBgColor by animateColorAsState(if (isPressed) hudBaseColor else hudDarkSurface, label = "bg")
    val animatedTextColor by animateColorAsState(if (isPressed) hudDarkSurface else hudBaseColor, label = "text")
    val buttonScale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

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
                color = if (isPressed) hudBaseColor else hudBaseColor.copy(alpha = 0.2f),
                shape = CircleShape // Changed to CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = animatedTextColor, fontWeight = FontWeight.Bold)
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

    if (isPressed) {
        Log.d(gameButton.name, "Pressed")
        HapticUtils.performButtonPressFeedback(view)
        gamepadState.ButtonsDown = gamepadState.ButtonsDown or gameButton.value
        DisposableEffect(Unit) {
            onDispose {
                Log.d(gameButton.name, "Released")
                HapticUtils.performButtonReleaseFeedback(view)
                gamepadState.ButtonsDown = gamepadState.ButtonsDown and gameButton.value.inv()
                gamepadState.ButtonsUp = gamepadState.ButtonsUp or gameButton.value
            }
        }
    }

    // Animations
    val animatedBgColor by animateColorAsState(if (isPressed) hudBaseColor else hudDarkSurface, label = "bg")
    val animatedIconColor by animateColorAsState(if (isPressed) hudDarkSurface else hudBaseColor, label = "icon")
    val buttonScale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "scale")

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
                color = if (isPressed) hudBaseColor else hudBaseColor.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = stringResource(R.string.content_desc_button, gameButton.name),
                modifier = Modifier.size(size / 2),
                tint = animatedIconColor
            )
        } else {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.screenicon),
                contentDescription = stringResource(R.string.content_desc_button, gameButton.name),
                modifier = Modifier.size(size / 2),
                tint = animatedIconColor
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
    fun getConfig(component: ButtonComponent) =
        buttonConfigs[component] ?: ButtonConfig.default(component)

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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CentralButtonsPreview() {
    CentralButtons(
        baseDp = 400,
        gamepadState = GamepadReading(),
        buttonConfigs = io.github.kitswas.virtualgamepadmobile.data.defaultButtonConfigs,
    )
}
