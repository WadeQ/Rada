package com.rada.android.features.settings

import com.rada.android.data.ThemeMode
import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.domain.WarningLead

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userName: String = "",
    val nameInput: String = "",
    val nameError: String? = null,
    val photoPath: String? = null,
    val alertsEnabled: Boolean = true,
    val warningLead: WarningLead = WarningLead.STANDARD
) : MviState

sealed interface SettingsIntent : MviIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
    data class NameInputChanged(val value: String) : SettingsIntent
    data class PhotoPicked(val uri: android.net.Uri) : SettingsIntent
    data object RemovePhotoTapped : SettingsIntent
    data class AlertsToggled(val enabled: Boolean) : SettingsIntent
    data class WarningLeadSelected(val lead: WarningLead) : SettingsIntent
    data object SignOutTapped : SettingsIntent
}

sealed interface SettingsEffect : MviEffect {
    data object NavigateToLogin : SettingsEffect
}
