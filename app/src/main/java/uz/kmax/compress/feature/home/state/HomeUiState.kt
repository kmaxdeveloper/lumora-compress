package uz.kmax.compress.feature.home.state

import uz.kmax.compress.feature.home.model.RecentFileUiModel

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Content(
        val userName: String,
        val greeting: String,
        val compressedCount: Int,
        val storageSaved: String,
        val averageReduction: String,
        val recentFiles: List<RecentFileUiModel>,
        val remainingFreeBatch: Int,
        val isPremium: Boolean
    ) : HomeUiState()
    data object Empty : HomeUiState()
}
