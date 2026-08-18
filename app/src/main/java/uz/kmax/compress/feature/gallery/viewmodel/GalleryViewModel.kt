package uz.kmax.compress.feature.gallery.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.feature.gallery.event.GalleryEvent
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import uz.kmax.compress.feature.gallery.state.GalleryUiState
import uz.kmax.compress.core.storage.SelectedImagesRepository
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val storageManager: StorageManager,
    private val selectedImagesRepository: SelectedImagesRepository
) : ViewModel() {

    companion object { private const val MAX_BATCH_SELECTION = 500 }

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<GalleryEvent>()
    val event = _event.asSharedFlow()

    init {
        loadRecentImages()
    }

    fun onEvent(event: GalleryEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    fun loadRecentImages() {
        viewModelScope.launch {
            _uiState.value = GalleryUiState.Loading
            try {
                val images = withContext(Dispatchers.IO) { storageManager.getRecentImages(30) }.map { 
                    GalleryImageUiModel(
                        uri = it.uri,
                        name = it.name,
                        size = formatSize(it.size),
                        width = it.width,
                        height = it.height,
                        mimeType = it.mimeType,
                        date = it.dateAdded
                    )
                }
                _uiState.value = if (images.isEmpty()) GalleryUiState.Empty else GalleryUiState.Content(images)
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(e.message ?: "Failed to load images")
            }
        }
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            try {
                val model = fetchMetadata(uri)
                _event.emit(GalleryEvent.NavigateCompress(model))
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(e.message ?: "Error processing selected image")
            }
        }
    }

    fun onImagesPicked(uris: List<Uri>) {
        viewModelScope.launch {
            if (uris.isEmpty()) return@launch
            if (uris.size > MAX_BATCH_SELECTION) {
                _uiState.value = GalleryUiState.Error("Select up to $MAX_BATCH_SELECTION images per batch")
                return@launch
            }
            
            _uiState.value = GalleryUiState.Loading
            
            try {
                if (uris.size == 1) {
                    val model = fetchMetadata(uris[0])
                    _event.emit(GalleryEvent.NavigateCompress(model))
                } else {
                    // Process all selected URIs. 
                    // SelectedImagesRepository prevents TransactionTooLargeException by keeping them out of Navigation.
                    val models = uris.map { uri ->
                        async { fetchMetadata(uri) }
                    }.awaitAll()
                    
                    val batchId = selectedImagesRepository.store(models)
                    _event.emit(GalleryEvent.NavigateBatch(batchId))
                }
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error("Invalid request or limit exceeded: ${e.message}")
            } finally {
                // Return to normal state or load images
                loadRecentImages()
            }
        }
    }

    private suspend fun fetchMetadata(uri: Uri): GalleryImageUiModel = withContext(Dispatchers.IO) {
        val metadata = storageManager.getImageMetadata(uri)
        if (metadata != null) {
            GalleryImageUiModel(
                uri = metadata.uri,
                name = metadata.name,
                size = formatSize(metadata.size),
                width = metadata.width,
                height = metadata.height,
                mimeType = metadata.mimeType,
                date = metadata.dateAdded
            )
        } else {
            GalleryImageUiModel(
                uri = uri,
                name = "image_${System.currentTimeMillis()}",
                size = "0 KB",
                width = 0,
                height = 0,
                mimeType = "image/*",
                date = System.currentTimeMillis() / 1000
            )
        }
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024
        return if (kb > 1024) {
            String.format("%.1f MB", kb / 1024f)
        } else {
            "$kb KB"
        }
    }
}
