package com.rada.android.features.signup

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState

data class SignUpState(
    val username: String = "",
    val password: String = "",
    val isBusy: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface SignUpIntent : MviIntent {
    data class UsernameChanged(val value: String) : SignUpIntent
    data class PasswordChanged(val value: String) : SignUpIntent
}

sealed interface SignUpEffect : MviEffect {
    data object SignedIn : SignUpEffect
    data object NavigateToCompleteProfile : SignUpEffect
}
