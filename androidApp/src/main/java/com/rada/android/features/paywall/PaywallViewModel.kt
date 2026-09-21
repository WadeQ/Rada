package com.rada.android.features.paywall

import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthRepository
import com.rada.android.data.PlayEvent
import com.rada.android.data.PlayBillingRepository
import com.rada.android.data.PlayProducts
import com.rada.android.data.ProRepository
import com.rada.android.domain.KenyanPhoneValidator
import com.rada.android.features.paywall.PaywallEffect.*
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository,
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
            setState { copy(playStatus = PlayStatus.UNAVAILABLE, payMethod = PayMethod.MPESA) }
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
            is PaywallIntent.MethodSelected -> setState { copy(payMethod = intent.method, errorMessage = null, statusMessage = null) }
            is PaywallIntent.PlaySubscribeTapped -> Unit
            is PaywallIntent.PlanSelected -> setState { copy(selectedPlanIndex = intent.index) }
            is PaywallIntent.PhoneChanged -> setState {
                copy(
                    phoneInput = KenyanPhoneValidator.localDigits(intent.value),
                    errorMessage = null
                )
            }

            is PaywallIntent.PayTapped -> startPayment()
            is PaywallIntent.RestoreTapped -> restore()
        }
    }

    private fun startPayment() {
        val result = KenyanPhoneValidator.validate(currentState.phoneInput)
        if (!result.isValid) {
            setState { copy(errorMessage = result.errorMessage) }
            return
        }
        val plan = currentState.plans[currentState.selectedPlanIndex]
        setState {
            copy(
                isProcessing = true,
                errorMessage = null,
                statusMessage = "Starting payment…"
            )
        }

        val e164 = result.e164 ?: run {
            setState { copy(isProcessing = false, statusMessage = null, errorMessage = "Enter a valid Safaricom number") }
            return
        }
        viewModelScope.launch {
            when (val initiation = paymentRepository.initiateMpesaPayment(e164, plan.amountKes, plan.id)) {
                is PaymentInitiationResult.Failed -> {
                    setState {
                        copy(
                            isProcessing = false,
                            statusMessage = null,
                            errorMessage = initiation.message
                        )
                    }
                    sendEffect(ShowError(initiation.message))
                }

                is PaymentInitiationResult.Pending -> {
                    setState { copy(statusMessage = "Check your phone for the M-Pesa prompt…") }
                    pollForCompletion(initiation.checkoutReference)
                }
            }
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

    private suspend fun pollForCompletion(checkoutReference: String) {
        repeat(POLL_ATTEMPTS) {
            delay(3000)
            when (val status = paymentRepository.checkPaymentStatus(checkoutReference)) {
                is PaymentStatus.Success -> {
                    setState { copy(isProcessing = false, statusMessage = null) }
                    sendEffect(PaywallEffect.NavigateToDashboard)
                    return
                }

                is PaymentStatus.Failed -> {
                    setState {
                        copy(
                            isProcessing = false,
                            statusMessage = null,
                            errorMessage = status.message
                        )
                    }
                    sendEffect(PaywallEffect.ShowError(status.message))
                    return
                }

                is PaymentStatus.Pending -> Unit
            }
        }
        setState {
            copy(
                isProcessing = false,
                statusMessage = null,
                errorMessage = "Payment timed out — try again."
            )
        }
    }

    private companion object {
        const val POLL_ATTEMPTS = 24
    }
}
