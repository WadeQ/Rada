package com.rada.android.features.signup

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthRepository
import com.rada.android.data.SignInResult
import com.rada.android.data.UserProfilePreferences
import com.rada.android.data.syncProfileName
import com.rada.android.domain.UsernameValidator
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userProfilePreferences: UserProfilePreferences
) : MviViewModel<SignUpIntent, SignUpState, SignUpEffect>(SignUpState()) {

    override fun onIntent(intent: SignUpIntent) {
        when (intent) {
            is SignUpIntent.UsernameChanged -> setState { copy(username = intent.value.filter { !it.isWhitespace() }, errorMessage = null) }
            is SignUpIntent.PasswordChanged -> setState { copy(password = intent.value, errorMessage = null) }
        }
    }

    fun signUp() {
        if (currentState.isBusy) return
        val username = currentState.username.trim()
        (UsernameValidator.accountError(username) ?: UsernameValidator.passwordError(currentState.password))?.let { message ->
            setState { copy(errorMessage = message) }
            return
        }
        run { authRepository.signUpWithUsername(username, currentState.password) }
    }

    fun signUpWithGoogle(activity: Activity) = run { authRepository.signInWithGoogle(activity) }

    fun signUpWithApple(activity: Activity) = run { authRepository.signInWithApple(activity) }

    fun tryOneTap(activity: Activity) = run { authRepository.signInWithGoogleOneTap(activity, authorizedOnly = false) }

    private fun run(block: suspend () -> SignInResult) {
        if (currentState.isBusy) return
        setState { copy(isBusy = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = block()) {
                is SignInResult.Success -> {
                    syncProfileName(authRepository, userProfilePreferences)
                    val name = userProfilePreferences.userName.first()
                    sendEffect(if (name.isNullOrBlank()) SignUpEffect.NavigateToCompleteProfile else SignUpEffect.SignedIn)
                }
                is SignInResult.Failed -> setState { copy(errorMessage = result.message) }
                is SignInResult.Cancelled -> Unit
            }
            setState { copy(isBusy = false) }
        }
    }
}
