package com.app.velopath.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.velopath.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val theme = stringPreferencesKey("theme_mode")

    suspend fun saveThemePreference(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[theme] = themeMode.name
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val mode = preferences[theme]
            when (mode) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM_DEFAULT
            }
        }
}