package com.rada.android.features.help

import com.rada.android.mvi.MviViewModel

class HelpViewModel : MviViewModel<HelpIntent, HelpState, HelpEffect>(HelpState()) {
    override fun onIntent(intent: HelpIntent) {
        when (intent) {
            is HelpIntent.CallTapped -> sendEffect(HelpEffect.LaunchDialer(intent.contact.phone))
        }
    }
}
