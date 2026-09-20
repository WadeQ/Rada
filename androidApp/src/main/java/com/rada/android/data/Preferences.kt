package com.rada.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "rada_prefs")

// Epoch-millis timestamps of reports submitted from this device, comma-separated. Declared at
// file level so sign-out (UserProfilePreferences) can clear it alongside the rest of the profile.
private val reportTimesKey = stringPreferencesKey("my_report_times")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Backs "on the fly" dark/light switching: RadaTheme reads this as a Flow, so changing the
 * preference (e.g. from a future Settings screen) recomposes the whole app's colors
 * immediately — no restart, no cached ColorScheme baked in at launch.
 */
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

/**
 * Gates onboarding to first-time users only, per the requirement that returning users go
 * straight to login (phone/biometric) and never see onboarding again.
 */
class OnboardingPreferences(private val context: Context) {
    private val key = booleanPreferencesKey("onboarding_complete")

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[key] ?: false
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs -> prefs[key] = true }
    }
}

/**
 * Collected once, right after a user's first successful sign-in — not asked again on
 * returning logins, same one-time-only principle as OnboardingPreferences above. Phone
 * number lives in Firebase Auth already (that's what signs you in); this only needs the
 * display name/username the phone number doesn't carry.
 */
class UserProfilePreferences(private val context: Context) {
    private val key = stringPreferencesKey("user_display_name")

    val userName: Flow<String?> = context.dataStore.data.map { prefs -> prefs[key] }

    private val photoKey = stringPreferencesKey("user_photo_path")

    /** Absolute path of the app-private copy of the chosen profile photo, or null for the initials avatar. */
    val photoPath: Flow<String?> = context.dataStore.data.map { prefs -> prefs[photoKey] }

    suspend fun setPhotoPath(path: String?) {
        context.dataStore.edit { prefs ->
            if (path == null) prefs.remove(photoKey) else prefs[photoKey] = path
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs -> prefs[key] = name }
    }

    /** Used by Settings' sign-out — without this, signing out of Firebase but leaving the
     *  saved name in place would mean Splash's returning-user check (see AuthConfig) still
     *  treats the device as "already signed up" and skips straight past Login. */
    suspend fun clearUserName() {
        context.dataStore.edit { prefs -> prefs.remove(key); prefs.remove(photoKey); prefs.remove(reportTimesKey) }
    }
}

/**
 * Whether the user wants proximity alert notifications. Persisted so the Settings toggle keeps
 * its value across launches; defaults to on since alerts are the app's core purpose.
 */
class AlertPreferences(private val context: Context) {
    private val key = booleanPreferencesKey("alerts_enabled")

    val alertsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[key] ?: true }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[key] = enabled }
    }
}

/**
 * A local record of when this device submitted reports, backing the "reports this week"
 * counter. Reports carry no user id yet (sign-in is still stubbed), so this can't be a backend
 * query — it counts only what was submitted from this device, and is cleared on sign-out.
 */
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
