package com.rada.android.features.login

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isBusy: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface LoginIntent : MviIntent {
    data class UsernameChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
}

sealed interface LoginEffect : MviEffect {
    data object SignedInDirectly : LoginEffect
    data object NavigateToCompleteProfile : LoginEffect
}
