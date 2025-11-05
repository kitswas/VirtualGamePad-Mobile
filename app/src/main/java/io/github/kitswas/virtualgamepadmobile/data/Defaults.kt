package io.github.kitswas.virtualgamepadmobile.data

val defaultColorScheme = ColorScheme.SYSTEM
val defaultBaseColor = BaseColor.BLUE
const val defaultPollingDelay = 80 // in milliseconds
const val defaultHapticFeedbackEnabled = false // vibrations

// Default button configurations with offsets matching the original layout
// Note: Offset values are multipliers of baseDp (heightDp in landscape)
// These will be applied as: offsetX * baseDp or offsetY * baseDp at runtime
val defaultButtonConfigs = mapOf(
    ButtonComponent.LEFT_ANALOG_STICK to ButtonConfig(
        component = ButtonComponent.LEFT_ANALOG_STICK,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_LEFT
    ),
    ButtonComponent.RIGHT_ANALOG_STICK to ButtonConfig(
        component = ButtonComponent.RIGHT_ANALOG_STICK,
        visible = true,
        scale = 1.0f,
        offsetX = -0.25f, // -baseDp/4
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_RIGHT
    ),
    ButtonComponent.DPAD to ButtonConfig(
        component = ButtonComponent.DPAD,
        visible = true,
        scale = 1.0f,
        offsetX = 0.333f, // baseDp/3
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.FACE_BUTTONS to ButtonConfig(
        component = ButtonComponent.FACE_BUTTONS,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_RIGHT
    ),
    ButtonComponent.LEFT_TRIGGER to ButtonConfig(
        component = ButtonComponent.LEFT_TRIGGER,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_LEFT
    ),
    ButtonComponent.RIGHT_TRIGGER to ButtonConfig(
        component = ButtonComponent.RIGHT_TRIGGER,
        visible = true,
        scale = 1.0f,
        offsetX = 0f,
        offsetY = 0f,
        anchor = ButtonAnchor.BOTTOM_RIGHT
    ),
    ButtonComponent.LEFT_SHOULDER to ButtonConfig(
        component = ButtonComponent.LEFT_SHOULDER,
        visible = true,
        scale = 1.0f,
        offsetX = -0.25f, // -baseDp/4
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.RIGHT_SHOULDER to ButtonConfig(
        component = ButtonComponent.RIGHT_SHOULDER,
        visible = true,
        scale = 1.0f,
        offsetX = 0.25f, // baseDp/4
        offsetY = 0f,
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.SELECT_BUTTON to ButtonConfig(
        component = ButtonComponent.SELECT_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = -0.25f, // -baseDp/4
        offsetY = 0.25f, // baseDp/4
        anchor = ButtonAnchor.TOP_CENTER
    ),
    ButtonComponent.START_BUTTON to ButtonConfig(
        component = ButtonComponent.START_BUTTON,
        visible = true,
        scale = 1.0f,
        offsetX = 0.25f, // baseDp/4
        offsetY = 0.25f, // baseDp/4
        anchor = ButtonAnchor.TOP_CENTER
    )
)
