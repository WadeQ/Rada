package com.rada.android.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import kotlin.coroutines.resume

object PlayProducts {
    const val MONTHLY = "rada_pro_monthly"
    const val ANNUAL = "rada_pro_annual"
    val ALL = listOf(MONTHLY, ANNUAL)

    fun forPlan(planId: String) = if (planId == "annual") ANNUAL else MONTHLY
    fun planFor(productId: String) = if (productId == ANNUAL) "annual" else "monthly"
}

sealed interface PlayEvent {
    data class Verified(val active: Boolean) : PlayEvent
    data object Pending : PlayEvent
    data object Cancelled : PlayEvent
    data class Failed(val message: String) : PlayEvent
}

class PlayBillingRepository(
    context: Context,
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("europe-west1")
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _events = MutableSharedFlow<PlayEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<PlayEvent> = _events.asSharedFlow()

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(PurchasesUpdatedListener { result, purchases -> onPurchasesUpdated(result, purchases) })
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    suspend fun connect(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { continuation ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() = Unit
            })
        }
    }

    suspend fun productDetails(): Map<String, ProductDetails> {
        if (!connect()) return emptyMap()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                PlayProducts.ALL.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()
        val result = client.queryProductDetails(params)
        return result.productDetailsList.orEmpty().associateBy { it.productId }
    }

    fun formattedPrice(details: ProductDetails): String? =
        details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice

    suspend fun launch(activity: Activity, planId: String): String? {
        val details = productDetails()[PlayProducts.forPlan(planId)]
            ?: return "This plan isn't available on Google Play yet."
        val offer = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return "This plan isn't available on Google Play yet."
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return "Sign in to subscribe."
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offer)
                        .build()
                )
            )
            .setObfuscatedAccountId(accountHash(uid))
            .build()
        val result = client.launchBillingFlow(activity, params)
        return if (result.responseCode == BillingClient.BillingResponseCode.OK) null
        else "Google Play couldn't start the purchase. Try again."
    }

    suspend fun restore(): Boolean {
        if (!connect()) return false
        val owned = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ).purchasesList
        var active = false
        for (purchase in owned) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) continue
            val event = verify(purchase)
            if (event is PlayEvent.Verified && event.active) active = true
        }
        return active
    }

    fun close() {
        scope.cancel()
        if (client.isReady) client.endConnection()
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { purchase ->
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> scope.launch { _events.emit(verify(purchase)) }
                    Purchase.PurchaseState.PENDING -> _events.tryEmit(PlayEvent.Pending)
                    else -> Unit
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> _events.tryEmit(PlayEvent.Cancelled)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch {
                _events.emit(PlayEvent.Verified(active = restore()))
            }

            else -> _events.tryEmit(PlayEvent.Failed("Google Play couldn't complete the purchase."))
        }
    }

    private suspend fun verify(purchase: Purchase): PlayEvent = try {
        val data = functions.getHttpsCallable("verifyPlaySubscription")
            .call(mapOf("productId" to purchase.products.first(), "purchaseToken" to purchase.purchaseToken))
            .await().data as? Map<*, *>
        PlayEvent.Verified(active = data?.get("active") == true)
    } catch (e: Exception) {
        PlayEvent.Failed("We couldn't confirm your purchase yet. Tap Restore purchase in a moment.")
    }

    companion object {
        fun accountHash(uid: String): String =
            MessageDigest.getInstance("SHA-256").digest(uid.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .take(32)
    }
}
