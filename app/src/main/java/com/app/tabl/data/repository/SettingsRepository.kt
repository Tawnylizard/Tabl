package com.app.tabl.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_SNOOZE_MINUTES = intPreferencesKey("snooze_minutes")
        val KEY_MAX_SNOOZE_COUNT = intPreferencesKey("max_snooze_count")
        val KEY_MAX_REPEAT_COUNT = intPreferencesKey("max_repeat_count")
        val KEY_THEME = stringPreferencesKey("theme")

        const val DEFAULT_SNOOZE_MINUTES = 10
        const val DEFAULT_MAX_SNOOZE_COUNT = 3
        const val DEFAULT_MAX_REPEAT_COUNT = 2
        const val DEFAULT_THEME = "system"
    }

    val snoozeMinutes: Flow<Int> = dataStore.data.map {
        it[KEY_SNOOZE_MINUTES] ?: DEFAULT_SNOOZE_MINUTES
    }

    val maxSnoozeCount: Flow<Int> = dataStore.data.map {
        it[KEY_MAX_SNOOZE_COUNT] ?: DEFAULT_MAX_SNOOZE_COUNT
    }

    val maxRepeatCount: Flow<Int> = dataStore.data.map {
        it[KEY_MAX_REPEAT_COUNT] ?: DEFAULT_MAX_REPEAT_COUNT
    }

    val theme: Flow<String> = dataStore.data.map {
        it[KEY_THEME] ?: DEFAULT_THEME
    }

    suspend fun setSnoozeMinutes(minutes: Int) {
        dataStore.edit { it[KEY_SNOOZE_MINUTES] = minutes }
    }

    suspend fun setMaxSnoozeCount(count: Int) {
        dataStore.edit { it[KEY_MAX_SNOOZE_COUNT] = count }
    }

    suspend fun setMaxRepeatCount(count: Int) {
        dataStore.edit { it[KEY_MAX_REPEAT_COUNT] = count }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { it[KEY_THEME] = theme }
    }
}
