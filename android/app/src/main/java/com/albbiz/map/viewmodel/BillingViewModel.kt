// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.albbiz.map.data.BusinessRepository
import com.albbiz.map.ui.CurrentLanguage
import com.android.billingclient.api.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "BillingViewModel"

// Surfaced to SubscriptionScreen after a purchase is acknowledged by Google Play
// (meaning the user has genuinely paid) so the Firestore side of activating the
// plan is never silently invisible — see handlePurchase()'s comment for the full
// list of failure points this now covers, all of which used to just do nothing.
sealed class SubscriptionUpdateState {
    object Idle : SubscriptionUpdateState()
    object Success : SubscriptionUpdateState()
    data class Error(val message: String) : SubscriptionUpdateState()
}

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BusinessRepository()

    private val _subscriptionUpdateState = MutableStateFlow<SubscriptionUpdateState>(SubscriptionUpdateState.Idle)
    val subscriptionUpdateState: StateFlow<SubscriptionUpdateState> = _subscriptionUpdateState.asStateFlow()

    fun clearSubscriptionUpdateState() {
        _subscriptionUpdateState.value = SubscriptionUpdateState.Idle
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    // Play Billing Library 8 removed the no-arg enablePendingPurchases(). Per
    // the migration guide, PendingPurchasesParams.newBuilder().enableOneTimeProducts()
    // is the exact functional equivalent of the old no-arg call, so behavior
    // here is unchanged — just the required syntax for PBL 8+.
    private var billingClient = BillingClient.newBuilder(application)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _isBillingConnected = MutableStateFlow(false)
    val isBillingConnected: StateFlow<Boolean> = _isBillingConnected.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    // Keep track of which business is currently being upgraded
    private var pendingBusinessId: String? = null

    init {
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingConnected.value = true
                    queryProducts()
                    queryExistingPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                _isBillingConnected.value = false
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium_subscription")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("featured_subscription")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("sponsored_subscription")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        // PBL 8 changed this callback's signature: it now hands back a
        // QueryProductDetailsResult (successfully fetched products plus an
        // unfetchedProductList for ones that couldn't be found) instead of a
        // plain List<ProductDetails> directly.
        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = queryProductDetailsResult.productDetailsList
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String, businessId: String) {
        val productDetails = _products.value.find { it.productId == productId } ?: return
        pendingBusinessId = businessId

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        // pendingBusinessId alone doesn't survive process death (e.g. the app
        // gets killed while the Play billing sheet is up, or between Google's
        // acknowledgement and our Firestore write). Stamping the businessId as
        // the obfuscated account id means Google hands it straight back on the
        // resulting Purchase object — including later, via queryExistingPurchases()
        // on a fresh process — so we can always recover which business a purchase
        // belongs to instead of just losing track of it.
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(businessId)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    // Re-checks Google Play for any purchase this device already made that
    // never finished being processed in-app — most commonly a subscription
    // renewal that happened in the background, or a purchase interrupted by
    // the app/process dying mid-flow. Safe to call every time the billing
    // connection comes up (including every app start): handlePurchase() already
    // no-ops immediately for anything already acknowledged, so this only ever
    // does work for the exact purchases that previously would have silently
    // sat unacknowledged until Google auto-refunded them after 3 days.
    private fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else {
                Log.e(TAG, "queryPurchasesAsync failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
            }
        }
    }

    // Retries the Firestore write a few times with a short backoff before
    // giving up — this is the step that actually activates what the user just
    // paid Google for, so a single transient network blip shouldn't be enough
    // to leave a real, acknowledged purchase with no plan applied.
    private suspend fun updateSubscriptionWithRetry(
        businessId: String,
        tier: String,
        maxAttempts: Int = 3
    ): Result<Unit> {
        var lastError: Throwable? = null
        repeat(maxAttempts) { attempt ->
            val result = repository.updateSubscription(businessId, tier)
            if (result.isSuccess) return result
            lastError = result.exceptionOrNull()
            Log.e(TAG, "updateSubscription attempt ${attempt + 1}/$maxAttempts failed for business=$businessId tier=$tier", lastError)
            if (attempt < maxAttempts - 1) delay(1000L * (attempt + 1))
        }
        return Result.failure(lastError ?: Exception("Unknown error updating subscription"))
    }

    // Every branch here used to just silently do nothing on failure — the user
    // had already paid Google Play at this point, so any of these failing left
    // a real charge with no corresponding plan activated and no way for anyone
    // (the user, or you) to know something went wrong. Each one now logs with
    // enough detail to trace the specific purchase, and updates
    // _subscriptionUpdateState so SubscriptionScreen can tell the user directly
    // rather than them just seeing nothing happen.
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (purchase.isAcknowledged) return

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "acknowledgePurchase failed: ${billingResult.responseCode} ${billingResult.debugMessage}, token=${purchase.purchaseToken}")
                _subscriptionUpdateState.value = SubscriptionUpdateState.Error(
                    String.format(CurrentLanguage.strings().purchaseAckFailedTemplate, billingResult.responseCode.toString())
                )
                return@acknowledgePurchase
            }

            // The obfuscated account id (stamped in launchBillingFlow) survives
            // process death and reconciliation via queryExistingPurchases() on a
            // fresh app start; pendingBusinessId is just a same-process fallback
            // for purchases made before this field existed on the purchase.
            val businessId = purchase.accountIdentifiers?.obfuscatedAccountId ?: pendingBusinessId
            if (businessId.isNullOrBlank()) {
                // Purchase.products/purchaseToken are still logged so this is
                // traceable and fixable manually if it ever happens.
                Log.e(TAG, "Purchase acknowledged but no businessId could be resolved — products=${purchase.products}, token=${purchase.purchaseToken}")
                _subscriptionUpdateState.value = SubscriptionUpdateState.Error(
                    CurrentLanguage.strings().purchaseLostBusinessError
                )
                return@acknowledgePurchase
            }

            val productId = purchase.products.firstOrNull()
            val tier = when (productId) {
                "premium_subscription" -> "premium"
                "featured_subscription" -> "featured"
                "sponsored_subscription" -> "sponsored"
                else -> null
            }

            if (tier == null) {
                Log.e(TAG, "Purchase acknowledged but productId didn't match a known tier: productId=$productId, businessId=$businessId")
                _subscriptionUpdateState.value = SubscriptionUpdateState.Error(
                    CurrentLanguage.strings().purchaseUnknownProductError
                )
                return@acknowledgePurchase
            }

            viewModelScope.launch {
                updateSubscriptionWithRetry(businessId, tier)
                    .onSuccess {
                        _subscriptionUpdateState.value = SubscriptionUpdateState.Success
                    }
                    .onFailure { e ->
                        Log.e(TAG, "updateSubscription permanently failed after retries for business=$businessId tier=$tier", e)
                        _subscriptionUpdateState.value = SubscriptionUpdateState.Error(
                            String.format(CurrentLanguage.strings().purchaseActivationFailedTemplate, e.message ?: "")
                        )
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingClient.endConnection()
    }
}
