package com.rada.android.features.login

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthConfig
import com.rada.android.data.AuthRepository
import com.rada.android.data.OtpEvent
import com.rada.android.data.UserProfilePreferences
import com.rada.android.domain.KenyanPhoneValidator
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userProfilePreferences: UserProfilePreferences
) : MviViewModel<LoginIntent, LoginState, LoginEffect>(LoginState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneChanged -> setState { copy(phoneInput = intent.value, errorMessage = null) }
            is LoginIntent.SubmitTapped -> Unit
        }
    }

    fun submit(activity: Activity) {
        val result = KenyanPhoneValidator.validate(currentState.phoneInput)
        if (!result.isValid) {
            setState { copy(errorMessage = result.errorMessage) }
            return
        }
        setState { copy(isValidating = true, errorMessage = null) }

        if (!AuthConfig.FIREBASE_ENABLED) {
            viewModelScope.launch {
                delay(400)
                setState { copy(isValidating = false) }
                sendEffect(nextEffectAfterSignIn())
            }
            return
        }

        viewModelScope.launch {
            authRepository.requestOtp(result.e164!!, activity).collect { event ->
                when (event) {
                    is OtpEvent.CodeSent -> {
                        setState { copy(isValidating = false) }
                        sendEffect(LoginEffect.NavigateToOtp(event.verificationId, result.e164))
                    }
                    is OtpEvent.AutoVerified -> {
                        val signInResult = authRepository.signInWithCredential(event.credential)
                        setState { copy(isValidating = false) }
                        when (signInResult) {
                            is com.rada.android.data.SignInResult.Success -> sendEffect(nextEffectAfterSignIn())
                            is com.rada.android.data.SignInResult.Failed -> sendEffect(LoginEffect.ShowError(signInResult.message))
                        }
                    }
                    is OtpEvent.Failed -> {
                        setState { copy(isValidating = false, errorMessage = event.message) }
                        sendEffect(LoginEffect.ShowError(event.message))
                    }
                }
            }
        }
    }

    private suspend fun nextEffectAfterSignIn(): LoginEffect {
        val name = userProfilePreferences.userName.first()
        return if (name.isNullOrBlank()) LoginEffect.NavigateToCompleteProfile else LoginEffect.SignedInDirectly
    }
}
