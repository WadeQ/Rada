package com.rada.android.features.settings

import com.rada.android.data.ThemeMode
import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState

/**
 * Every field here is backed by DataStore and written as soon as it changes — there is no
 * separate "save" step. [nameInput] is just the live text of the name field.
 */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userName: String = "",
    val nameInput: String = "",
    val nameError: String? = null,
    val photoPath: String? = null,
    val alertsEnabled: Boolean = true
) : MviState

sealed interface SettingsIntent : MviIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
    data class NameInputChanged(val value: String) : SettingsIntent
    data class PhotoPicked(val uri: android.net.Uri) : SettingsIntent
    data object RemovePhotoTapped : SettingsIntent
    data class AlertsToggled(val enabled: Boolean) : SettingsIntent
    data object SignOutTapped : SettingsIntent
}

sealed interface SettingsEffect : MviEffect {
    data object NavigateToLogin : SettingsEffect
}
