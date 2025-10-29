package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kitswas.virtualgamepadmobile.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.oxanium)),
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.oxanium)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.oxanium)),
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.astro_space)),
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.astro_space)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.astro_space)),
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.android_insomnia)),
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.android_insomnia)),
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.android_insomnia)),
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
)

// Helper function to get a TextStyle for Face Buttons based on size
fun faceButtonTextStyle(size: Dp): TextStyle {
    val threshold = 20.dp
    val fontSize = when {
        size < threshold -> (size.value * 0.6).sp
        else -> (size.value * 0.4).sp
    }
    return TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.astro_space)),
        fontWeight = FontWeight.Bold,
        fontSize = fontSize
    )
}
