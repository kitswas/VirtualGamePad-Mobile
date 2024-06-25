package io.github.kitswas.virtualgamepadmobile.data

import androidx.compose.ui.graphics.Color
import io.github.kitswas.virtualgamepadmobile.ui.theme.GlossyBlue
import io.github.kitswas.virtualgamepadmobile.ui.theme.GlossyGreen
import io.github.kitswas.virtualgamepadmobile.ui.theme.GlossyRed
import io.github.kitswas.virtualgamepadmobile.ui.theme.NeonBlue
import io.github.kitswas.virtualgamepadmobile.ui.theme.NeonGreen
import io.github.kitswas.virtualgamepadmobile.ui.theme.NeonRed

enum class BaseColor {
    RED, GREEN, BLUE;

    companion object {
        fun fromInt(i: Int): BaseColor {
            return when (i) {
                0 -> RED
                1 -> GREEN
                2 -> BLUE
                else -> throw IllegalArgumentException("Invalid BaseColor value")
            }
        }
    }
}

/**
 * Returns a color from a given base color name depending on the current color scheme.
 */
fun getColorFromBaseColor(baseColor: BaseColor, isDarkMode: Boolean): Color {
    return when (baseColor) {
        BaseColor.RED -> if (isDarkMode) NeonRed else GlossyRed
        BaseColor.GREEN -> if (isDarkMode) NeonGreen else GlossyGreen
        BaseColor.BLUE -> if (isDarkMode) NeonBlue else GlossyBlue
    }
}
