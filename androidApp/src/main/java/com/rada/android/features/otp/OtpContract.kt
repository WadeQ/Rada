package com.rada.android.features.otp

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState

data class OtpState(
    val phoneDisplay: String,
    val code: String = "",
    val isVerifying: Boolean = false,
    val errorMessage: String? = null
) : MviState

sealed interface OtpIntent : MviIntent {
    data class CodeChanged(val value: String) : OtpIntent
    data object VerifyTapped : OtpIntent
    data object ResendTapped : OtpIntent
}

sealed interface OtpEffect : MviEffect {
    data object NavigateToDashboard : OtpEffect
    data object NavigateToCompleteProfile : OtpEffect
    data class ShowError(val message: String) : OtpEffect
}