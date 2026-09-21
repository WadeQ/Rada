package com.rada.android.features.settings

import androidx.lifecycle.viewModelScope
import com.rada.android.domain.UsernameValidator
import com.rada.android.data.AlertPreferences
import com.rada.android.data.AuthRepository
import com.rada.android.data.ProfilePhotoStore
import com.rada.android.data.ThemePreferences
import com.rada.android.data.UserProfilePreferences
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val userProfilePreferences: UserProfilePreferences,
    private val alertPreferences: AlertPreferences,
    private val photoStore: ProfilePhotoStore,
    private val authRepository: AuthRepository
) : MviViewModel<SettingsIntent, SettingsState, SettingsEffect>(SettingsState()) {

    private var nameSaveJob: Job? = null

    init {
        viewModelScope.launch {
            themePreferences.themeMode.collect { mode -> setState { copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            userProfilePreferences.userName.collect { name ->
                val saved = name ?: ""
                setState {
                    val typing = nameSaveJob?.isActive == true
                    copy(userName = saved, nameInput = if (typing) nameInput else saved)
                }
            }
        }
        viewModelScope.launch {
            userProfilePreferences.photoPath.collect { path -> setState { copy(photoPath = path) } }
        }
        viewModelScope.launch {
            alertPreferences.alertsEnabled.collect { on -> setState { copy(alertsEnabled = on) } }
        }
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected -> viewModelScope.launch {
                themePreferences.setThemeMode(intent.mode)
            }

            is SettingsIntent.NameInputChanged -> onNameChanged(intent.value)

            is SettingsIntent.PhotoPicked -> viewModelScope.launch {
                photoStore.save(intent.uri)?.let { userProfilePreferences.setPhotoPath(it) }
            }

            is SettingsIntent.RemovePhotoTapped -> viewModelScope.launch {
                userProfilePreferences.setPhotoPath(null)
                photoStore.delete()
            }

            is SettingsIntent.AlertsToggled -> viewModelScope.launch {
                alertPreferences.setAlertsEnabled(intent.enabled)
            }

            is SettingsIntent.SignOutTapped -> signOut()
        }
    }

    private fun onNameChanged(value: String) {
        nameSaveJob?.cancel()
        val trimmed = value.trim()
        UsernameValidator.error(trimmed)?.let { message ->
            setState { copy(nameInput = value, nameError = message) }
            return
        }
        setState { copy(nameInput = value, nameError = null) }
        nameSaveJob = viewModelScope.launch {
            delay(NAME_SAVE_DEBOUNCE_MS)
            if (trimmed != currentState.userName) {
                userProfilePreferences.setUserName(trimmed)
                authRepository.updateDisplayName(trimmed)
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            nameSaveJob?.cancel()
            authRepository.signOut()
            userProfilePreferences.clearUserName()
            photoStore.delete()
            sendEffect(SettingsEffect.NavigateToLogin)
        }
    }

    private companion object {
        const val NAME_SAVE_DEBOUNCE_MS = 600L
    }
}
