package uz.kmax.compress.core.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val purchaseVerifier: PurchaseVerifier
) : PurchasesUpdatedListener {

    private val managerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _billingConnected = MutableStateFlow(false)
    val billingConnected: StateFlow<Boolean> = _billingConnected.asStateFlow()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _purchases = MutableSharedFlow<List<Purchase>>(replay = 1)
    val purchases: SharedFlow<List<Purchase>> = _purchases.asSharedFlow()

    private var reconnectRetryCount = 0
    private val maxRetryCount = 5

    init {
        startConnection()
    }

    fun startConnection() {
        if (billingClient.isReady) return

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _billingConnected.value = true
                    reconnectRetryCount = 0
                    queryAllProducts()
                    queryAllPurchases()
                } else {
                    _billingConnected.value = false
                    retryConnection()
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingConnected.value = false
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        if (reconnectRetryCount < maxRetryCount) {
            reconnectRetryCount++
            managerScope.launch {
                delay(2000L * reconnectRetryCount)
                startConnection()
            }
        }
    }

    fun queryAllProducts() {
        managerScope.launch {
            val subs = queryByType(ProductType.SUBS, listOf("premium_monthly", "premium_yearly"))
            val inApp = queryByType(ProductType.INAPP, listOf("premium_lifetime_v2"))
            _productDetails.value = subs + inApp
        }
    }

    private suspend fun queryByType(type: String, ids: List<String>): List<ProductDetails> {
        val productList = ids.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(type)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val result = billingClient.queryProductDetails(params)
        
        return if (result.billingResult.responseCode == BillingResponseCode.OK) {
            result.productDetailsList ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun queryAllPurchases() {
        managerScope.launch {
            val subsResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS).build()
            )
            val inAppResult = billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
            )

            val allPurchases = mutableListOf<Purchase>()
            if (subsResult.billingResult.responseCode == BillingResponseCode.OK) {
                allPurchases.addAll(subsResult.purchasesList)
            }
            if (inAppResult.billingResult.responseCode == BillingResponseCode.OK) {
                allPurchases.addAll(inAppResult.purchasesList)
            }
            
            processPurchases(allPurchases)
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): BillingResult {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .apply {
                    productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let {
                        setOfferToken(it)
                    }
                }
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        return billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>) {
        managerScope.launch {
            val verifiedPurchases = purchasesList.filter { purchaseVerifier.isPurchaseValid(it) }
            for (purchase in verifiedPurchases) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { _ ->
                        // Purchase acknowledged
                    }
                }
            }
            _purchases.emit(verifiedPurchases)
        }
    }

    fun release() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
        managerScope.cancel()
    }
}
