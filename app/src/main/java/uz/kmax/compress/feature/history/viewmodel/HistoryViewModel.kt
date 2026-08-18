package uz.kmax.compress.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.domain.usecase.*
import uz.kmax.compress.feature.history.event.HistoryEvent
import uz.kmax.compress.feature.history.model.HistoryUiModel
import uz.kmax.compress.feature.history.state.HistoryUiState
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val deleteHistoryUseCase: DeleteHistoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Content())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HistoryEvent>()
    val event = _event.asSharedFlow()

    private val _query = MutableStateFlow("")
    private val _sort = MutableStateFlow("NEWEST")

    val historyPagingData: Flow<PagingData<HistoryUiModel>> = combine(_query, _sort) { q, s ->
        q to s
    }.flatMapLatest { (q, s) ->
        getHistoryUseCase(q, s)
    }.map { pagingData ->
        pagingData.map { history ->
            HistoryUiModel(
                id = history.id,
                originalUri = android.net.Uri.parse(history.originalUri),
                compressedUri = android.net.Uri.parse(history.compressedUri),
                originalSize = formatSize(history.originalSize),
                compressedSize = formatSize(history.compressedSize),
                savedBytes = formatSize(history.savedBytes),
                savedPercent = String.format("%.1f%%", history.savedPercent),
                format = history.format,
                resolution = history.resolution,
                date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(history.createdAt)),
                favorite = history.favorite
            )
        }
    }.cachedIn(viewModelScope)

    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        analyticsManager.logHistoryOpened()
    }

    fun onSearch(query: String) {
        _query.value = query
    }

    fun onSort(sort: String) {
        _sort.value = sort
    }

    fun toggleSelection(id: Long) {
        selectedIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
        updateSelectionState()
    }

    private fun updateSelectionState() {
        _uiState.update { state ->
            if (state is HistoryUiState.Content) {
                state.copy(
                    selectionMode = selectedIds.value.isNotEmpty(),
                    selectedCount = selectedIds.value.size
                )
            } else state
        }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
        updateSelectionState()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val idsToDelete = selectedIds.value.toList()
            deleteHistoryUseCase(idsToDelete)
            _event.emit(HistoryEvent.ShowSnackbar("Deleted ${idsToDelete.size} items"))
            clearSelection()
        }
    }

    fun onFavoriteToggled(id: Long, favorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(id, !favorite)
        }
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024
        return if (kb > 1024) {
            String.format(Locale.getDefault(), "%.1f MB", kb / 1024f)
        } else {
            "$kb KB"
        }
    }
}
