package io.github.kitswas.virtualgamepadmobile.data

enum class MotionStickControl {
    OFF,
    LEFT,
    RIGHT;

    companion object {
        fun fromInt(value: Int): MotionStickControl =
            entries.getOrElse(value) { OFF }
    }
}
