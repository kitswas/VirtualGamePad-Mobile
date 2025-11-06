package io.github.kitswas.virtualgamepadmobile.data

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Anchor point for button positioning
 */
@Immutable
@Serializable
@Parcelize
enum class ButtonAnchor(val displayName: String) : Parcelable {
    TOP_LEFT("Top Left"),
    TOP_CENTER("Top Center"),
    TOP_RIGHT("Top Right"),
    CENTER_LEFT("Center Left"),
    CENTER("Center"),
    CENTER_RIGHT("Center Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right");

    override fun toString(): String = displayName
}

/**
 * Represents all customizable button components in the gamepad
 */
@Immutable
@Serializable
@Parcelize
enum class ButtonComponent(val displayName: String, val defaultAnchor: ButtonAnchor) : Parcelable {
    LEFT_ANALOG_STICK("Left Analog Stick", ButtonAnchor.TOP_LEFT),
    RIGHT_ANALOG_STICK("Right Analog Stick", ButtonAnchor.BOTTOM_RIGHT),
    DPAD("D-Pad", ButtonAnchor.BOTTOM_LEFT),
    FACE_BUTTONS("Face Buttons (A/B/X/Y)", ButtonAnchor.TOP_RIGHT),
    LEFT_TRIGGER("Left Trigger (LT)", ButtonAnchor.BOTTOM_LEFT),
    RIGHT_TRIGGER("Right Trigger (RT)", ButtonAnchor.BOTTOM_RIGHT),
    LEFT_SHOULDER("Left Shoulder (LB)", ButtonAnchor.TOP_CENTER),
    RIGHT_SHOULDER("Right Shoulder (RB)", ButtonAnchor.TOP_CENTER),
    SELECT_BUTTON("Select (View)", ButtonAnchor.TOP_CENTER),
    START_BUTTON("Start (Menu)", ButtonAnchor.TOP_CENTER);

    override fun toString(): String = displayName
}

/**
 * Configuration for a single button component
 */
@Stable
@Serializable
@Parcelize
data class ButtonConfig(
    val component: ButtonComponent,
    val visible: Boolean = true,
    val scale: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val anchor: ButtonAnchor = component.defaultAnchor
) : Parcelable {
    companion object {
        fun default(component: ButtonComponent) = ButtonConfig(component)
    }
}
