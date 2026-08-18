package uz.kmax.compress.feature.premium.state

import com.android.billingclient.api.ProductDetails
import uz.kmax.compress.core.premium.PremiumState

data class PremiumUiState(
    val products: List<ProductDetails> = emptyList(),
    val selectedProduct: ProductDetails? = null,
    val premiumState: PremiumState = PremiumState.Checking,
    val isLoading: Boolean = true,
    val error: String? = null
)
