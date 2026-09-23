package com.rada.android.features.paywall

import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthRepository
import com.rada.android.data.PlayEvent
import com.rada.android.data.PlayBillingRepository
import com.rada.android.data.PlayProducts
import com.rada.android.data.ProRepository
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val authRepository: AuthRepository,
    private val proRepository: ProRepository,
    private val playBilling: PlayBillingRepository
) : MviViewModel<PaywallIntent, PaywallState, PaywallEffect>(
    PaywallState()
) {

    init {
        viewModelScope.launch { loadPlay() }
        viewModelScope.launch { playBilling.events.collect(::onPlayEvent) }
    }

    override fun onCleared() {
        playBilling.close()
        super.onCleared()
    }

    private suspend fun loadPlay() {
        val details = runCatching { playBilling.productDetails() }.getOrDefault(emptyMap())
        if (details.isEmpty()) {
            setState { copy(playStatus = PlayStatus.UNAVAILABLE) }
            return
        }
        val prices = details.mapNotNull { (productId, d) ->
            playBilling.formattedPrice(d)?.let { PlayProducts.planFor(productId) to it }
        }.toMap()
        setState { copy(playStatus = PlayStatus.AVAILABLE, playPrices = prices) }
        runCatching { playBilling.restore() }
    }

    private fun onPlayEvent(event: PlayEvent) {
        when (event) {
            is PlayEvent.Verified -> if (event.active) {
                setState { copy(isProcessing = false, statusMessage = null, errorMessage = null) }
                sendEffect(PaywallEffect.NavigateToDashboard)
            } else {
                setState { copy(isProcessing = false, statusMessage = null, errorMessage = "Your subscription isn't active yet.") }
            }

            is PlayEvent.Pending -> setState {
                copy(isProcessing = false, statusMessage = "Your payment is pending. Pro turns on as soon as it clears.", errorMessage = null)
            }

            is PlayEvent.Cancelled -> setState { copy(isProcessing = false, statusMessage = null) }
            is PlayEvent.Failed -> setState { copy(isProcessing = false, statusMessage = null, errorMessage = event.message) }
        }
    }

    fun subscribeWithPlay(activity: android.app.Activity) {
        if (currentState.isProcessing) return
        val plan = currentState.plans[currentState.selectedPlanIndex]
        setState { copy(isProcessing = true, errorMessage = null, statusMessage = "Opening Google Play…") }
        viewModelScope.launch {
            val problem = playBilling.launch(activity, plan.id)
            if (problem != null) {
                setState { copy(isProcessing = false, statusMessage = null, errorMessage = problem) }
            }
        }
    }

    override fun onIntent(intent: PaywallIntent) {
        when (intent) {
            is PaywallIntent.PlanSelected -> setState { copy(selectedPlanIndex = intent.index) }
            is PaywallIntent.PlaySubscribeTapped -> Unit
            is PaywallIntent.RestoreTapped -> restore()
        }
    }

    private fun restore() {
        if (currentState.isProcessing) return
        setState { copy(isProcessing = true, errorMessage = null, statusMessage = "Checking your account…") }
        viewModelScope.launch {
            runCatching { playBilling.restore() }
            val until = runCatching { proRepository.fetchProUntilMillis() }.getOrNull()
            if (until != null && until > System.currentTimeMillis()) {
                setState { copy(isProcessing = false, statusMessage = null) }
                sendEffect(PaywallEffect.NavigateToDashboard)
            } else {
                setState { copy(isProcessing = false, statusMessage = null, errorMessage = "No active Pro subscription found on this account.") }
            }
        }
    }
}
