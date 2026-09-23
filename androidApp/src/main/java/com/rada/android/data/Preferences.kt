package com.rada.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rada.shared.domain.WarningLead
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "rada_prefs")

private val reportTimesKey = stringPreferencesKey("my_report_times")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class ThemePreferences(private val context: Context) {
    private val key = intPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[key]) {
            1 -> ThemeMode.LIGHT
            2 -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[key] = when (mode) {
                ThemeMode.SYSTEM -> 0
                ThemeMode.LIGHT -> 1
                ThemeMode.DARK -> 2
            }
        }
    }
}

class OnboardingPreferences(private val context: Context) {
    private val key = booleanPreferencesKey("onboarding_complete")

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[key] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs -> prefs[key] = true }
    }
}

class UserProfilePreferences(private val context: Context) {
    private val key = stringPreferencesKey("user_display_name")

    val userName: Flow<String?> = context.dataStore.data.map { prefs -> prefs[key] }

    private val photoKey = stringPreferencesKey("user_photo_path")

    val photoPath: Flow<String?> = context.dataStore.data.map { prefs -> prefs[photoKey] }

    suspend fun setPhotoPath(path: String?) {
        context.dataStore.edit { prefs ->
            if (path == null) prefs.remove(photoKey) else prefs[photoKey] = path
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs -> prefs[key] = name }
    }

    suspend fun clearUserName() {
        context.dataStore.edit { prefs -> prefs.remove(key); prefs.remove(photoKey); prefs.remove(reportTimesKey) }
    }
}

class AlertPreferences(private val context: Context) {
    private val key = booleanPreferencesKey("alerts_enabled")

    val alertsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[key] ?: true }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[key] = enabled }
    }

    private val leadKey = intPreferencesKey("warning_lead")

    val warningLead: Flow<WarningLead> = context.dataStore.data.map { prefs ->
        WarningLead.entries.getOrElse(prefs[leadKey] ?: 0) { WarningLead.STANDARD }
    }

    suspend fun setWarningLead(lead: WarningLead) {
        context.dataStore.edit { prefs -> prefs[leadKey] = lead.ordinal }
    }
}

class ReportHistoryPreferences(private val context: Context) {
    private val weekMillis = 7L * 24 * 60 * 60 * 1000

    val reportsThisWeek: Flow<Int> = context.dataStore.data.map { prefs ->
        val cutoff = System.currentTimeMillis() - weekMillis
        parse(prefs[reportTimesKey]).count { it >= cutoff }
    }

    suspend fun record(timestamp: Long) {
        context.dataStore.edit { prefs ->
            val cutoff = System.currentTimeMillis() - weekMillis
            val kept = parse(prefs[reportTimesKey]).filter { it >= cutoff } + timestamp
            prefs[reportTimesKey] = kept.joinToString(",")
        }
    }

    suspend fun remove(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[reportTimesKey] = parse(prefs[reportTimesKey]).filter { it != timestamp }.joinToString(",")
        }
    }

    private fun parse(raw: String?): List<Long> =
        raw?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
}

sealed interface TrialState {
    data object Loading : TrialState
    data object NotStarted : TrialState
    data class Active(val daysLeft: Int) : TrialState
    data object Expired : TrialState
}

class TrialPreferences(private val context: Context) {
    private val startedKey = longPreferencesKey("drive_trial_started_at")
    private val ownerKey = stringPreferencesKey("drive_trial_owner_uid")

    val state: Flow<TrialState> = combine(
        context.dataStore.data.map { it[startedKey] },
        flow { while (true) { emit(Unit); delay(60_000L) } },
        synced
    ) { startedAt, _, isSynced ->
        when {
            !isSynced -> TrialState.Loading
            startedAt == null -> TrialState.NotStarted
            else -> {
                val remainingMs = startedAt + TRIAL_DAYS * DAY_MS - System.currentTimeMillis()
                if (remainingMs <= 0) TrialState.Expired
                else TrialState.Active(((remainingMs + DAY_MS - 1) / DAY_MS).toInt())
            }
        }
    }

    suspend fun adoptAccountTrial(uid: String, startedAtMillis: Long?) {
        context.dataStore.edit { prefs ->
            if (prefs[ownerKey] != uid) prefs.remove(startedKey)
            prefs[ownerKey] = uid
            if (startedAtMillis != null) prefs[startedKey] = startedAtMillis
        }
    }

    fun markSynced() { synced.value = true }

    suspend fun startIfNeeded() {
        context.dataStore.edit { prefs ->
            if (prefs[startedKey] == null) prefs[startedKey] = System.currentTimeMillis()
        }
    }

    companion object {
        private val synced = MutableStateFlow(false)
        fun resetSync() { synced.value = false }
        const val TRIAL_DAYS = 7L
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }
}

class BatteryPromptPreferences(private val context: Context) {
    private val key = booleanPreferencesKey("battery_prompt_shown")

    val shown: Flow<Boolean> = context.dataStore.data.map { it[key] ?: false }

    suspend fun markShown() {
        context.dataStore.edit { it[key] = true }
    }
}
