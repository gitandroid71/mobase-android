package dev.mobase.purchases.google

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.GetBillingConfigParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import dev.mobase.common.coroutines.Dispatchers
import dev.mobase.purchases.Purchases
import dev.mobase.purchases.google.identifiers.AccountIdentifiersProvider
import dev.mobase.purchases.google.util.isFailure
import dev.mobase.purchases.google.util.isSuccess
import dev.mobase.purchases.google.util.toProduct
import dev.mobase.purchases.model.Entitlements
import dev.mobase.purchases.model.ErrorCode
import dev.mobase.purchases.model.Product
import dev.mobase.purchases.model.PurchaseTransaction
import dev.mobase.purchases.model.PurchasesError
import dev.mobase.purchases.model.PurchasesException
import dev.mobase.purchases.model.Storefront
import dev.mobase.purchases.storage.DefaultPurchasesStorage
import dev.mobase.purchases.storage.PurchasesStorage
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume


@OptIn(FlowPreview::class)
class GooglePlayPurchases(
    applicationContext: Context,
    private val accountIdentifiersProvider: AccountIdentifiersProvider,
    private val storage: PurchasesStorage = DefaultPurchasesStorage(applicationContext),
    private val dispatchers: Dispatchers = Dispatchers(),
) : Purchases {

    private companion object {
        const val FOREGROUND_DEBOUNCE_MS = 30_000L
        const val PURCHASE_UPDATE_DELAY_MS = 300L
    }

    /**
     * Completes when the BillingClient is connected. Replaced with a new incomplete deferred on
     * each disconnect so that callers block until the next successful reconnect.
     */
    @Volatile
    private var connectionDeferred: CompletableDeferred<Unit> = CompletableDeferred()

    private val isConnecting = AtomicBoolean(false)

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)

    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            Timber.d("Billing service disconnected — resetting connection deferred")
            isConnecting.set(false)
            val oldDeferred = connectionDeferred
            connectionDeferred = CompletableDeferred()
            oldDeferred.completeExceptionally(
                PurchasesException(
                    PurchasesError(
                        ErrorCode.UNKNOWN_ERROR,
                        "Billing service disconnected"
                    )
                )
            )
        }

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            Timber.d("Billing setup finished with result: $billingResult")
            isConnecting.set(false)

            if (billingResult.isSuccess) {
                connectionDeferred.complete(Unit)

                scope.launch(dispatchers.io) {
                    getEntitlements()
                }
            } else {
                val e = PurchasesException(billingResult)
                Timber.e(e, "Billing setup failed")
                val current = connectionDeferred
                connectionDeferred = CompletableDeferred()
                current.completeExceptionally(e)
            }
        }
    }

    private data class PendingPurchaseState(
        val productId: String,
        val product: Product,
        val deferred: CompletableDeferred<Result<PurchaseTransaction>>,
    )

    /** Atomically holds the active purchase flow state so reads/writes cannot interleave. */
    private val pendingPurchaseState = AtomicReference<PendingPurchaseState?>(null)

    override val entitlements: Flow<Entitlements> = storage.entitlements

    private val billingClient = BillingClient.newBuilder(applicationContext)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enablePrepaidPlans()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .setListener(::onPurchasesUpdated)
        .build()

    internal fun initialize() {
        isConnecting.set(true)
        billingClient.startConnection(billingClientStateListener)

        ProcessLifecycleOwner.get()
            .lifecycle
            .currentStateFlow
            .filter { it == Lifecycle.State.STARTED }
            .debounce(FOREGROUND_DEBOUNCE_MS)
            .onEach { getEntitlements() }
            .launchIn(scope)
    }

    /**
     * Suspends until the BillingClient is connected. If the client is currently disconnected,
     * starts a new connection attempt before waiting. Retries once on setup failure so that a
     * transient error does not permanently block callers.
     */
    private suspend fun awaitConnection() {
        if (billingClient.isReady) return

        repeat(2) {
            val deferred = connectionDeferred

            if (billingClient.connectionState == BillingClient.ConnectionState.DISCONNECTED) {
                if (isConnecting.compareAndSet(false, true)) {
                    billingClient.startConnection(billingClientStateListener)
                }
            }

            try {
                deferred.await()
                return
            } catch (_: PurchasesException) {
                // Setup failed — retry once with the new deferred created by the listener.
            }
        }

        // Final attempt — propagate any exception.
        connectionDeferred.await()
    }

    override suspend fun getStorefront(): Result<Storefront> {
        awaitConnection()

        return suspendCancellableCoroutine { continuation ->
            val params = GetBillingConfigParams.newBuilder().build()
            billingClient.getBillingConfigAsync(params) { billingResult, billingConfig ->
                when (billingResult.responseCode) {
                    BillingResponseCode.OK -> {
                        val value = Storefront(billingConfig?.countryCode)
                        Timber.d("Got storefront: $value")
                        continuation.resume(Result.success(value))
                    }

                    else -> {
                        val e = PurchasesException(billingResult)
                        Timber.e(e, "Error getting storefront")
                        continuation.resume(Result.failure(e))
                    }
                }
            }
        }
    }

    override suspend fun getProducts(productIds: List<String>): Result<List<Product>> {
        awaitConnection()

        return try {
            val products = coroutineScope {
                val inAppDeferred = async { queryProducts(productIds, ProductType.INAPP) }
                val subsDeferred = async { queryProducts(productIds, ProductType.SUBS) }

                val inAppResult = inAppDeferred.await()
                val subsResult = subsDeferred.await()

                if (inAppResult.isFailure && subsResult.isFailure) {
                    throw inAppResult.exceptionOrNull() ?: subsResult.exceptionOrNull()!!
                }

                val allProducts = inAppResult.getOrDefault(emptyList()) +
                        subsResult.getOrDefault(emptyList())

                val productsMap = allProducts.associateBy { it.id }
                productIds.mapNotNull { productsMap[it] }
            }
            Result.success(products)
        } catch (e: Exception) {
            Timber.e(e, "Error querying products")
            Result.failure(e)
        }
    }

    private suspend fun queryProducts(
        productIds: List<String>,
        @ProductType productType: String
    ): Result<List<Product>> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(productType)
                        .build()
                }
            )
            .build()

        val result = withContext(dispatchers.io) {
            billingClient.queryProductDetails(params)
        }

        return if (result.billingResult.isSuccess) {
            val products = result.productDetailsList.orEmpty()
                .mapNotNull { it.toProduct() }
            Result.success(products)
        } else {
            Result.failure(PurchasesException(result.billingResult))
        }
    }

    override suspend fun purchase(
        activity: Activity,
        productId: String
    ): Result<PurchaseTransaction> {
        awaitConnection()

        if (pendingPurchaseState.get() != null) {
            return Result.failure(IllegalStateException("Purchase already in progress"))
        }

        val productDetails = getProductDetails(productId)
            ?: return Result.failure(IllegalStateException("Product not found: $productId"))

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply {
                productDetails.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.offerToken
                    ?.let(::setOfferToken)
            }
            .build()

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .apply {
                val accountIdentifiers = accountIdentifiersProvider()
                accountIdentifiers?.obfuscatedAccountId?.let(::setObfuscatedAccountId)
                accountIdentifiers?.obfuscatedProfileId?.let(::setObfuscatedProfileId)
            }
            .build()

        val product = productDetails.toProduct()
            ?: return Result.failure(IllegalStateException("Failed to map product details"))

        val deferred = CompletableDeferred<Result<PurchaseTransaction>>()
        val state = PendingPurchaseState(productId, product, deferred)

        pendingPurchaseState.set(state)

        val billingResult = withContext(dispatchers.main) {
            billingClient.launchBillingFlow(activity, params)
        }

        if (billingResult.isFailure) {
            pendingPurchaseState.compareAndSet(state, null)
            val e = PurchasesException(billingResult)
            Timber.e(e, "Error launching billing flow")
            return Result.failure(e)
        }

        return try {
            deferred.await()
        } finally {
            pendingPurchaseState.compareAndSet(state, null)
        }
    }

    override suspend fun restore(): Result<Entitlements> {
        awaitConnection()
        return getEntitlements()
    }

    // Called by the BillingClient on purchase updates (new purchase, subscription renewal, etc.).
    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val state = pendingPurchaseState.getAndSet(null)

        if (billingResult.isSuccess && purchases != null) {
            scope.launch {
                launch {
                    delay(PURCHASE_UPDATE_DELAY_MS)
                    getEntitlements()
                }

                launch {
                    processPurchases(purchases)
                }
            }

            if (state != null) {
                val purchase = purchases.firstOrNull { state.productId in it.products }

                if (purchase != null) {
                    val purchaseTransaction = PurchaseTransaction(
                        productId = state.productId,
                        orderId = purchase.orderId,
                        purchaseToken = purchase.purchaseToken,
                        product = state.product
                    )

                    Timber.d("Purchase updated: $purchaseTransaction")

                    state.deferred.complete(Result.success(purchaseTransaction))
                } else {
                    val e = PurchasesException(
                        PurchasesError(
                            code = ErrorCode.UNKNOWN_ERROR,
                            message = "Purchase not found for productId: ${state.productId}"
                        )
                    )

                    Timber.e(e, "Error updating purchases")

                    state.deferred.complete(Result.failure(e))
                }
            }
        } else if (state != null) {
            val e = PurchasesException(billingResult)
            Timber.e(e, "Error updating purchases")
            state.deferred.complete(Result.failure(e))
        }
    }

    private suspend fun getProductDetails(productId: String): ProductDetails? {
        for (productType in listOf(ProductType.INAPP, ProductType.SUBS)) {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    )
                )
                .build()

            val result = withContext(dispatchers.io) {
                billingClient.queryProductDetails(params)
            }

            return result.productDetailsList?.firstOrNull() ?: continue
        }

        return null
    }

    override suspend fun getEntitlements(): Result<Entitlements> {
        awaitConnection()

        val (inAppPurchases, subscriptionPurchases) = coroutineScope {
            val inApp = async { getPurchases(ProductType.INAPP) }
            val subs = async { getPurchases(ProductType.SUBS) }
            inApp.await() to subs.await()
        }

        if (inAppPurchases.isFailure && subscriptionPurchases.isFailure) {
            return Result.failure(
                inAppPurchases.exceptionOrNull()
                    ?: subscriptionPurchases.exceptionOrNull()!!
            )
        }

        val oneTimeProducts = inAppPurchases
            .getOrDefault(emptyList())
            .purchasedProductIds()

        val activeSubscriptionIds = subscriptionPurchases
            .getOrDefault(emptyList())
            .purchasedProductIds()

        val entitlements = Entitlements(
            purchasedProductIds = oneTimeProducts + activeSubscriptionIds,
            activeSubscriptionIds = activeSubscriptionIds,
        )

        try {
            storage.save(entitlements)
        } catch (e: Exception) {
            Timber.e(e, "Error saving entitlements")
        }

        return Result.success(entitlements)
    }

    private suspend fun getPurchases(@ProductType productType: String): Result<List<Purchase>> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()

        val result = withContext(dispatchers.io) {
            billingClient.queryPurchasesAsync(params)
        }

        return if (result.billingResult.isSuccess) {
            val purchases = result.purchasesList

            processPurchases(purchases)

            Result.success(purchases)
        } else {
            Result.failure(PurchasesException(result.billingResult))
        }
    }

    private fun List<Purchase>.purchasedProductIds(): Set<String> {
        return filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMapTo(mutableSetOf()) { it.products }
    }

    private suspend fun processPurchases(purchases: List<Purchase>) {
        coroutineScope {
            for (purchase in purchases) {
                launch {
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    /**
     * Acknowledges the purchase if not yet acknowledged.
     * Google Play will refund unacknowledged purchases after 3 days.
     */
    private suspend fun acknowledgePurchase(purchase: Purchase): Result<Unit> {
        if (purchase.isAcknowledged) {
            return Result.success(Unit)
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        return withContext(dispatchers.io) {
            val billingResult = billingClient.acknowledgePurchase(params)
            if (billingResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(PurchasesException(billingResult))
            }
        }
    }
}