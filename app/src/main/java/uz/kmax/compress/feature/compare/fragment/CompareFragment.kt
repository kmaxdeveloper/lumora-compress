package uz.kmax.compress.feature.compare.fragment

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import androidx.core.content.FileProvider
import uz.kmax.compress.databinding.FragmentCompareBinding
import uz.kmax.compress.feature.compare.event.CompareEvent
import uz.kmax.compress.feature.compare.state.CompareUiState
import uz.kmax.compress.feature.compare.viewmodel.CompareViewModel
import java.io.File
import java.util.*

@AndroidEntryPoint
class CompareFragment : BaseFragmentNV<FragmentCompareBinding>(FragmentCompareBinding::inflate) {

    private val viewModel: CompareViewModel by viewModels()
    private var isContentRendered = false

    override fun onViewCreated() {
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
        binding.toolbar.inflateMenu(R.menu.menu_compare)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_rename -> {
                    showRenameDialog()
                    true
                }
                R.id.action_home -> {
                    viewModel.onHomeClicked()
                    true
                }
                else -> false
            }
        }
        binding.btnToolbarShare.setOnClickListener {
            viewModel.onShareClicked()
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnSave.setOnClickListener { viewModel.onSaveClicked(requireActivity()) }
            btnShare.setOnClickListener { viewModel.onShareClicked() }
            btnDeleteOriginal.setOnClickListener { viewModel.onDeleteOriginalClicked() }
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

    private fun renderState(state: CompareUiState) {
        when (state) {
            is CompareUiState.Loading -> {
                // Initial loading if needed
            }
            is CompareUiState.Content -> {
                if (!isContentRendered) {
                    binding.apply {
                        animateText(tvYouSaved, state.result.savedPercentage)
                        tvOriginalSize.text = formatSize(state.result.originalSize)
                        tvCompressedSize.text = formatSize(state.result.compressedSize)
                        
                        tvPhotosCount.text = getString(R.string.photos_count, state.estimatedPhotos)
                        tvVideosCount.text = getString(R.string.videos_count, state.estimatedVideos)

                        loadComparisonBitmaps(state)
                    }
                    isContentRendered = true
                }
                
                // Update only saving state if it changes
                binding.btnSave.isEnabled = !state.isSaving
            }
            is CompareUiState.Error -> {
                // Show error message
            }
        }
    }

    private fun animateText(view: TextView, targetValue: Float) {
        val animator = ValueAnimator.ofFloat(0f, targetValue)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val state = viewModel.uiState.value
            if (state is CompareUiState.Content) {
                val savedBytes = state.result.savedBytes
                if (savedBytes > 0) {
                    view.text = getString(R.string.you_saved, formatSize(savedBytes), value)
                    view.setTextColor(MaterialColors.getColor(binding.root, R.attr.lumoraSecondary))
                } else if (savedBytes == 0L) {
                    view.text = getString(R.string.already_optimized)
                    view.setTextColor(MaterialColors.getColor(binding.root, R.attr.lumoraPrimary))
                } else {
                    view.text = getString(R.string.size_increased, formatSize(-savedBytes), -value)
                    view.setTextColor(MaterialColors.getColor(binding.root, R.attr.lumoraError))
                }
            }
        }
        animator.start()
    }

    private fun loadComparisonBitmaps(state: CompareUiState.Content) {
        val loader = ImageLoader(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            val originalReq = ImageRequest.Builder(requireContext())
                .data(state.result.originalUri)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            val compressedReq = ImageRequest.Builder(requireContext())
                .data(state.result.compressedUri)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            val originalResult = loader.execute(originalReq)
            val compressedResult = loader.execute(compressedReq)

            if (originalResult.drawable is BitmapDrawable && compressedResult.drawable is BitmapDrawable) {
                val originalBitmap = (originalResult.drawable as BitmapDrawable).bitmap
                val compressedBitmap = (compressedResult.drawable as BitmapDrawable).bitmap
                binding.comparisonView.setBitmaps(originalBitmap, compressedBitmap)
            }
        }
    }

    private fun handleEvent(event: CompareEvent) {
        when (event) {
            is CompareEvent.Share -> {
                shareImage(event.uri)
            }
            is CompareEvent.ShowSnackbar -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_LONG).show()
            }
            is CompareEvent.ShowDialog -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(event.title)
                    .setMessage(event.message)
                    .setPositiveButton("Confirm") { _, _ ->
                        // Handle confirmation logic
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            is CompareEvent.NavigateHome -> {
                navController.popBackStack(R.id.homeFragment, false)
            }
            else -> {}
        }
    }

    private fun showRenameDialog() {
        val input = EditText(requireContext()).apply {
            setPadding(48)
            hint = getString(R.string.enter_filename)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename_image)
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    // Logic to rename output
                    Snackbar.make(binding.root, "Renamed to $newName", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareImage(uri: android.net.Uri) {
        val state = viewModel.uiState.value as? CompareUiState.Content
        val mimeType = when (state?.result?.format?.uppercase()) {
            "PNG" -> "image/png"
            "WEBP" -> "image/webp"
            else -> "image/jpeg"
        }

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
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Compressed Image"))
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024
        return if (kb > 1024) {
            String.format(Locale.getDefault(), "%.1f MB", kb / 1024f)
        } else {
            "$kb KB"
        }
    }
}
