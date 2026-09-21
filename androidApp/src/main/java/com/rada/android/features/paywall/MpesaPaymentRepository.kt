package com.rada.android.features.paywall

import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.FirebaseFunctionsException.Code
import kotlinx.coroutines.tasks.await

class MpesaPaymentRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(REGION)
) : PaymentRepository {

    override suspend fun initiateMpesaPayment(
        phoneE164: String,
        amountKes: Int,
        planId: String
    ): PaymentInitiationResult = try {
        val data = functions.getHttpsCallable("initiateMpesaPayment")
            .call(mapOf("phone" to phoneE164, "planId" to planId))
            .await().data as? Map<*, *>
        val checkoutId = data?.get("checkoutRequestId") as? String
        if (checkoutId != null) PaymentInitiationResult.Pending(checkoutId)
        else PaymentInitiationResult.Failed("Couldn't start the M-Pesa prompt. Try again.")
    } catch (e: Exception) {
        PaymentInitiationResult.Failed(userMessage(e))
    }

    override suspend fun checkPaymentStatus(checkoutReference: String): PaymentStatus = try {
        val data = functions.getHttpsCallable("queryMpesaPayment")
            .call(mapOf("checkoutRequestId" to checkoutReference))
            .await().data as? Map<*, *>
        when (data?.get("status") as? String) {
            "success" -> PaymentStatus.Success
            "cancelled" -> PaymentStatus.Failed("You cancelled the payment.")
            "failed" -> PaymentStatus.Failed((data["message"] as? String) ?: "The payment didn't go through.")
            else -> PaymentStatus.Pending
        }
    } catch (e: Exception) {
        if (e is FirebaseFunctionsException && e.code in TRANSIENT) PaymentStatus.Pending
        else PaymentStatus.Failed(userMessage(e))
    }

    private fun userMessage(e: Exception): String =
        if (e is FirebaseFunctionsException && e.code in SHOWN_AS_IS && !e.message.isNullOrBlank()) e.message!!
        else "Couldn't reach the payment service. Check your connection and try again."

    private companion object {
        const val REGION = "europe-west1"
        val TRANSIENT = setOf(Code.UNAVAILABLE, Code.DEADLINE_EXCEEDED, Code.INTERNAL)
        val SHOWN_AS_IS = setOf(
            Code.INVALID_ARGUMENT, Code.RESOURCE_EXHAUSTED, Code.UNAVAILABLE,
            Code.FAILED_PRECONDITION, Code.UNAUTHENTICATED, Code.NOT_FOUND
        )
    }
}
