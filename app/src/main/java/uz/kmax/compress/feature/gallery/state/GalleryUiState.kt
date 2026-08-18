package uz.kmax.compress.feature.gallery.state

import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data object Empty : GalleryUiState
    data class Content(val images: List<GalleryImageUiModel>) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
}
