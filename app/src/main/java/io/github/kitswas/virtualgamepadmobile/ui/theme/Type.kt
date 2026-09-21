package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kitswas.virtualgamepadmobile.R

// Noto Naskh Arabic (matches the منصة المتحكمات server theme);
// system fonts cover any glyphs it lacks.
val Naskh = FontFamily(
    Font(R.font.naskh_regular, FontWeight.Normal),
    Font(R.font.naskh_bold, FontWeight.Bold),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Naskh,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Naskh,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Naskh,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    )
    // Add remaining required styles using the Naskh family
)

fun faceButtonTextStyle(size: Dp): TextStyle {
    val threshold = 20.dp
    val fontSize = when {
        size < threshold -> (size.value * 0.6).sp
        else -> (size.value * 0.4).sp
    }
    return TextStyle(
        fontFamily = Naskh,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize
    )
}
