package io.github.kitswas.virtualgamepadmobile.data

enum class ButtonSize(val scaleFactor: Float) {
    SMALL(0.8f),
    MEDIUM(0.9f),
    LARGE(1.0f);

    companion object {
        fun fromInt(value: Int): ButtonSize {
            return entries.getOrElse(value) { MEDIUM }
        }
    }

    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
