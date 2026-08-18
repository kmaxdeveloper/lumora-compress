package uz.kmax.compress.feature.history.event

import uz.kmax.compress.feature.history.model.HistoryUiModel

sealed interface HistoryEvent {
    data class NavigateCompare(val model: HistoryUiModel) : HistoryEvent
    data object NavigateHome : HistoryEvent
    data class ShowSnackbar(val message: String, val undoAction: (() -> Unit)? = null) : HistoryEvent
    data class ShowDeleteDialog(val count: Int) : HistoryEvent
}
