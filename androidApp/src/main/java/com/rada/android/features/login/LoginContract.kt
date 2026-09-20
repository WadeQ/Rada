package com.rada.android.features.login

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState

data class LoginState(
    val phoneInput: String = "",
    val isValidating: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface LoginIntent : MviIntent {
    data class PhoneChanged(val value: String) : LoginIntent
    data object SubmitTapped : LoginIntent
}

sealed interface LoginEffect : MviEffect {
    data class NavigateToOtp(val verificationId: String, val e164Phone: String) : LoginEffect
    data object SignedInDirectly : LoginEffect
    data object NavigateToCompleteProfile : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}