package com.rada.android.features.paywall

import androidx.lifecycle.viewModelScope
import com.rada.android.data.AuthRepository
import com.rada.android.domain.KenyanPhoneValidator
import com.rada.android.features.paywall.PaywallEffect.*
import com.rada.android.mvi.MviViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository
) : MviViewModel<PaywallIntent, PaywallState, PaywallEffect>(
    PaywallState(phoneInput = authRepository.currentUserPhone().orEmpty())
) {

    override fun onIntent(intent: PaywallIntent) {
        when (intent) {
            is PaywallIntent.PlanSelected -> setState { copy(selectedPlanIndex = intent.index) }
            is PaywallIntent.PhoneChanged -> setState {
                copy(
                    phoneInput = intent.value,
                    errorMessage = null
                )
            }

            is PaywallIntent.PayTapped -> startPayment()
            is PaywallIntent.RestoreTapped -> {
                // TODO: a real implementation checks subscription status against your backend
                // (or Play Billing, if this ever also sells through Play). Not built — there's
                // no backend yet for it to check against.
                sendEffect(PaywallEffect.ShowError("Restore isn't available yet."))
            }
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

        viewModelScope.launch {
            when (val initiation =
                result.e164?.let {
                    paymentRepository.initiateMpesaPayment(
                        it, plan.amountKes, plan.id)
                }) {
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

                null -> TODO()
            }
        }
    }

    /**
     * Polls rather than waiting for a push/websocket notification — simpler to reason about
     * for a first version, at the cost of a short delay after the user actually completes
     * payment on their phone before the app notices. A real build would ideally have the
     * backend push a Firestore/websocket update instead of the app polling it.
     */
    private suspend fun pollForCompletion(checkoutReference: String) {
        repeat(10) {
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

                is PaymentStatus.Pending -> Unit // keep polling
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
}