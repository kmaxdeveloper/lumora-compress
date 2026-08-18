package uz.kmax.compress.feature.home.fragment

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentHomeBinding
import uz.kmax.compress.feature.home.adapter.RecentFilesAdapter
import uz.kmax.compress.feature.home.event.HomeEvent
import uz.kmax.compress.feature.home.state.HomeUiState
import uz.kmax.compress.feature.home.viewmodel.HomeViewModel

@AndroidEntryPoint
class HomeFragment : BaseFragmentNV<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private val adapter by lazy {
        RecentFilesAdapter { file ->
            // Handle file click
        }
    }

    override fun onViewCreated() {
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        setupEmptyState()
        viewModel.loadNativeAd(requireActivity())
    }

    private fun setupEmptyState() {
        binding.emptyState.setState(
            icon = R.drawable.ic_gallery,
            title = getString(R.string.no_recent_files),
            description = getString(R.string.no_recent_files_desc)
        )
    }

    private fun setupRecyclerView() {
        binding.rvRecentFiles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
        binding.headerRecent.setTitle(getString(R.string.recent_files))
    }

    private fun setupListeners() {
        binding.apply {
            cardCompress.setOnClickListener { viewModel.onCompressClick() }
            cardBatch.setOnClickListener { viewModel.onBatchCompressClick() }
            cardPremium.setOnClickListener { viewModel.onPremiumClick() }
            cardStorageGuardian.setOnClickListener { 
                navController.navigate(R.id.action_homeFragment_to_storageGuardianFragment)
            }
            ivAvatar.setOnClickListener { viewModel.onSettingsClick() }
            btnShareApp.setOnClickListener { shareApp() }
        }
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, requireContext().packageName))
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
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
                launch {
                    viewModel.nativeAd.collect { ad ->
                        if (ad != null) {
                            binding.nativeAd.visibility = View.VISIBLE
                            binding.nativeAd.setNativeAd(ad)
                        } else {
                            binding.nativeAd.visibility = View.GONE
                        }
                    }
                }
                launch {
                    viewModel.yandexNativeAd.collect { ad ->
                        if (ad != null) {
                            binding.yandexNativeAd.visibility = View.VISIBLE
                            binding.yandexNativeAd.setNativeAd(ad)
                        } else {
                            binding.yandexNativeAd.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun renderState(state: HomeUiState) {
        when (state) {
            is HomeUiState.Loading -> {}
            is HomeUiState.Content -> {
                binding.apply {
                    tvGreeting.text = state.greeting
                    animateNumber(tvStatCount, state.compressedCount)
                    tvStatSaved.text = state.storageSaved
                    tvStatReduction.text = state.averageReduction
                    
                    tvBatchLimit.text = if (state.isPremium) "" else "${state.remainingFreeBatch} left"
                    tvBatchLimit.visibility = if (state.isPremium) View.GONE else View.VISIBLE
                    ivBatchLock.visibility = if (state.isPremium) View.GONE else View.VISIBLE
                    
                    cardPremium.visibility = if (state.isPremium) View.GONE else View.VISIBLE
                }
                adapter.submitList(state.recentFiles)
                binding.rvRecentFiles.visibility = if (state.recentFiles.isEmpty()) View.GONE else View.VISIBLE
                binding.emptyState.visibility = if (state.recentFiles.isEmpty()) View.VISIBLE else View.GONE
            }
            is HomeUiState.Empty -> {
                binding.rvRecentFiles.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }

    private fun animateNumber(view: TextView, targetValue: Int) {
        val animator = ValueAnimator.ofInt(0, targetValue)
        animator.duration = 1500
        animator.addUpdateListener { animation ->
            view.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    private fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OpenGallery -> navController.navigate(R.id.action_homeFragment_to_galleryFragment)
            is HomeEvent.OpenBatch -> navController.navigate(R.id.action_homeFragment_to_galleryFragment)
            is HomeEvent.OpenHistory -> navController.navigate(R.id.action_homeFragment_to_historyFragment)
            is HomeEvent.OpenSettings -> navController.navigate(R.id.action_homeFragment_to_settingsFragment)
            is HomeEvent.OpenPremium -> navController.navigate(R.id.action_global_premiumFragment)
            is HomeEvent.OpenAbout -> navController.navigate(R.id.action_homeFragment_to_aboutFragment)
        }
    }
}
