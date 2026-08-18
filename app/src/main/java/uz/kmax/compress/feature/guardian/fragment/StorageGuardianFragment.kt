package uz.kmax.compress.feature.guardian.fragment

import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentStorageGuardianBinding
import uz.kmax.compress.databinding.ViewStatItemBinding
import uz.kmax.compress.domain.model.CategoryType
import uz.kmax.compress.domain.model.HealthLabel
import uz.kmax.compress.domain.model.StorageGuardianResult
import uz.kmax.compress.feature.guardian.event.StorageGuardianEvent
import uz.kmax.compress.feature.guardian.mapper.StorageGuardianMapper
import uz.kmax.compress.feature.guardian.state.StorageGuardianUiState
import uz.kmax.compress.feature.guardian.viewmodel.StorageGuardianViewModel

@AndroidEntryPoint
class StorageGuardianFragment : BaseFragmentNV<FragmentStorageGuardianBinding>(FragmentStorageGuardianBinding::inflate) {

    private val viewModel: StorageGuardianViewModel by viewModels()

    override fun onViewCreated() {
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnCompressCamera.setOnClickListener { viewModel.onActionClicked(CategoryType.CAMERA) }
            btnCompressScreenshots.setOnClickListener { viewModel.onActionClicked(CategoryType.SCREENSHOTS) }
            btnOpenBatch.setOnClickListener { viewModel.onNavigateToBatch() }
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

    private fun renderState(state: StorageGuardianUiState) {
        binding.loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        
        state.result?.let { result ->
            renderResult(result)
        }
    }

    private fun renderResult(result: StorageGuardianResult) {
        binding.apply {
            tvHealthScore.text = result.healthScore.toString()
            progressHealth.progress = result.healthScore
            tvHealthLabel.text = getLabelString(result.healthLabel)
            tvPotentialSaving.text = StorageGuardianMapper.formatSize(result.potentialSavingBytes)

            // Stats
            val totalCountBinding = ViewStatItemBinding.bind(layoutTotalCount.root)
            totalCountBinding.tvLabel.text = getString(R.string.total_images)
            totalCountBinding.tvValue.text = result.totalImageCount.toString()

            val recentlyAddedBinding = ViewStatItemBinding.bind(layoutRecentlyAdded.root)
            recentlyAddedBinding.tvLabel.text = getString(R.string.recently_added)
            recentlyAddedBinding.tvValue.text = result.recentlyAddedCount.toString()

            if (result.isCapped) {
                val textView = TextView(requireContext()).apply {
                    text = "Analysis limited to top 20,000 images per category for performance."
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall)
                    setTextColor(com.google.android.material.color.MaterialColors.getColor(requireContext(), R.attr.lumoraError, android.graphics.Color.RED))
                    setPadding(0, 16, 0, 0)
                }
                layoutRecommendations.addView(textView)
            }

            // Recommendations
            layoutRecommendations.removeAllViews()
            result.recommendations.forEach { recommendation ->
                val textView = TextView(requireContext()).apply {
                    text = getString(R.string.recommendation_item, recommendation)
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                    setPadding(0, 8, 0, 8)
                }
                layoutRecommendations.addView(textView)
            }
        }
    }

    private fun getLabelString(label: HealthLabel): String {
        return when (label) {
            HealthLabel.EXCELLENT -> getString(R.string.excellent)
            HealthLabel.GOOD -> getString(R.string.good)
            HealthLabel.AVERAGE -> getString(R.string.average)
            HealthLabel.NEEDS_OPTIMIZATION -> getString(R.string.needs_optimization)
        }
    }

    private fun handleEvent(event: StorageGuardianEvent) {
        when (event) {
            is StorageGuardianEvent.ShowError -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is StorageGuardianEvent.NavigateToBatch -> {
                val action = StorageGuardianFragmentDirections.actionStorageGuardianFragmentToBatchFragment(
                    batchId = null,
                    categoryType = event.category.name
                )
                navController.navigate(action)
            }
            is StorageGuardianEvent.NavigateToGallery -> {
                navController.navigate(R.id.action_storageGuardianFragment_to_galleryFragment)
            }
            else -> {}
        }
    }
}
