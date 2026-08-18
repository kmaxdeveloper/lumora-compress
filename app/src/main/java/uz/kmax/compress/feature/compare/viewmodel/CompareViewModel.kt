package uz.kmax.compress.feature.compare.viewmodel

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.kmax.compress.core.monetization.BillingManager
import uz.kmax.compress.core.monetization.PlayServicesManager
import uz.kmax.compress.core.compressor.writer.OutputWriter
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.domain.usecase.DeleteHistoryUseCase
import uz.kmax.compress.feature.compare.event.CompareEvent
import uz.kmax.compress.feature.compare.model.CompareResultUiModel
import uz.kmax.compress.feature.compare.state.CompareUiState
import javax.inject.Inject

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val outputWriter: OutputWriter,
    private val storageManager: StorageManager,
    private val deleteHistoryUseCase: DeleteHistoryUseCase,
    private val playServicesManager: PlayServicesManager
) : ViewModel() {

    private val result: CompareResultUiModel = savedStateHandle.get<CompareResultUiModel>("compareResult")
        ?: throw IllegalArgumentException("CompareResultUiModel is required")

    private val _uiState = MutableStateFlow<CompareUiState>(CompareUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<CompareEvent>()
    val event = _event.asSharedFlow()

    init {
        calculateStats()
    }

    private fun calculateStats() {
        viewModelScope.launch {
            val savedBytes = result.savedBytes
            val estimatedPhotos = (savedBytes / (3 * 1024 * 1024)).toInt()
            val estimatedSongs = (savedBytes / (5 * 1024 * 1024)).toInt()
            val estimatedVideos = (savedBytes / (50 * 1024 * 1024)).toInt()

            _uiState.value = CompareUiState.Content(
                result = result,
                estimatedPhotos = estimatedPhotos.coerceAtLeast(0),
                estimatedSongs = estimatedSongs.coerceAtLeast(0),
                estimatedVideos = estimatedVideos.coerceAtLeast(0)
            )
        }
    }

    fun onSliderMoved(position: Float) {
        _uiState.update { state ->
            if (state is CompareUiState.Content) {
                state.copy(sliderPosition = position)
            } else state
        }
    }

    fun onSaveClicked(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { if (it is CompareUiState.Content) it.copy(isSaving = true) else it }
            
            try {
                val file = storageManager.uriToFile(result.compressedUri)
                if (file != null && file.exists()) {
                    val mimeType = when (result.format.uppercase()) {
                        "PNG" -> "image/png"
                        "WEBP" -> "image/webp"
                        else -> "image/jpeg"
                    }
                    
                    val metadata = storageManager.getImageMetadata(result.originalUri)
                    val baseName = metadata?.name?.substringBeforeLast(".") ?: "image_${System.currentTimeMillis()}"
                    val fileName = "Lumora_${baseName}.${result.format.lowercase()}"
                    
                    val savedUri = storageManager.saveToPublicStorage(file, mimeType, fileName)
                    
                    if (savedUri != null) {
                        _event.emit(CompareEvent.ShowSnackbar("Saved to Gallery"))
                        // Request review after saving
                        playServicesManager.requestReview(activity)
                    } else {
                        _event.emit(CompareEvent.ShowSnackbar("Save failed: Could not create file in gallery"))
                    }
                } else {
                    _event.emit(CompareEvent.ShowSnackbar("Save failed: Compressed file not found"))
                }
            } catch (e: Exception) {
                _event.emit(CompareEvent.ShowSnackbar("Save failed: ${e.message}"))
            } finally {
                _uiState.update { if (it is CompareUiState.Content) it.copy(isSaving = false) else it }
            }
        }
    }

    fun onShareClicked() {
        viewModelScope.launch {
            _event.emit(CompareEvent.Share(result.compressedUri))
        }
    }

    fun onDeleteOriginalClicked() {
        viewModelScope.launch {
            _event.emit(CompareEvent.ShowDialog("Delete Original", "Are you sure?"))
        }
    }

    fun onHomeClicked() {
        viewModelScope.launch {
            _event.emit(CompareEvent.NavigateHome)
        }
    }
}
