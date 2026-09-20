package com.rada.android.features.otp

import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthRepository
import com.rada.android.data.SignInResult
import com.rada.android.data.UserProfilePreferences
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OtpViewModel(
    private val authRepository: AuthRepository,
    private val userProfilePreferences: UserProfilePreferences,
    private val verificationId: String,
    phoneDisplay: String
) : MviViewModel<OtpIntent, OtpState, OtpEffect>(OtpState(phoneDisplay = phoneDisplay)) {

    override fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.CodeChanged -> setState { copy(code = intent.value, errorMessage = null) }
            is OtpIntent.VerifyTapped -> verify()
            is OtpIntent.ResendTapped -> Unit
        }
    }

    private fun verify() {
        if (currentState.code.length != 6) {
            setState { copy(errorMessage = "Enter the 6-digit code") }
            return
        }
        setState { copy(isVerifying = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.verifyOtp(verificationId, currentState.code)) {
                is SignInResult.Success -> {
                    setState { copy(isVerifying = false) }
                    val name = userProfilePreferences.userName.first()
                    sendEffect(
                        if (name.isNullOrBlank()) OtpEffect.NavigateToCompleteProfile
                        else OtpEffect.NavigateToDashboard
                    )
                }

                is SignInResult.Failed -> {
                    setState { copy(isVerifying = false, errorMessage = result.message) }
                    sendEffect(OtpEffect.ShowError(result.message))
                }
            }
        }
    }
}
