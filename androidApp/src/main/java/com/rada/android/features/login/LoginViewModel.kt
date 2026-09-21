package com.rada.android.features.login

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthRepository
import com.rada.android.data.SignInResult
import com.rada.android.data.TrialPreferences
import com.rada.android.data.UserProfilePreferences
import com.rada.android.data.syncProfileName
import com.rada.android.data.syncTrial
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userProfilePreferences: UserProfilePreferences,
    private val trialPreferences: TrialPreferences
) : MviViewModel<LoginIntent, LoginState, LoginEffect>(LoginState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> setState { copy(username = intent.value.filter { !it.isWhitespace() }, errorMessage = null) }
            is LoginIntent.PasswordChanged -> setState { copy(password = intent.value, errorMessage = null) }
        }
    }

    fun signIn() {
        if (currentState.isBusy) return
        val username = currentState.username.trim()
        if (username.isEmpty() || currentState.password.isEmpty()) {
            setState { copy(errorMessage = "Enter your username and password") }
            return
        }
        run { authRepository.signInWithUsername(username, currentState.password) }
    }

    fun signInWithGoogle(activity: Activity) = run { authRepository.signInWithGoogle(activity) }

    fun signInWithApple(activity: Activity) = run { authRepository.signInWithApple(activity) }

    fun tryOneTap(activity: Activity) = run { authRepository.signInWithGoogleOneTap(activity, authorizedOnly = true) }

    private fun run(block: suspend () -> SignInResult) {
        if (currentState.isBusy) return
        setState { copy(isBusy = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = block()) {
                is SignInResult.Success -> {
                    syncProfileName(authRepository, userProfilePreferences)
                    syncTrial(trialPreferences)
                    val name = userProfilePreferences.userName.first()
                    sendEffect(if (name.isNullOrBlank()) LoginEffect.NavigateToCompleteProfile else LoginEffect.SignedInDirectly)
                }
                is SignInResult.Failed -> setState { copy(errorMessage = result.message) }
                is SignInResult.Cancelled -> Unit
            }
            setState { copy(isBusy = false) }
        }
    }
}
