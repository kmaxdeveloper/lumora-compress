package uz.kmax.compress.domain.repository

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import uz.kmax.compress.core.premium.PremiumState

interface BillingRepository {
    val premiumState: StateFlow<PremiumState>
    val productDetails: StateFlow<List<ProductDetails>>
    val purchaseEvents: SharedFlow<List<Purchase>>
    
    suspend fun startBillingConnection()
    suspend fun fetchAvailableProducts()
    suspend fun restorePurchases()
}
