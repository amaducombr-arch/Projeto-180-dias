package br.com.projeto180dias.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "project_settings")

data class AppSettings(
    /** Dia inicial como epoch day (LocalDate.toEpochDay), nulo até o usuário definir. */
    val journeyStartEpochDay: Long? = null,
    val remindersEnabled: Boolean = false,
    /** Minutos após meia-noite, de 0 a 1439. */
    val dailyReminderMinutes: Int = 8 * 60,
    val onboardingCompleted: Boolean = false
)

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    private object Keys {
        val journeyStartEpochDay = longPreferencesKey("journey_start_epoch_day")
        val remindersEnabled = booleanPreferencesKey("reminders_enabled")
        val dailyReminderMinutes = intPreferencesKey("daily_reminder_minutes")
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            journeyStartEpochDay = preferences[Keys.journeyStartEpochDay],
            remindersEnabled = preferences[Keys.remindersEnabled] ?: false,
            dailyReminderMinutes = (preferences[Keys.dailyReminderMinutes] ?: 480).coerceIn(0, 1439),
            onboardingCompleted = preferences[Keys.onboardingCompleted] ?: false
        )
    }

    suspend fun setJourneyStartEpochDay(epochDay: Long?) {
        dataStore.edit { preferences ->
            if (epochDay == null) preferences.remove(Keys.journeyStartEpochDay)
            else preferences[Keys.journeyStartEpochDay] = epochDay
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.remindersEnabled] = enabled }
    }

    suspend fun setDailyReminderMinutes(minutes: Int) {
        dataStore.edit { it[Keys.dailyReminderMinutes] = minutes.coerceIn(0, 1439) }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.onboardingCompleted] = completed }
    }
}
