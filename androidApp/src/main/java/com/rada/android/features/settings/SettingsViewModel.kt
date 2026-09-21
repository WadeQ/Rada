package com.rada.android.features.settings

import androidx.lifecycle.viewModelScope
import com.rada.android.data.AlertPreferences
import com.rada.android.data.AuthRepository
import com.rada.android.data.ProfilePhotoStore
import com.rada.android.data.ThemePreferences
import com.rada.android.data.UserProfilePreferences
import com.rada.android.data.syncProfileName
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val userProfilePreferences: UserProfilePreferences,
    private val alertPreferences: AlertPreferences,
    private val photoStore: ProfilePhotoStore,
    private val authRepository: AuthRepository
) : MviViewModel<SettingsIntent, SettingsState, SettingsEffect>(SettingsState()) {

    init {
        viewModelScope.launch {
            themePreferences.themeMode.collect { mode -> setState { copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            userProfilePreferences.userName.collect { name -> setState { copy(userName = name ?: "") } }
        }
        viewModelScope.launch {
            runCatching { authRepository.reloadUser() }
            syncProfileName(authRepository, userProfilePreferences)
        }
        viewModelScope.launch {
            userProfilePreferences.photoPath.collect { path -> setState { copy(photoPath = path) } }
        }
        viewModelScope.launch {
            alertPreferences.alertsEnabled.collect { on -> setState { copy(alertsEnabled = on) } }
        }
        viewModelScope.launch {
            alertPreferences.warningLead.collect { lead -> setState { copy(warningLead = lead) } }
        }
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> viewModelScope.launch {
                themePreferences.setThemeMode(intent.mode)
            }

            is SettingsIntent.PhotoPicked -> viewModelScope.launch {
                photoStore.save(intent.uri)?.let { userProfilePreferences.setPhotoPath(it) }
            }

            is SettingsIntent.RemovePhotoTapped -> viewModelScope.launch {
                userProfilePreferences.setPhotoPath(null)
                photoStore.delete()
            }

            is SettingsIntent.WarningLeadSelected -> viewModelScope.launch {
                alertPreferences.setWarningLead(intent.lead)
            }
            is SettingsIntent.AlertsToggled -> viewModelScope.launch {
                alertPreferences.setAlertsEnabled(intent.enabled)
            }

            is SettingsIntent.SignOutTapped -> signOut()
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            userProfilePreferences.clearUserName()
            photoStore.delete()
            sendEffect(SettingsEffect.NavigateToLogin)
        }
    }
}
