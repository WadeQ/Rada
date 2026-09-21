package com.rada.android.features.paywall

import com.rada.android.mvi.MviEffect
import com.rada.android.mvi.MviIntent
import com.rada.android.mvi.MviState
import com.rada.shared.data.MockData

enum class PayMethod { PLAY, MPESA }

enum class PlayStatus { CHECKING, AVAILABLE, UNAVAILABLE }

data class PaywallState(
    val payMethod: PayMethod = PayMethod.PLAY,
    val playStatus: PlayStatus = PlayStatus.CHECKING,
    val playPrices: Map<String, String> = emptyMap(),
    val plans: List<MockData.PricingPlan> = MockData.pricingPlans,
    val selectedPlanIndex: Int = MockData.pricingPlans.indexOfFirst { it.recommended }.coerceAtLeast(0),
    val phoneInput: String = "",
    val isProcessing: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) : MviState

sealed interface PaywallIntent : MviIntent {
    data class PlanSelected(val index: Int) : PaywallIntent
    data class PhoneChanged(val value: String) : PaywallIntent
    data class MethodSelected(val method: PayMethod) : PaywallIntent
    data object PlaySubscribeTapped : PaywallIntent
    data object PayTapped : PaywallIntent
    data object RestoreTapped : PaywallIntent
}

sealed interface PaywallEffect : MviEffect {
    data object NavigateToDashboard : PaywallEffect
    data class ShowError(val message: String) : PaywallEffect
}
