package uz.kmax.compress.feature.batch.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentBatchBinding
import uz.kmax.compress.feature.batch.adapter.BatchAdapter
import uz.kmax.compress.feature.batch.state.BatchUiState
import uz.kmax.compress.feature.batch.viewmodel.BatchViewModel

@AndroidEntryPoint
class BatchFragment : BaseFragmentNV<FragmentBatchBinding>(FragmentBatchBinding::inflate) {

    private val viewModel: BatchViewModel by viewModels()
    private val adapter by lazy { BatchAdapter() }
    private var isBatchFinished = false

    override fun onViewCreated() {
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            handleBackNavigation()
        }
    }

    private fun handleBackNavigation() {
        if (isBatchFinished) {
            navController.popBackStack(R.id.homeFragment, false)
        } else {
            navController.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        binding.rvQueue.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnStartPause.setOnClickListener {
            if (viewModel.uiState.value.isRunning) {
                viewModel.pauseBatch()
            } else {
                viewModel.startBatch()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: BatchUiState) {
        binding.apply {
            adapter.submitList(state.items)
            tvOverallProgress.text = "Overall: ${state.overallProgress}%"
            progressOverall.progress = state.overallProgress
            tvRemainingTime.text = if (state.isRunning) "Estimated remaining: ${state.remainingTime}" else state.remainingTime
            btnStartPause.text = if (state.isRunning) "Pause Batch" else "Start Batch"
            
            if (!state.isPremium && state.remainingFreeCount <= 0) {
                btnStartPause.isEnabled = false
                btnStartPause.text = "Free limit reached"
            } else {
                btnStartPause.isEnabled = true
            }

            // Current Item
            layoutCurrentItem.visibility = if (state.isRunning && state.currentItem != null) View.VISIBLE else View.GONE
            state.currentItem?.let { item ->
                ivCurrentPreview.load(item.uri)
                tvCurrentName.text = "Processing: ${item.name}"
            }

            val isFinished = state.items.isNotEmpty() && state.items.all { 
                it.status == uz.kmax.compress.feature.batch.model.BatchItemUiModel.Status.COMPLETED || 
                it.status == uz.kmax.compress.feature.batch.model.BatchItemUiModel.Status.FAILED 
            }
            this@BatchFragment.isBatchFinished = isFinished
            tvBatchSummary.visibility = if (isFinished && !state.isRunning) View.VISIBLE else View.GONE
            if (isFinished) {
                tvRemainingTime.text = "All tasks processed"
                btnStartPause.visibility = View.GONE
            }
        }
    }
}
