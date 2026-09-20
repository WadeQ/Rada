package com.rada.android.features.paywall

import kotlinx.coroutines.delay

sealed interface PaymentInitiationResult {
    data class Pending(val checkoutReference: String) : PaymentInitiationResult
    data class Failed(val message: String) : PaymentInitiationResult
}

sealed interface PaymentStatus {
    data object Pending : PaymentStatus
    data object Success : PaymentStatus
    data class Failed(val message: String) : PaymentStatus
}

interface PaymentRepository {
    suspend fun initiateMpesaPayment(phoneE164: String, amountKes: Int, planId: String): PaymentInitiationResult
    suspend fun checkPaymentStatus(checkoutReference: String): PaymentStatus
}

class StubPaymentRepository : PaymentRepository {
    override suspend fun initiateMpesaPayment(phoneE164: String, amountKes: Int, planId: String): PaymentInitiationResult {
        delay(600)
        return PaymentInitiationResult.Failed(
            "Payments aren't connected to a backend yet — see PaymentRepository.kt for what's needed."
        )
    }

    override suspend fun checkPaymentStatus(checkoutReference: String): PaymentStatus {
        return PaymentStatus.Failed("No payment backend configured.")
    }
}
