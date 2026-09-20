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
}
