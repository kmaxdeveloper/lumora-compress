package uz.kmax.compress.core.monetization

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

data class BillingState(
    val isConnected: Boolean = false,
    val products: List<ProductDetails> = emptyList(),
    val purchases: List<Purchase> = emptyList(),
    val error: String? = null
)
