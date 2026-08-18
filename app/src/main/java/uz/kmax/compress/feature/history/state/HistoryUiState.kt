package uz.kmax.compress.feature.history.state

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Content(
        val selectionMode: Boolean = false,
        val selectedCount: Int = 0,
        val currentQuery: String = "",
        val currentSort: String = "NEWEST"
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}
