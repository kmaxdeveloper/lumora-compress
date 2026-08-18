package uz.kmax.compress.feature.compress.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.analytics.CrashlyticsManager
import uz.kmax.compress.core.compressor.*
import uz.kmax.compress.core.compressor.metadata.MetadataOptions
import uz.kmax.compress.core.monetization.AdsManager
import uz.kmax.compress.core.preferences.PreferencesManager
import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.usecase.InsertHistoryUseCase
import uz.kmax.compress.feature.compare.model.CompareResultUiModel
import uz.kmax.compress.feature.compress.event.CompressEvent
import uz.kmax.compress.feature.compress.state.CompressUiState
import android.net.Uri
import uz.kmax.compress.core.smart.SmartCompressionEngine
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import javax.inject.Inject
import uz.kmax.compress.core.social.SocialPreset
import uz.kmax.compress.core.premium.PremiumManager
import uz.kmax.compress.core.premium.PremiumFeature
import uz.kmax.compress.domain.usecase.CreateSocialCompressionRequestUseCase
import uz.kmax.compress.feature.compress.state.CompressionMode
import uz.kmax.compress.domain.usecase.CompressionLoopUseCase
import uz.kmax.compress.domain.adaptive.CompressionLoopResult
import uz.kmax.compress.core.service.CompressionService
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class CompressViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val compressionEngine: CompressionEngine,
    private val smartEngine: SmartCompressionEngine,
    private val insertHistoryUseCase: InsertHistoryUseCase,
    private val adsManager: AdsManager,
    private val analyticsManager: AnalyticsManager,
    private val crashlyticsManager: CrashlyticsManager,
    private val storageManager: StorageManager,
    private val preferencesManager: PreferencesManager,
    private val premiumManager: PremiumManager,
    private val createSocialCompressionRequest: CreateSocialCompressionRequestUseCase,
    private val compressionLoopUseCase: CompressionLoopUseCase
) : ViewModel() {

    private val imageModel: GalleryImageUiModel = savedStateHandle.get<GalleryImageUiModel>("imageModel")
        ?: throw IllegalArgumentException("GalleryImageUiModel is required")

    private val _uiState = MutableStateFlow(CompressUiState(imageModel = imageModel))
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CompressEvent>()
    val event = _event.asSharedFlow()

    private var estimationJob: Job? = null
    private var activeCompressionRequestId: String? = null

    init {
        estimateSize()
        adsManager.loadInterstitial()
        observePreferences()
        observeCompressionService()
    }

    private fun observePreferences() {
        preferencesManager.preferencesFlow
            .onEach { prefs ->
                _uiState.update { it.copy(isPremium = prefs.isPremium) }
            }
            .launchIn(viewModelScope)
    }

    fun onFormatSelected(format: CompressionFormat) {
        if (format == CompressionFormat.AVIF && !premiumManager.isFeatureUnlocked(PremiumFeature.AVIF_FORMAT)) {
            viewModelScope.launch { _event.emit(CompressEvent.ShowPremiumPaywall) }
            return
        }
        _uiState.update { it.copy(selectedFormat = format, isSmartMode = false) }
        estimateSize()
    }

    fun onQualityChanged(quality: Int) {
        _uiState.update { it.copy(selectedQuality = quality, isSmartMode = false) }
        estimateSize()
    }

    fun onResizeToggled(enabled: Boolean) {
        _uiState.update { it.copy(resizeEnabled = enabled, isSmartMode = false) }
        estimateSize()
    }

    fun onMetadataStrategySelected(strategy: MetadataOptions.Strategy) {
        _uiState.update { it.copy(metadataStrategy = strategy, isSmartMode = false) }
        estimateSize()
    }

    fun onModeChanged(isSmart: Boolean) {
        _uiState.update { it.copy(isSmartMode = isSmart, mode = if (isSmart) CompressionMode.SMART else CompressionMode.MANUAL) }
        estimateSize()
    }

    fun onSocialModeSelected() { _uiState.update { it.copy(isSmartMode = false, mode = CompressionMode.SOCIAL) }; estimateSize() }
    fun onTargetSizeModeSelected() {
        if (!premiumManager.isFeatureUnlocked(PremiumFeature.TARGET_SIZE_COMPRESSION)) { viewModelScope.launch { _event.emit(CompressEvent.ShowPremiumPaywall) }; return }
        _uiState.update { it.copy(isSmartMode = false, mode = CompressionMode.TARGET_SIZE) }
        analyticsManager.logTargetLoop("target_mode_selected", _uiState.value.targetSizeBytes)
    }
    fun onTargetSizeSelected(bytes: Long) { _uiState.update { it.copy(targetSizeBytes = bytes) } }

    fun onSocialPresetSelected(preset: SocialPreset) {
        if (preset.requiresPremium && !premiumManager.isFeatureUnlocked(PremiumFeature.SOCIAL_MEDIA_PRESETS)) {
            viewModelScope.launch { _event.emit(CompressEvent.ShowPremiumPaywall) }
            return
        }
        analyticsManager.logSocialPresetSelected(preset.name)
        _uiState.update { it.copy(selectedSocialPreset = preset) }
        estimateSize()
    }

    private fun estimateSize() {
        estimationJob?.cancel()
        estimationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (_uiState.value.mode == CompressionMode.SMART) {
                try {
                    val decision = smartEngine.makeDecision(imageModel.uri)
                    _uiState.update { it.copy(
                        smartDecision = decision,
                        estimatedSize = decision.estimatedOutputSize,
                        isLoading = false
                    ) }
                } catch (e: Exception) {
                    crashlyticsManager.recordException(e)
                    _uiState.update { it.copy(isSmartMode = false, isLoading = false) }
                    estimateManualSize()
                }
            } else if (_uiState.value.mode == CompressionMode.SOCIAL) {
                val preset = _uiState.value.selectedSocialPreset
                val baseSize = imageModel.size.toLongOrNull() ?: 1024 * 1024L
                _uiState.update { it.copy(estimatedSize = preset.targetSizeBytes ?: (baseSize * preset.quality / 125L), isLoading = false) }
            } else {
                estimateManualSize()
            }
        }
    }

    private suspend fun estimateManualSize() {
        delay(300)
        val parsedSize = imageModel.size.toLongOrNull() ?: (1024 * 1024L)
        val baseSize = if (parsedSize > 0) parsedSize else 1024 * 1024L
        val qualityFactor = _uiState.value.selectedQuality / 100f
        val formatFactor = when (_uiState.value.selectedFormat) {
            CompressionFormat.JPEG -> 0.8f
            CompressionFormat.PNG -> 1.2f
            CompressionFormat.WEBP_LOSSY -> 0.6f
            CompressionFormat.WEBP_LOSSLESS -> 0.9f
            CompressionFormat.AVIF -> 0.4f
            else -> 0.7f
        }
        val resizeFactor = if (_uiState.value.resizeEnabled) 0.5f else 1.0f
        val estimated = (baseSize.toFloat() * qualityFactor * formatFactor * resizeFactor).toLong()
        _uiState.update { it.copy(estimatedSize = estimated, isLoading = false) }
    }

    private fun observeCompressionService() {
        CompressionService.singleState
            .onEach { state ->
                val stateRequestId = when (state) {
                    is uz.kmax.compress.core.service.SingleCompressionState.Processing -> state.requestId
                    is uz.kmax.compress.core.service.SingleCompressionState.Completed -> state.requestId
                    is uz.kmax.compress.core.service.SingleCompressionState.Failed -> state.requestId
                    else -> null
                }
                if (stateRequestId != null && stateRequestId != activeCompressionRequestId) return@onEach
                
                when (state) {
                    is uz.kmax.compress.core.service.SingleCompressionState.Processing -> {
                        _uiState.update { it.copy(isCompressing = true, targetIteration = state.iteration) }
                    }
                    is uz.kmax.compress.core.service.SingleCompressionState.Completed -> {
                        activeCompressionRequestId = null
                        _uiState.update { it.copy(isCompressing = false, targetResultBytes = state.result.compressedSize) }
                        _event.emit(CompressEvent.Compress)
                        navigateToCompare(state.result)
                        state.targetResult?.takeIf { !it.reachedTarget }?.let {
                            _event.emit(CompressEvent.ShowError("Unable to reach target size. Closest result: ${state.result.compressedSize / 1024} KB"))
                        }
                    }
                    is uz.kmax.compress.core.service.SingleCompressionState.Failed -> {
                        activeCompressionRequestId = null
                        _uiState.update { it.copy(isCompressing = false) }
                        analyticsManager.logCompressFailed(state.message)
                        _event.emit(CompressEvent.ShowError(state.message))
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCompressClicked() {
        viewModelScope.launch {
            if (_uiState.value.isCompressing || activeCompressionRequestId != null) return@launch
            val state = _uiState.value
            
            val format = if (state.isSmartMode) state.smartDecision?.format ?: state.selectedFormat else state.selectedFormat
            val quality = if (state.isSmartMode) state.smartDecision?.quality ?: CompressionQuality.Custom(state.selectedQuality) else CompressionQuality.Custom(state.selectedQuality)
            val metadataStrategy = if (state.isSmartMode) state.smartDecision?.metadataStrategy ?: MetadataOptions.Strategy.REMOVE_ALL else state.metadataStrategy
            
            val resizeFactor = if (state.isSmartMode) state.smartDecision?.resizeFactor ?: 1.0f else 1.0f

            analyticsManager.logCompressStarted(format.name, quality.value)
            
            val ext = when (format) {
                CompressionFormat.AUTO -> "jpg"
                CompressionFormat.AVIF -> "avif"
                else -> format.name.lowercase()
            }
            val tempOutputFile = storageManager.createTempFile(ext, imageModel.name)
            val outputUri = Uri.fromFile(tempOutputFile)

            var finalResizeOptions: CompressionRequest.ResizeOptions? = null
            if (state.isSmartMode && resizeFactor < 1.0f) {
                val metadata = storageManager.getImageMetadata(state.imageModel.uri)
                if (metadata != null && metadata.width > 0 && metadata.height > 0) {
                    finalResizeOptions = CompressionRequest.ResizeOptions(
                        width = (metadata.width * resizeFactor).toInt(),
                        height = (metadata.height * resizeFactor).toInt()
                    )
                }
            }

            val request = if (state.mode == CompressionMode.SOCIAL) {
                createSocialCompressionRequest(state.imageModel.uri, outputUri, state.selectedSocialPreset, state.smartDecision)
            } else CompressionRequest(
                inputUri = state.imageModel.uri,
                outputUri = outputUri,
                format = format,
                quality = quality,
                metadataOptions = MetadataOptions(strategy = metadataStrategy),
                resizeOptions = finalResizeOptions
            )

            _uiState.update { it.copy(isCompressing = true, targetIteration = 0, targetResultBytes = null) }
            if (state.mode == CompressionMode.TARGET_SIZE) analyticsManager.logTargetLoop("target_loop_started", state.targetSizeBytes)
            // The foreground service is the sole executor and publishes the authoritative result.
            activeCompressionRequestId = CompressionService.start(
                context,
                request,
                state.targetSizeBytes.takeIf { state.mode == CompressionMode.TARGET_SIZE }
            )
        }
    }

    private fun navigateToCompare(result: CompressionResult.Success) {
        viewModelScope.launch {
            val compareModel = CompareResultUiModel(
                originalUri = imageModel.uri,
                compressedUri = result.outputUri,
                originalSize = result.originalSize,
                compressedSize = result.compressedSize,
                originalResolution = result.statistics.originalResolution,
                compressedResolution = result.statistics.outputResolution,
                format = result.outputUri.lastPathSegment?.substringAfterLast(".")?.uppercase() ?: "JPG",
                processingTime = result.elapsedTime,
                savedBytes = result.savedBytes,
                savedPercentage = result.savedPercent
            )
            _event.emit(CompressEvent.NavigateCompare(compareModel))
        }
    }

    fun showInterstitial(activity: android.app.Activity, onDismiss: () -> Unit) {
        adsManager.showInterstitial(activity, onDismiss)
    }
}
