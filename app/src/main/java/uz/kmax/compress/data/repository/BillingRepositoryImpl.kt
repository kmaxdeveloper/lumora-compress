package uz.kmax.compress.data.repository

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import uz.kmax.compress.core.monetization.BillingManager
import uz.kmax.compress.core.monetization.PurchaseVerifier
import uz.kmax.compress.core.premium.PremiumState
import uz.kmax.compress.domain.repository.BillingRepository
import uz.kmax.compress.core.preferences.PreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
    private val purchaseVerifier: PurchaseVerifier,
    private val preferencesManager: PreferencesManager
) : BillingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _premiumState = MutableStateFlow<PremiumState>(PremiumState.Checking)
    override val premiumState: StateFlow<PremiumState> = _premiumState.asStateFlow()

    override val productDetails: StateFlow<List<ProductDetails>> = billingManager.productDetails
    override val purchaseEvents: SharedFlow<List<Purchase>> = billingManager.purchases

    init {
        observeCachedState()
        observePurchases()
    }

    private fun observeCachedState() {
        preferencesManager.preferencesFlow
            .onEach { prefs ->
                if (_premiumState.value is PremiumState.Checking) {
                    _premiumState.value = if (prefs.isPremium) PremiumState.Premium else PremiumState.Free
                }
            }
            .launchIn(repositoryScope)
    }

    private fun observePurchases() {
        billingManager.purchases
            .onEach { purchases ->
                val isPremium = purchases.any { purchaseVerifier.isPurchaseValid(it) }
                _premiumState.value = if (isPremium) PremiumState.Premium else PremiumState.Free
                preferencesManager.setPremiumState(isPremium)
            }
            .launchIn(repositoryScope)
    }

    override suspend fun startBillingConnection() {
        billingManager.startConnection()
    }

    override suspend fun fetchAvailableProducts() {
        billingManager.queryAllProducts()
    }

    override suspend fun restorePurchases() {
        billingManager.queryAllPurchases()
    }
}
