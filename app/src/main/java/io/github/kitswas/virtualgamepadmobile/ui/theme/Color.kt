package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

// Should be the same as the values defined in values/colors.xml

// The colours should be changed to match the colours of the gamepad
// Neon in the dark for the gamers' eyes

val NeonGreen = Color(0xFF39FF14)
val NeonBlue = Color(0xFF00FFFF)
val NeonRed = Color(0xFFFF0000)
val GlossyGreen = Color(0xFF00AA00)
val GlossyBlue = Color(0xFF035CC2)
val GlossyRed = Color(0xFFDD0000)
val Gold = Color(0xFFFFD700)
val Silver = Color(0xFFC0C0C0)
val PureBlack = Color(0xFF000000)
val PristineWhite = Color(0xFFFFFFFF)

/**
 * Darkens a [Color] by the given [fraction] (in percentage).
 * Can be used to create dark tones for a color.
 * @see lighten
 */
fun darken(color: Color, fraction: Float): Color {
    return Color(
        red = color.red * (1 - fraction),
        green = color.green * (1 - fraction),
        blue = color.blue * (1 - fraction),
        alpha = color.alpha
    )
}

/**
 * Lightens a [Color] by the given [fraction] (in percentage).
 * Can be used to create light tones for a color.
 * @see darken
 */
fun lighten(color: Color, fraction: Float): Color {
    return Color(
        red = color.red + (1 - color.red) * fraction,
        green = color.green + (1 - color.green) * fraction,
        blue = color.blue + (1 - color.blue) * fraction,
        alpha = color.alpha
    )
}

/**
 * Hue shifts a [Color] by the given [degrees] (-359 to 359).
 */
fun shift(color: Color, degrees: Int): Color {
    val degrees = degrees % 360 // Ensure degrees is in range
    // Convert to HSL
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    // Shift hue
    hsl[0] = (hsl[0] + degrees + 360) % 360
    // Convert back to Color
    return Color(ColorUtils.HSLToColor(hsl))
}

/**
 * Returns a color for the given [color] with sufficient contrast.
 */
fun contrasting(color: Color): Color {
    return if (color.luminance() > 0.5f) PureBlack else PristineWhite
}
