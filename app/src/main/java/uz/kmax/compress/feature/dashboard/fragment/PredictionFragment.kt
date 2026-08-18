package uz.kmax.compress.feature.dashboard.fragment

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentPredictionBinding
import uz.kmax.compress.feature.dashboard.adapter.PredictionCardAdapter
import uz.kmax.compress.feature.dashboard.state.PredictionState
import uz.kmax.compress.feature.dashboard.viewmodel.PredictionViewModel

@AndroidEntryPoint
class PredictionFragment : BaseFragmentNV<FragmentPredictionBinding>(FragmentPredictionBinding::inflate) {
    private val vm: PredictionViewModel by viewModels()
    private val adapter = PredictionCardAdapter()

    override fun onViewCreated() {
        setupUI()
        observeState()
    }

    private fun setupUI() {
        val spanCount = if (resources.configuration.screenWidthDp >= 600) 2 else 1
        binding.cards.apply {
            adapter = this@PredictionFragment.adapter
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            setHasFixedSize(true)
        }
        binding.toolbar.setNavigationOnClickListener { navController.popBackStack() }
        binding.continueButton.setOnClickListener {
            navController.navigate(PredictionFragmentDirections.actionPredictionFragmentToCompressFragment(vm.image()))
        }
        binding.viewError.root.findViewById<View>(R.id.btnRetry).setOnClickListener {
            vm.retry()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collect { state ->
                when (state) {
                    is PredictionState.Loading -> {
                        binding.viewLoading.root.visibility = View.VISIBLE
                        binding.viewError.root.visibility = View.GONE
                        binding.content.visibility = View.GONE
                    }
                    is PredictionState.Error -> {
                        binding.viewLoading.root.visibility = View.GONE
                        binding.viewError.root.visibility = View.VISIBLE
                        binding.content.visibility = View.GONE
                        binding.viewError.root.findViewById<android.widget.TextView>(R.id.tvTitle).text = getString(R.string.error)
                        binding.viewError.root.findViewById<android.widget.TextView>(R.id.tvDescription).text = state.message
                    }
                    is PredictionState.Content -> {
                        binding.viewLoading.root.visibility = View.GONE
                        binding.viewError.root.visibility = View.GONE
                        
                        if (binding.content.visibility != View.VISIBLE) {
                            binding.content.visibility = View.VISIBLE
                            binding.content.alpha = 0f
                            binding.content.translationY = 32f
                            binding.content.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(300)
                                .start()
                        }

                        val model = state.model
                        binding.apply {
                            summaryTitle.text = getString(R.string.dashboard_summary, model.saving, model.quality)
                            tvEstimatedSize.text = model.estimatedSize
                            tvEstimatedSize.contentDescription = "${getString(R.string.estimated_size)}: ${model.estimatedSize}"
                            tvSavedSpace.text = model.saving
                            tvSavedSpace.contentDescription = "${getString(R.string.saved_space)}: ${model.saving}"
                            tvQuality.text = getString(R.string.percent_format, model.quality)
                            tvQuality.contentDescription = "${getString(R.string.quality)}: ${model.quality}%"
                            tvTime.text = model.time
                            tvTime.contentDescription = "${getString(R.string.estimated_time)}: ${model.time}"
                            tvFormat.text = model.format
                            tvFormat.contentDescription = "${getString(R.string.format)}: ${model.format}"
                            analysis.text = "${model.imageType}\n\n${model.reason}"
                        }

                        adapter.submit(listOf(
                            getString(R.string.original_size) to model.originalSize,
                            getString(R.string.output_resolution) to model.resolution
                        ))
                    }
                }
            }
        }
    }
}
