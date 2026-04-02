package io.github.kitswas.virtualgamepadmobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val dataStore = context.settingsDataStore

    val baseColor: Flow<BaseColor> = dataStore.data.map { preferences ->
        BaseColor.fromInt(preferences[BASE_COLOR] ?: defaultBaseColor.ordinal)
    }

    val colorScheme: Flow<ColorScheme> = dataStore.data.map { preferences ->
        ColorScheme.fromInt(preferences[COLOR_SCHEME] ?: defaultColorScheme.ordinal)
    }

    val pollingDelay: Flow<Int> = dataStore.data.map { preferences ->
        preferences[POLLING_DELAY] ?: defaultPollingDelay
    }

    val hapticFeedbackEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAPTIC_FEEDBACK_ENABLED] ?: defaultHapticFeedbackEnabled
    }

    val buttonConfigs: Flow<Map<ButtonComponent, ButtonConfig>> =
        dataStore.data.map { preferences ->
            val jsonString = preferences[BUTTON_CONFIGS]
            if (jsonString != null) {
                try {
                    Json.decodeFromString<Map<ButtonComponent, ButtonConfig>>(jsonString)
                } catch (e: Exception) {
                    defaultButtonConfigs
                }
            } else {
                defaultButtonConfigs
            }
        }

    val lastConnectionIpAddress: Flow<String> = dataStore.data.map { preferences ->
        preferences[LAST_CONNECTION_IP_ADDRESS] ?: ""
    }

    val lastConnectionPort: Flow<String> = dataStore.data.map { preferences ->
        preferences[LAST_CONNECTION_PORT] ?: ""
    }

    val saveConnectionCredentials: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SAVE_CONNECTION_CREDENTIALS] ?: defaultSaveConnectionCredentials
    }

    val fullScreenEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FULL_SCREEN_ENABLED] ?: defaultFullScreenEnabled
    }

    val motionStickControl: Flow<MotionStickControl> = dataStore.data.map { preferences ->
        MotionStickControl.fromInt(
            preferences[MOTION_STICK_CONTROL] ?: defaultMotionStickControl.ordinal
        )
    }

    val motionSensitivity: Flow<Float> = dataStore.data.map { preferences ->
        preferences[MOTION_SENSITIVITY] ?: defaultMotionSensitivity
    }

    suspend fun setBaseColor(baseColor: BaseColor) {
        dataStore.edit { preferences ->
            preferences[BASE_COLOR] = baseColor.ordinal
        }
    }

    suspend fun setColorScheme(colorScheme: ColorScheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_SCHEME] = colorScheme.ordinal
        }
    }

    suspend fun setPollingDelay(delay: Int) {
        dataStore.edit { preferences ->
            preferences[POLLING_DELAY] = delay
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setButtonConfig(component: ButtonComponent, config: ButtonConfig) {
        dataStore.edit { preferences ->
            val currentJson = preferences[BUTTON_CONFIGS]
            val currentConfigs = if (currentJson != null) {
                try {
                    Json.decodeFromString<Map<ButtonComponent, ButtonConfig>>(currentJson)
                        .toMutableMap()
                } catch (e: Exception) {
                    defaultButtonConfigs.toMutableMap()
                }
            } else {
                defaultButtonConfigs.toMutableMap()
            }
            currentConfigs[component] = config
            preferences[BUTTON_CONFIGS] = Json.encodeToString(currentConfigs)
        }
    }

    suspend fun setAllButtonConfigs(configs: Map<ButtonComponent, ButtonConfig>) {
        dataStore.edit { preferences ->
            preferences[BUTTON_CONFIGS] = Json.encodeToString(configs)
        }
    }

    suspend fun setLastConnectionCredentials(ipAddress: String, port: String) {
        dataStore.edit { preferences ->
            preferences[LAST_CONNECTION_IP_ADDRESS] = ipAddress
            preferences[LAST_CONNECTION_PORT] = port
        }
    }

    suspend fun setSaveConnectionCredentials(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SAVE_CONNECTION_CREDENTIALS] = enabled
        }
    }

    suspend fun setFullScreenEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FULL_SCREEN_ENABLED] = enabled
        }
    }

    suspend fun setMotionStickControl(control: MotionStickControl) {
        dataStore.edit { preferences ->
            preferences[MOTION_STICK_CONTROL] = control.ordinal
        }
    }

    suspend fun setMotionSensitivity(sensitivity: Float) {
        dataStore.edit { preferences ->
            preferences[MOTION_SENSITIVITY] = sensitivity
        }
    }

    suspend fun resetAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val COLOR_SCHEME = intPreferencesKey("color_scheme")
        private val BASE_COLOR = intPreferencesKey("base_color")
        private val POLLING_DELAY = intPreferencesKey("polling_delay")
        private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        private val BUTTON_CONFIGS = stringPreferencesKey("button_configs")
        private val LAST_CONNECTION_IP_ADDRESS = stringPreferencesKey("last_connection_ip_address")
        private val LAST_CONNECTION_PORT = stringPreferencesKey("last_connection_port")
        private val SAVE_CONNECTION_CREDENTIALS =
            booleanPreferencesKey("save_connection_credentials")
        private val FULL_SCREEN_ENABLED = booleanPreferencesKey("full_screen_enabled")
        private val MOTION_STICK_CONTROL = intPreferencesKey("motion_stick_control")
        private val MOTION_SENSITIVITY = floatPreferencesKey("motion_sensitivity")
    }
}
