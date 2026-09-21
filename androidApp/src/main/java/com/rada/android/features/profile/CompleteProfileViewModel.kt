package com.rada.android.features.profile

import androidx.lifecycle.viewModelScope
import com.rada.android.domain.UsernameValidator
import com.rada.android.data.UserProfilePreferences
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.launch

class CompleteProfileViewModel(
    private val userProfilePreferences: UserProfilePreferences
) : MviViewModel<CompleteProfileIntent, CompleteProfileState, CompleteProfileEffect>(
    CompleteProfileState()
) {

    override fun onIntent(intent: CompleteProfileIntent) {
        when (intent) {
            is CompleteProfileIntent.NameChanged -> setState {
                copy(
                    nameInput = intent.value,
                    errorMessage = null
                )
            }

            is CompleteProfileIntent.SaveTapped -> save()
        }
    }

    private fun save() {
        val trimmed = currentState.nameInput.trim()
        UsernameValidator.error(trimmed)?.let { message ->
            setState { copy(errorMessage = message) }
            return
        }
        viewModelScope.launch {
            userProfilePreferences.setUserName(trimmed)
            sendEffect(CompleteProfileEffect.NavigateToDashboard)
        }
    }
}
