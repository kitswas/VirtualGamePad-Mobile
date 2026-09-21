package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

// 1. Our Minimalist Luxury Palette
val DeepSurface = Color(0xFF121212)
val SlateGray = Color(0xFF2C2C2E)
val SubduedSilver = Color(0xFFB0B0B0)
val PremiumGold = Color(0xFFD4AF37) 
val PureBlack = Color(0xFF000000)
val PristineWhite = Color(0xFFFFFFFF)
val SuccessGreen = Color(0xFF388E3C) 

// 2. Legacy Aliases (Satisfies the compiler but forces the premium look)
val NeonGreen = SlateGray
val NeonBlue = SlateGray
val NeonRed = SlateGray
val GlossyGreen = DeepSurface
val GlossyBlue = DeepSurface
val GlossyRed = DeepSurface
val Gold = PremiumGold
val Silver = SubduedSilver

// 3. Helper Functions
fun darken(color: Color, fraction: Float): Color {
    return Color(
        red = color.red * (1 - fraction),
        green = color.green * (1 - fraction),
        blue = color.blue * (1 - fraction),
        alpha = color.alpha
    )
}

fun lighten(color: Color, fraction: Float): Color {
    return Color(
        red = color.red + (1 - color.red) * fraction,
        green = color.green + (1 - color.green) * fraction,
        blue = color.blue + (1 - color.blue) * fraction,
        alpha = color.alpha
    )
}

fun shift(color: Color, degrees: Int): Color {
    val modDegrees = degrees % 360
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[0] = (hsl[0] + modDegrees + 360) % 360
    return Color(ColorUtils.HSLToColor(hsl))
}

fun contrasting(color: Color): Color {
    return if (color.luminance() > 0.5f) PureBlack else PristineWhite
}
