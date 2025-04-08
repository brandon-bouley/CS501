package com.example.personaldiaryapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDataStore(private val context: Context) {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    companion object {
        private val THEME_KEY = booleanPreferencesKey("theme_mode")
        private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    }

    val themeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences: Preferences ->
            preferences[THEME_KEY] ?: false
        }

    val fontSizeFlow: Flow<Float> = context.dataStore.data
        .map { preferences: Preferences ->
            preferences[FONT_SIZE_KEY] ?: 16f
        }

    suspend fun toggleTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDark
        }
    }

    suspend fun updateFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size
        }
    }
}