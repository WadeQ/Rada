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
            is LoginIntent.SubmitTapped -> Unit // handled via submit(activity) below, needs an Activity for Firebase's phone-auth flow
        }
    }

    /**
     * Firebase's phone-auth API needs an Activity reference (for the reCAPTCHA fallback UI),
     * which the MVI intent alone can't carry cleanly — so submission is a direct method call
     * from the Route screen rather than routed purely through onIntent. This is a deliberate,
     * narrow exception to "everything goes through onIntent", not a pattern to repeat elsewhere.
     */
    fun submit(activity: Activity) {
        val result = KenyanPhoneValidator.validate(currentState.phoneInput)
        if (!result.isValid) {
            setState { copy(errorMessage = result.errorMessage) }
            return
        }
        setState { copy(isValidating = true, errorMessage = null) }

        if (!AuthConfig.FIREBASE_ENABLED) {
            // Stubbed: skip the real SMS/OTP round trip entirely so the app is clickable for
            // UI testing before Firebase console setup (phone provider, SHA-1, Play Integrity)
            // is done. Flip FIREBASE_ENABLED above to restore the real flow.
            viewModelScope.launch {
                delay(400) // small delay so the slide-to-confirm's "Signing in" state is visible at all
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
                        // Some devices confirm via SMS Retriever without the user ever typing
                        // a code — sign in immediately instead of routing through the OTP
                        // screen at all, since there's no code for the user to enter.
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

    /**
     * Returning users (who already have a saved name from a previous sign-in) go straight to
     * Dashboard; first-time sign-ins are routed to CompleteProfile first — same one-time-only
     * principle as onboarding. This check runs on every "signed in" path (both the stub above
     * and the real Firebase auto-verify path) so neither one can accidentally skip it.
     */
    private suspend fun nextEffectAfterSignIn(): LoginEffect {
        val name = userProfilePreferences.userName.first()
        return if (name.isNullOrBlank()) LoginEffect.NavigateToCompleteProfile else LoginEffect.SignedInDirectly
    }
}