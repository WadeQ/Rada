package com.rada.android.features.profile

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState

data class CompleteProfileState(
    val nameInput: String = "",
    val errorMessage: String? = null
) : MviState

sealed interface CompleteProfileIntent : MviIntent {
    data class NameChanged(val value: String) : CompleteProfileIntent
    data object SaveTapped : CompleteProfileIntent
}

sealed interface CompleteProfileEffect : MviEffect {
    data object NavigateToDashboard : CompleteProfileEffect
}