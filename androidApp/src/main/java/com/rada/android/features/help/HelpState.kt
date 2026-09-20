package com.rada.android.features.help

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.data.MockData

data class HelpState(
    val contacts: List<MockData.Contact> = MockData.roadsideContacts
) : MviState

sealed interface HelpIntent : MviIntent {
    data class CallTapped(val contact: MockData.Contact) : HelpIntent
}

sealed interface HelpEffect : MviEffect {
    data class LaunchDialer(val phoneNumber: String) : HelpEffect
}