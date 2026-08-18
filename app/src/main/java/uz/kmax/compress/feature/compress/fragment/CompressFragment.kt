package uz.kmax.compress.feature.compress.fragment

import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.databinding.FragmentCompressBinding
import uz.kmax.compress.feature.compress.event.CompressEvent
import uz.kmax.compress.feature.compress.state.CompressUiState
import uz.kmax.compress.feature.compress.viewmodel.CompressViewModel
import java.util.Locale
import uz.kmax.compress.core.social.SocialPreset
import uz.kmax.compress.common.bottomsheet.PremiumPaywallBottomSheet
import uz.kmax.compress.feature.compress.state.CompressionMode
import uz.kmax.compress.feature.compress.adapter.SocialPresetAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors

@AndroidEntryPoint
class CompressFragment : BaseFragmentNV<FragmentCompressBinding>(FragmentCompressBinding::inflate) {

    private val viewModel: CompressViewModel by viewModels()
    private val socialAdapter by lazy { SocialPresetAdapter { viewModel.onSocialPresetSelected(it) } }

    private var lastEstimatedSize = 0L

    override fun onViewCreated() {
        setupToolbar()
        setupListeners()
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        binding.socialPresetsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = socialAdapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
    }

    private fun setupListeners() {
        binding.apply {
            toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    when (checkedId) {
                        R.id.btnSmart -> viewModel.onModeChanged(true)
                        R.id.btnManual -> viewModel.onModeChanged(false)
                        R.id.btnSocial -> viewModel.onSocialModeSelected()
                        R.id.btnTargetSize -> viewModel.onTargetSizeModeSelected()
                    }
                    animateContentSwitch()
                }
            }

            sliderQuality.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    viewModel.onQualityChanged(value.toInt())
                }
                updateQualityText(value.toInt())
            }

            chipLow.setOnClickListener { 
                viewModel.onQualityChanged(30)
                sliderQuality.value = 30f 
                updateQualityText(30)
            }
            chipMedium.setOnClickListener { 
                viewModel.onQualityChanged(60)
                sliderQuality.value = 60f 
                updateQualityText(60)
            }
            chipHigh.setOnClickListener { 
                viewModel.onQualityChanged(80)
                sliderQuality.value = 80f 
                updateQualityText(80)
            }
            chipMaximum.setOnClickListener { 
                viewModel.onQualityChanged(100)
                sliderQuality.value = 100f 
                updateQualityText(100)
            }

            chipAuto.setOnClickListener { viewModel.onFormatSelected(CompressionFormat.AUTO) }
            chipJpeg.setOnClickListener { viewModel.onFormatSelected(CompressionFormat.JPEG) }
            chipPng.setOnClickListener { viewModel.onFormatSelected(CompressionFormat.PNG) }
            chipWebp.setOnClickListener { viewModel.onFormatSelected(CompressionFormat.WEBP_LOSSY) }
            chipAvif.visibility = View.GONE
            
            target100.setOnClickListener { viewModel.onTargetSizeSelected(100 * 1024L) }
            target200.setOnClickListener { viewModel.onTargetSizeSelected(200 * 1024L) }
            target500.setOnClickListener { viewModel.onTargetSizeSelected(500 * 1024L) }
            target1mb.setOnClickListener { viewModel.onTargetSizeSelected(1024 * 1024L) }
            targetCustom.setOnClickListener { showTargetSizeDialog() }

            switchResize.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onResizeToggled(isChecked)
                layoutResizeOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            chipKeepMetadata.setOnClickListener { viewModel.onMetadataStrategySelected(uz.kmax.compress.core.compressor.metadata.MetadataOptions.Strategy.KEEP_ALL) }
            chipRemoveGps.setOnClickListener { viewModel.onMetadataStrategySelected(uz.kmax.compress.core.compressor.metadata.MetadataOptions.Strategy.REMOVE_GPS) }
            chipRemoveAll.setOnClickListener { viewModel.onMetadataStrategySelected(uz.kmax.compress.core.compressor.metadata.MetadataOptions.Strategy.REMOVE_ALL) }

            btnCompress.setOnClickListener {
                viewModel.onCompressClicked()
            }
        }
    }

    private fun animateContentSwitch() {
        binding.apply {
            val views = listOf(layoutManualSettings, cardSmartInfo, socialPresetsList, targetSizeCard)
            views.forEach { v ->
                if (v.visibility == View.VISIBLE) {
                    v.alpha = 0f
                    v.translationY = 20f
                    v.animate().alpha(1f).translationY(0f).setDuration(300).start()
                }
            }
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

    private fun updateQualityText(quality: Int) {
        binding.tvCurrentQuality.text = getString(R.string.percent_format, quality)
        binding.tvQualityWarning.text = when {
            quality < 40 -> "Significant quality loss, very small file size"
            quality < 75 -> "Good balance between size and quality"
            quality < 95 -> "Excellent quality, moderate size reduction"
            else -> "Maximum quality, minimal size reduction"
        }
    }

    private fun renderState(state: CompressUiState) {
        binding.apply {
            if (!sliderQuality.isFocused) {
                sliderQuality.value = state.selectedQuality.toFloat()
                updateQualityText(state.selectedQuality)
            }
            ivPreview.load(state.imageModel.uri) {
                crossfade(true)
            }

            tvImageInfo.text = getString(
                R.string.image_info_format,
                getString(R.string.resolution_placeholder, state.imageModel.width, state.imageModel.height),
                state.imageModel.size,
                state.imageModel.mimeType.substringAfter("/").uppercase(Locale.getDefault())
            )

            // Mode Switching Logic
            layoutManualSettings.visibility = if (state.mode == CompressionMode.MANUAL) View.VISIBLE else View.GONE
            cardSmartInfo.visibility = if (state.mode == CompressionMode.SMART) View.VISIBLE else View.GONE
            socialPresetsList.visibility = if (state.mode == CompressionMode.SOCIAL) View.VISIBLE else View.GONE
            targetSizeCard.visibility = if (state.mode == CompressionMode.TARGET_SIZE) View.VISIBLE else View.GONE
            
            if (state.mode == CompressionMode.SOCIAL) {
                socialAdapter.setPremium(state.isPremium)
                socialAdapter.setSelected(state.selectedSocialPreset)
            }

            if (state.mode == CompressionMode.TARGET_SIZE) {
                layoutTargetStatus.visibility = if (state.isCompressing || state.targetIteration > 0) View.VISIBLE else View.GONE
                targetIterationProgress.progress = state.targetIteration
                targetProgress.text = if (state.targetIteration > 0) 
                    "Iteration ${state.targetIteration} / 8 • ${state.targetResultBytes?.let(::formatSize) ?: "Processing..."}" 
                    else "Target: ${formatSize(state.targetSizeBytes)}"
            }
            
            if (state.isSmartMode && state.smartDecision != null) {
                val decision = state.smartDecision
                tvSmartReason.text = decision.reason
                tvSmartDetails.text = String.format(
                    Locale.getDefault(),
                    "%s • %d%% • Resize %.0f%%",
                    decision.format.name,
                    decision.quality.value,
                    (decision.resizeFactor ?: 1.0f) * 100
                )
                tvSmartQuality.text = getString(R.string.estimated_quality, decision.estimatedQualityScore)
            }

            if (lastEstimatedSize != state.estimatedSize) {
                animateSizeChange(tvEstimatedSize, lastEstimatedSize, state.estimatedSize)
                lastEstimatedSize = state.estimatedSize
            }
            
            // Heuristic reduction for UI
            val originalBytes = parseSizeToBytes(state.imageModel.size)
            if (state.estimatedSize > 0 && originalBytes > 0) {
                val diff = originalBytes - state.estimatedSize
                val percent = (diff.toFloat() / originalBytes.toFloat()) * 100
                
                when {
                    percent > 0 -> {
                        tvSavedSpace.text = String.format(Locale.getDefault(), "-%.0f%%", percent)
                        tvSavedSpace.setTextColor(MaterialColors.getColor(binding.root, R.attr.lumoraSecondary))
                    }
                    percent == 0f -> {
                        tvSavedSpace.text = getString(R.string.already_optimized)
                        tvSavedSpace.setTextColor(MaterialColors.getColor(binding.root, R.attr.lumoraPrimary))
                    }
                    else -> {
                        tvSavedSpace.text = String.format(Locale.getDefault(), "+%.0f%%", -percent)
                        tvSavedSpace.setTextColor(MaterialColors.getColor(binding.root, R.attr.lumoraError))
                    }
                }
            } else {
                tvSavedSpace.text = "..."
            }

            btnCompress.isEnabled = !state.isCompressing
            btnCompress.contentDescription = if (state.isCompressing) "Compressing in progress" else "Start compression"
            compressProgress.visibility = if (state.isCompressing) View.VISIBLE else View.GONE
            
            // Statistics Binding
            tvTargetFormat.text = when {
                state.mode == CompressionMode.SMART -> state.smartDecision?.format?.name ?: state.selectedFormat.name
                state.mode == CompressionMode.SOCIAL -> state.selectedSocialPreset.format.name
                else -> state.selectedFormat.name
            }
            tvEstimatedTime.text = getString(R.string.estimated_time_placeholder) // Defined in strings.xml

            // Hide/Show locks based on premium status
            cardTargetLock.visibility = if (state.isPremium) View.GONE else View.VISIBLE
            chipAvif.visibility = View.GONE
            
            // Accessibility
            tvEstimatedSize.contentDescription = "${getString(R.string.estimated_size)}: ${tvEstimatedSize.text}"
            tvSavedSpace.contentDescription = "${getString(R.string.saved_space)}: ${tvSavedSpace.text}"
        }
    }

    private fun showTargetSizeDialog() {
        val input = com.google.android.material.textfield.TextInputEditText(requireContext()).apply { inputType = android.text.InputType.TYPE_CLASS_TEXT; hint = "500 KB" }
        val layout = com.google.android.material.textfield.TextInputLayout(requireContext()).apply { hint = getString(R.string.target_size); addView(input) }
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.target_size).setView(layout).setPositiveButton(android.R.string.ok) { _, _ -> uz.kmax.compress.domain.adaptive.TargetSizeParser.parse(input.text?.toString().orEmpty())?.let(viewModel::onTargetSizeSelected) }.setNegativeButton(android.R.string.cancel, null).show()
    }

    private fun animateSizeChange(view: TextView, startValue: Long, endValue: Long) {
        val animator = ValueAnimator.ofFloat(startValue.toFloat(), endValue.toFloat())
        animator.duration = 500
        animator.addUpdateListener { anim ->
            val value = (anim.animatedValue as Float).toLong()
            view.text = formatSize(value)
        }
        animator.start()
    }

    private fun parseSizeToBytes(sizeStr: String): Long {
        return try {
            val value = sizeStr.substringBefore(" ").replace(",", ".").toFloat()
            val unit = sizeStr.substringAfter(" ").uppercase()
            when (unit) {
                "MB" -> (value * 1024 * 1024).toLong()
                "KB" -> (value * 1024).toLong()
                else -> value.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun handleEvent(event: CompressEvent) {
        when (event) {
            is CompressEvent.Compress -> {
                viewModel.showInterstitial(requireActivity()) {
                    // Ad dismissed or not shown
                }
            }
            is CompressEvent.NavigateCompare -> {
                val action = CompressFragmentDirections.actionCompressFragmentToCompareFragment(event.result)
                navController.navigate(action)
            }
            is CompressEvent.ShowError -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(event.message)
                    .setPositiveButton("OK", null)
                    .show()
            }
            CompressEvent.ShowPremiumPaywall -> PremiumPaywallBottomSheet.newInstance().show(parentFragmentManager, PremiumPaywallBottomSheet.TAG)
            else -> {}
        }
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
