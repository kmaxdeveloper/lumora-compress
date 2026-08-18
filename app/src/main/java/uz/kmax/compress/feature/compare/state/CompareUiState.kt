package uz.kmax.compress.feature.compare.state

import uz.kmax.compress.feature.compare.model.CompareResultUiModel

sealed interface CompareUiState {
    data object Loading : CompareUiState
    data class Content(
        val result: CompareResultUiModel,
        val sliderPosition: Float = 0.5f,
        val isSaving: Boolean = false,
        val estimatedPhotos: Int = 0,
        val estimatedVideos: Int = 0,
        val estimatedSongs: Int = 0
    ) : CompareUiState
    data class Error(val message: String) : CompareUiState
}
