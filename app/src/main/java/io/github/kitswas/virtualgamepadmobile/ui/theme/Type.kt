package io.github.kitswas.virtualgamepadmobile.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.kitswas.virtualgamepadmobile.R

// Set of Material typography styles to start with
val Typography = Typography(
    defaultFontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.audiowide)),
    body1 = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.android_insomnia)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.astro_space)),
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    /* Other default text styles to override
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)
