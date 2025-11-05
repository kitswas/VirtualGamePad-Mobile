package io.github.kitswas.virtualgamepadmobile.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents all customizable button components in the gamepad
 */
@Serializable
@Parcelize
enum class ButtonComponent(val displayName: String) : Parcelable {
    LEFT_ANALOG_STICK("Left Analog Stick"),
    RIGHT_ANALOG_STICK("Right Analog Stick"),
    DPAD("D-Pad"),
    FACE_BUTTONS("Face Buttons (A/B/X/Y)"),
    LEFT_TRIGGER("Left Trigger (LT)"),
    RIGHT_TRIGGER("Right Trigger (RT)"),
    LEFT_SHOULDER("Left Shoulder (LB)"),
    RIGHT_SHOULDER("Right Shoulder (RB)"),
    SELECT_BUTTON("Select Button"),
    START_BUTTON("Start Button");

    override fun toString(): String = displayName
}

/**
 * Configuration for a single button component
 */
@Serializable
@Parcelize
data class ButtonConfig(
    val component: ButtonComponent,
    val visible: Boolean = true,
    val scale: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Parcelable {
    companion object {
        fun default(component: ButtonComponent) = ButtonConfig(component)
    }
}
