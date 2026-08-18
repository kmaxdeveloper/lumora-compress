package uz.kmax.compress.feature.premium.fragment

import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentPremiumBinding
import uz.kmax.compress.databinding.ItemPremiumProductBinding
import uz.kmax.compress.feature.premium.event.PremiumEvent
import uz.kmax.compress.feature.premium.state.PremiumUiState
import uz.kmax.compress.feature.premium.viewmodel.PremiumViewModel
import uz.kmax.compress.core.premium.PremiumState

@AndroidEntryPoint
class PremiumFragment : BaseFragmentNV<FragmentPremiumBinding>(FragmentPremiumBinding::inflate) {

    private val viewModel: PremiumViewModel by viewModels()

    override fun onViewCreated() {
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            viewModel.onBackClicked()
        }
        binding.benefit1.tvBenefit.text = getString(R.string.benefit_ads)
        binding.benefit2.tvBenefit.text = getString(R.string.benefit_batch)
        binding.benefit3.tvBenefit.text = getString(R.string.benefit_avif)
        binding.benefit4.tvBenefit.text = getString(R.string.benefit_priority)
        binding.benefit5.tvBenefit.text = getString(R.string.unlimited_batch_desc)
    }

    private fun setupListeners() {
        binding.btnRestore.setOnClickListener {
            viewModel.onRestoreClicked()
        }
        binding.btnContinue.setOnClickListener {
            viewModel.onContinueClicked(requireActivity())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        renderState(state)
                    }
                }
                launch {
                    viewModel.event.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun renderState(state: PremiumUiState) {
        binding.apply {
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            val isPremium = state.premiumState is PremiumState.Premium
            if (isPremium) {
                layoutPurchase.visibility = View.GONE
                layoutAlreadyPremium.visibility = View.VISIBLE
                bottomActionArea.visibility = View.GONE
                return
            } else {
                layoutPurchase.visibility = View.VISIBLE
                layoutAlreadyPremium.visibility = View.GONE
                bottomActionArea.visibility = View.VISIBLE
            }
            
            layoutProducts.removeAllViews()
            state.products.forEach { product ->
                val itemBinding = ItemPremiumProductBinding.inflate(LayoutInflater.from(requireContext()), layoutProducts, true)
                itemBinding.tvTitle.text = product.name
                itemBinding.tvDescription.text = product.description
                
                val price = product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: product.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: ""
                itemBinding.tvPrice.text = price
                
                val isSelected = product.productId == state.selectedProduct?.productId
                itemBinding.radioButton.isChecked = isSelected
                
                val colorPrimary = androidx.core.content.ContextCompat.getColor(requireContext(), uz.kmax.compress.R.color.md_theme_primary)
                val colorOutline = androidx.core.content.ContextCompat.getColor(requireContext(), uz.kmax.compress.R.color.md_theme_outlineVariant)
                itemBinding.root.strokeColor = if (isSelected) colorPrimary else colorOutline
                itemBinding.root.strokeWidth = if (isSelected) 4 else 2

                if (product.productId == "premium_yearly") {
                    itemBinding.tvBadge.visibility = View.VISIBLE
                } else {
                    itemBinding.tvBadge.visibility = View.GONE
                }
                
                itemBinding.root.setOnClickListener {
                    viewModel.onProductClicked(product)
                }
                itemBinding.radioButton.setOnClickListener {
                    viewModel.onProductClicked(product)
                }
            }
        }
    }

    private fun handleEvent(event: PremiumEvent) {
        when (event) {
            is PremiumEvent.NavigateBack -> navController.popBackStack()
            is PremiumEvent.ShowSnackbar -> Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
        }
    }
}
