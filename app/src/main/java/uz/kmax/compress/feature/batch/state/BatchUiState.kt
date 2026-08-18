package uz.kmax.compress.feature.batch.state

import uz.kmax.compress.feature.batch.model.BatchItemUiModel

data class BatchUiState(
    val items: List<BatchItemUiModel> = emptyList(),
    val isRunning: Boolean = false,
    val overallProgress: Int = 0,
    val remainingTime: String = "--:--",
    val remainingFreeCount: Int = 0,
    val isPremium: Boolean = false,
    val currentItem: BatchItemUiModel? = null
)
