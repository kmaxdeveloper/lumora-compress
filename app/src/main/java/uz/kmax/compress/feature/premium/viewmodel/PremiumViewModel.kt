package uz.kmax.compress.feature.premium.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.core.monetization.BillingManager
import uz.kmax.compress.domain.repository.BillingRepository
import uz.kmax.compress.feature.premium.event.PremiumEvent
import uz.kmax.compress.feature.premium.state.PremiumUiState
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PremiumEvent>()
    val event = _event.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            billingManager.productDetails,
            billingRepository.premiumState
        ) { products, state ->
            val currentSelected = _uiState.value.selectedProduct
            val defaultSelected = products.find { it.productId == "premium_yearly" } ?: products.firstOrNull()
            
            _uiState.update { it.copy(
                products = products,
                selectedProduct = currentSelected ?: defaultSelected,
                premiumState = state,
                isLoading = false
            ) }
        }.launchIn(viewModelScope)
    }

    fun onProductClicked(product: ProductDetails) {
        _uiState.update { it.copy(selectedProduct = product) }
    }

    fun onContinueClicked(activity: Activity) {
        val product = _uiState.value.selectedProduct
        if (product != null) {
            val result = billingManager.launchBillingFlow(activity, product)
            if (result.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                viewModelScope.launch {
                    _event.emit(PremiumEvent.ShowSnackbar("Billing Error: ${result.debugMessage.ifEmpty { "Code ${result.responseCode}" }}"))
                }
            }
        } else {
            viewModelScope.launch {
                _event.emit(PremiumEvent.ShowSnackbar("Please select a product first"))
            }
        }
    }

    fun onRestoreClicked() {
        viewModelScope.launch {
            billingRepository.restorePurchases()
            _event.emit(PremiumEvent.ShowSnackbar("Restoring purchases..."))
        }
    }

    fun onBackClicked() = viewModelScope.launch { _event.emit(PremiumEvent.NavigateBack) }
}
