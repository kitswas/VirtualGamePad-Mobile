package io.github.kitswas.virtualgamepadmobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val dataStore = context.settingsDataStore

    val colorScheme: Flow<ColorScheme> = dataStore.data.map { preferences ->
        ColorScheme.fromInt(preferences[COLOR_SCHEME] ?: ColorScheme.SYSTEM.ordinal)
    }

    suspend fun setColorScheme(colorScheme: ColorScheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_SCHEME] = colorScheme.ordinal
        }
    }

    companion object {
        private val COLOR_SCHEME = intPreferencesKey("color_scheme")
    }
}
