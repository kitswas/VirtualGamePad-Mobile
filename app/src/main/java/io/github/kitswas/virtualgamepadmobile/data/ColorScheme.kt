package io.github.kitswas.virtualgamepadmobile.data

import androidx.annotation.StringRes
import io.github.kitswas.virtualgamepadmobile.R

enum class ColorScheme(@StringRes val nameRes: Int) {
    LIGHT(R.string.scheme_light),
    DARK(R.string.scheme_dark),
    SYSTEM(R.string.scheme_system);

    companion object {
        fun fromInt(i: Int): ColorScheme {
            return when (i) {
                0 -> LIGHT
                1 -> DARK
                2 -> SYSTEM
                else -> throw IllegalArgumentException("Invalid ColorScheme value")
            }
        }
    }
}
