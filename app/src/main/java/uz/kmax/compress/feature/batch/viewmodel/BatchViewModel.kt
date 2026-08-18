package uz.kmax.compress.feature.batch.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.compressor.CompressionEngine
import uz.kmax.compress.core.compressor.CompressionEngineStage
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.CompressionResult
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.core.preferences.PreferencesManager
import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.usecase.InsertHistoryUseCase
import uz.kmax.compress.domain.usecase.GetImagesByCategoryUseCase
import uz.kmax.compress.domain.model.CategoryType
import uz.kmax.compress.feature.batch.model.BatchItemUiModel
import uz.kmax.compress.feature.batch.state.BatchUiState
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import java.util.Locale
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import uz.kmax.compress.core.service.CompressionService
import uz.kmax.compress.core.service.BatchState
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.kmax.compress.core.storage.SelectedImagesRepository

@HiltViewModel
class BatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val engine: CompressionEngine,
    private val storageManager: StorageManager,
    private val preferencesManager: PreferencesManager,
    private val insertHistoryUseCase: InsertHistoryUseCase,
    private val getImagesByCategoryUseCase: GetImagesByCategoryUseCase,
    private val analyticsManager: AnalyticsManager,
    private val selectedImagesRepository: SelectedImagesRepository
) : ViewModel() {

    companion object {
        const val FREE_BATCH_LIMIT = 3
    }

    private val _uiState = MutableStateFlow(BatchUiState())
    val uiState = _uiState.asStateFlow()

    private var batchJob: Job? = null
    private var startTime: Long = 0
    private var processedCountAtStart: Int = 0

    init {
        loadImages()
        observePreferences()
        observeServiceState()
    }

    private fun observeServiceState() {
        CompressionService.batchState
            .onEach { state ->
                when (state) {
                    is BatchState.Idle -> {
                        _uiState.update { it.copy(isRunning = false) }
                    }
                    is BatchState.Processing -> {
                        _uiState.update { it.copy(
                            isRunning = true,
                            currentItem = state.currentItem
                        ) }
                        state.lastUpdate?.let { update ->
                            updateItemState(
                                update.id, 
                                update.status, 
                                update.progress,
                                update.originalSize,
                                update.compressedSize
                            )
                        }
                        updateOverallProgress()
                    }
                    is BatchState.Finished -> {
                        _uiState.update { it.copy(isRunning = false, remainingTime = "Finished") }
                        analyticsManager.logBatchFinished(state.success, state.failed)
                        if (!_uiState.value.isPremium && state.success > 0) {
                            preferencesManager.incrementBatchCount()
                        }
                    }
                    is BatchState.Failed -> {
                        _uiState.update { it.copy(isRunning = false, remainingTime = state.message) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePreferences() {
        preferencesManager.preferencesFlow
            .onEach { prefs ->
                _uiState.update { it.copy(
                    isPremium = prefs.isPremium,
                    remainingFreeCount = (FREE_BATCH_LIMIT - prefs.batchCount).coerceAtLeast(0)
                ) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadImages() {
        val batchId = savedStateHandle.get<String>("batchId")
        val categoryTypeName = savedStateHandle.get<String>("categoryType")

        viewModelScope.launch(Dispatchers.Default) {
            val items = when {
                batchId != null -> {
                    selectedImagesRepository.peek(batchId)?.map { model ->
                        BatchItemUiModel(
                            id = java.util.UUID.randomUUID().toString(),
                            uri = model.uri,
                            name = model.name
                        )
                    } ?: emptyList()
                }
                categoryTypeName != null -> {
                    val categoryType = CategoryType.valueOf(categoryTypeName)
                    val images = getImagesByCategoryUseCase(categoryType)
                    images.map { image ->
                        val model = uz.kmax.compress.feature.guardian.mapper.StorageGuardianMapper.toUiModel(image)
                        BatchItemUiModel(
                            id = java.util.UUID.randomUUID().toString(),
                            uri = model.uri,
                            name = model.name
                        )
                    }
                }
                else -> emptyList()
            }
            
            withContext(Dispatchers.Main) {
                items.forEach { itemMap[it.id] = it }
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun startBatch() {
        if (_uiState.value.isRunning) return
        
        // Check limit
        if (!_uiState.value.isPremium && _uiState.value.remainingFreeCount <= 0) {
            return
        }

        val batchId = savedStateHandle.get<String>("batchId")
        if (batchId != null) {
            CompressionService.startBatch(context, batchId)
            startTime = System.currentTimeMillis()
            processedCountAtStart = _uiState.value.items.count { it.status == BatchItemUiModel.Status.COMPLETED }
        } else {
            // If we selected by category, we need to store them first
            val items = _uiState.value.items
            if (items.isNotEmpty()) {
                val models = items.map { 
                    GalleryImageUiModel(
                        uri = it.uri,
                        name = it.name,
                        size = "", width = 0, height = 0, mimeType = "", date = 0
                    )
                }
                val newBatchId = selectedImagesRepository.store(models)
                CompressionService.startBatch(context, newBatchId)
                startTime = System.currentTimeMillis()
                processedCountAtStart = 0
            }
        }
    }

    private val itemMap = java.util.concurrent.ConcurrentHashMap<String, BatchItemUiModel>()

    private fun updateItemState(
        id: String, 
        status: BatchItemUiModel.Status, 
        progress: Int,
        originalSize: Long = 0,
        compressedSize: Long = 0
    ) {
        val currentItem = itemMap[id] ?: return
        
        // Skip redundant updates for progress to save CPU/Memory on large lists
        if (currentItem.status == status && 
            status == BatchItemUiModel.Status.PROCESSING && 
            kotlin.math.abs(currentItem.progress - progress) < 5) {
            return
        }

        val updatedItem = currentItem.copy(
            status = status, 
            progress = progress,
            originalSize = if (originalSize > 0) originalSize else currentItem.originalSize,
            compressedSize = if (compressedSize > 0) compressedSize else currentItem.compressedSize
        )
        
        itemMap[id] = updatedItem
        
        // Update the list in UI state
        _uiState.update { state ->
            // Use values.toList() carefully. For very large lists, we might want to 
            // only update the specific index if we had a more granular StateFlow.
            state.copy(items = itemMap.values.toList())
        }
    }

    private fun updateOverallProgress() {
        val currentState = _uiState.value
        val items = currentState.items
        val total = items.size
        if (total == 0) return

        val completedCount = items.count { it.status == BatchItemUiModel.Status.COMPLETED || it.status == BatchItemUiModel.Status.FAILED }
        val overallProgress = (completedCount * 100) / total
        
        // Time estimation
        val currentTime = System.currentTimeMillis()
        val elapsedSinceStart = currentTime - startTime
        val newlyProcessed = completedCount - processedCountAtStart
        
        val remainingTimeStr = if (newlyProcessed > 0 && currentState.isRunning) {
            val avgTimePerItem = elapsedSinceStart / newlyProcessed
            val itemsLeft = total - completedCount
            val remainingMillis = itemsLeft * avgTimePerItem
            formatRemainingTime(remainingMillis)
        } else if (completedCount == total) {
            "0s"
        } else {
            "--:--"
        }

        _uiState.update { it.copy(
            overallProgress = overallProgress,
            remainingTime = remainingTimeStr
        ) }
    }

    private fun formatRemainingTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        
        return when {
            hours > 0 -> String.format(Locale.getDefault(), "%dh %dm %ds", hours, minutes, seconds)
            minutes > 0 -> String.format(Locale.getDefault(), "%dm %ds", minutes, seconds)
            else -> String.format(Locale.getDefault(), "%ds", seconds)
        }
    }

    fun pauseBatch() {
        _uiState.update { it.copy(isRunning = false) }
        batchJob?.cancel(null)
        CompressionService.stop(context)
    }
}
