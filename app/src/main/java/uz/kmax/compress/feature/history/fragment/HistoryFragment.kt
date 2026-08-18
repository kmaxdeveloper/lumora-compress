package uz.kmax.compress.feature.history.fragment

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import com.google.android.gms.ads.AdRequest
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.core.monetization.AdsManager
import uz.kmax.compress.databinding.FragmentHistoryBinding
import uz.kmax.compress.feature.history.adapter.HistoryPagingAdapter
import uz.kmax.compress.feature.history.event.HistoryEvent
import uz.kmax.compress.feature.history.state.HistoryUiState
import uz.kmax.compress.feature.history.viewmodel.HistoryViewModel
import javax.inject.Inject

import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import uz.kmax.compress.feature.history.model.HistoryUiModel

@AndroidEntryPoint
class HistoryFragment : BaseFragmentNV<FragmentHistoryBinding>(FragmentHistoryBinding::inflate) {

    @Inject
    lateinit var adsManager: AdsManager

    private val viewModel: HistoryViewModel by viewModels()
    private val adapter by lazy {
        HistoryPagingAdapter(
            onFavoriteClick = { viewModel.onFavoriteToggled(it.id, it.favorite) },
            onShareClick = { shareImage(it.compressedUri) },
            onItemClick = { 
                if (viewModel.uiState.value is HistoryUiState.Content && (viewModel.uiState.value as HistoryUiState.Content).selectionMode) {
                    viewModel.toggleSelection(it.id)
                } else {
                    // Navigate to Compare Screen
                }
            },
            onItemLongClick = { viewModel.toggleSelection(it.id) }
        )
    }

    override fun onViewCreated() {
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        loadBannerAd()
    }

    private fun loadBannerAd() {
        val adView = adsManager.createBannerView(requireActivity())
        binding.adContainer.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            if (viewModel.uiState.value is HistoryUiState.Content && (viewModel.uiState.value as HistoryUiState.Content).selectionMode) {
                viewModel.clearSelection()
            } else {
                navController.popBackStack()
            }
        }
        binding.toolbar.inflateMenu(R.menu.menu_history)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_sort -> {
                    showSortMenu()
                    true
                }
                R.id.action_delete_all -> {
                    viewModel.deleteSelected()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvHistory.adapter = adapter
        
        adapter.addLoadStateListener { loadState ->
            val isEmpty = loadState.source.refresh is LoadState.NotLoading && adapter.itemCount == 0
            binding.layoutEmpty.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.progressBar.visibility = if (loadState.source.refresh is LoadState.Loading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.fabDelete.setOnClickListener {
            viewModel.deleteSelected()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.historyPagingData.collectLatest { 
                        adapter.submitData(it)
                    }
                }
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

    private fun renderState(state: HistoryUiState) {
        if (state is HistoryUiState.Content) {
            binding.fabDelete.visibility = if (state.selectionMode) View.VISIBLE else View.GONE
            binding.toolbar.title = if (state.selectionMode) "${state.selectedCount} selected" else "History"
        }
    }

    private fun handleEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.ShowSnackbar -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    private fun shareImage(uri: android.net.Uri) {
        val shareUri = if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } else {
            uri
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, shareUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Compressed Image"))
    }

    private fun showSortMenu() {
        val anchor = binding.toolbar.findViewById<View>(R.id.action_sort)
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_sort, popup.menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sort_newest -> viewModel.onSort("NEWEST")
                R.id.sort_oldest -> viewModel.onSort("OLDEST")
                R.id.sort_savings -> viewModel.onSort("SAVINGS")
            }
            true
        }
        popup.show()
    }
}
