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

/**
 * M-Pesa STK Push cannot be called directly from this Android app under any circumstances —
 * Safaricom's Daraja API requires a Consumer Secret held server-side, and a Consumer Secret
 * embedded in an APK is extractable by decompiling it. A backend is mandatory here, not a
 * style choice. This interface is shaped around calling YOUR OWN backend (not Safaricom's
 * Daraja API directly, and not an aggregator's API directly either):
 *
 *   POST https://your-backend.example.com/api/payments/mpesa/initiate
 *   body: { phone: "+2547...", amountKes: 100, planId: "annual" }
 *   response: { checkoutReference: "ws_CO_..." }
 *
 *   GET https://your-backend.example.com/api/payments/status/{checkoutReference}
 *   response: { status: "pending" | "success" | "failed" }
 *
 * Your backend is the only thing that should ever hold Safaricom Daraja credentials (or your
 * chosen aggregator's secret key, if using Flutterwave/Paystack/IntaSend instead of going
 * direct). It receives the payment-confirmation webhook from Safaricom/the aggregator, updates
 * its own database, and this repository's checkPaymentStatus() polls YOUR backend for the
 * result — never Safaricom directly from the phone.
 */
interface PaymentRepository {
    suspend fun initiateMpesaPayment(phoneE164: String, amountKes: Int, planId: String): PaymentInitiationResult
    suspend fun checkPaymentStatus(checkoutReference: String): PaymentStatus
}

/**
 * STUB — deliberately does not pretend to succeed. No backend exists yet, so this returns an
 * honest "not configured" failure rather than a fake success that would misrepresent whether
 * money actually moved. Replace with a real implementation (e.g. Retrofit/Ktor calls to your
 * backend) once that backend exists — the PaywallViewModel calling this doesn't need to
 * change, only this class does.
 */
class StubPaymentRepository : PaymentRepository {
    override suspend fun initiateMpesaPayment(phoneE164: String, amountKes: Int, planId: String): PaymentInitiationResult {
        delay(600) // simulates a network round trip
        return PaymentInitiationResult.Failed(
            "Payments aren't connected to a backend yet — see PaymentRepository.kt for what's needed."
        )
    }

    override suspend fun checkPaymentStatus(checkoutReference: String): PaymentStatus {
        return PaymentStatus.Failed("No payment backend configured.")
    }
}