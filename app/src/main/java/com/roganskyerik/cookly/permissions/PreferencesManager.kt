package com.roganskyerik.cookly.permissions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.roganskyerik.cookly.ui.Mode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val CAMERA_ENABLED = booleanPreferencesKey("camera_enabled")
        private val FILE_MANAGER_ENABLED = booleanPreferencesKey("file_manager_enabled")
        private val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
        private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val isNotificationsEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[NOTIFICATIONS_ENABLED] ?: false }

    val isCameraEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[CAMERA_ENABLED] ?: false }

    val isFileManagerEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[FILE_MANAGER_ENABLED] ?: false }

    val isLocationEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[LOCATION_ENABLED] ?: false }

    val isReminderEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[REMINDER_ENABLED] ?: false }

    val themeMode: Flow<Mode> = dataStore.data.map { preferences ->
        Mode.fromValue(preferences[THEME_MODE] ?: Mode.SYSTEM.value)
    }


    suspend fun setNotificationsEnabled(value: Boolean) {
        dataStore.edit { preferences -> preferences[NOTIFICATIONS_ENABLED] = value }
    }

    suspend fun setCameraEnabled(value: Boolean) {
        dataStore.edit { preferences -> preferences[CAMERA_ENABLED] = value }
    }

    suspend fun setLocationEnabled(value: Boolean) {
        dataStore.edit { preferences -> preferences[LOCATION_ENABLED] = value }
    }

    suspend fun setReminderEnabled(value: Boolean) {
        dataStore.edit { preferences -> preferences[REMINDER_ENABLED] = value }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
}
