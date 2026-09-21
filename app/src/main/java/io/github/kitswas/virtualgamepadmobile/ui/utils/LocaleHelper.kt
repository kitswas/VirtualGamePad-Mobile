package io.github.kitswas.virtualgamepadmobile.ui.utils

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.os.LocaleListCompat
import io.github.kitswas.virtualgamepadmobile.data.AppLanguage
import java.util.Locale

object LocaleHelper {
    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    fun applyToActivity(activity: android.app.Activity, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager =
                    activity.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales =
                    LocaleList(Locale.forLanguageTag(language.code))
                return
            } catch (_: Exception) {
                // Fall through to recreate path
            }
        }
        // Pre-Android 13: recreate so attachBaseContext wrapping takes effect.
        // LocaleListCompat is used only to keep the platform list in sync where possible.
        try {
            LocaleListCompat.forLanguageTags(language.code)
        } catch (_: Exception) {
        }
        activity.recreate()
    }

    fun currentLanguage(context: Context): AppLanguage {
        val tags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val lm = context.getSystemService(LocaleManager::class.java)
                val appLocales = lm?.applicationLocales
                if (appLocales != null && !appLocales.isEmpty) {
                    return AppLanguage.fromCode(appLocales[0]?.language)
                }
            } catch (_: Exception) {
            }
            null
        } else {
            null
        }
        if (tags != null) return tags
        val locale = context.resources.configuration.locales.get(0)
            ?: context.resources.configuration.locale
        return AppLanguage.fromCode(locale?.language)
    }
}
