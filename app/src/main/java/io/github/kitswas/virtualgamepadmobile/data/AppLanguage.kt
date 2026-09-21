package io.github.kitswas.virtualgamepadmobile.data

import androidx.annotation.StringRes
import io.github.kitswas.virtualgamepadmobile.R

enum class AppLanguage(
    val code: String,
    @StringRes val nameRes: Int,
) {
    ARABIC("ar", R.string.language_arabic),
    ENGLISH("en", R.string.language_english);

    companion object {
        val Default = ARABIC

        fun fromCode(code: String?): AppLanguage {
            return when (code) {
                "en" -> ENGLISH
                "ar" -> ARABIC
                else -> Default
            }
        }
    }
}
