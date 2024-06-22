package io.github.kitswas.virtualgamepadmobile.data

enum class ColorScheme {
    LIGHT, DARK, SYSTEM;

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
