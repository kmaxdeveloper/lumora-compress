package uz.kmax.compress.feature.guardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.domain.model.CategoryType
import uz.kmax.compress.domain.usecase.AnalyzeStorageUseCase
import uz.kmax.compress.feature.guardian.event.StorageGuardianEvent
import uz.kmax.compress.feature.guardian.state.StorageGuardianUiState
import javax.inject.Inject

@HiltViewModel
class StorageGuardianViewModel @Inject constructor(
    private val analyzeStorageUseCase: AnalyzeStorageUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageGuardianUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<StorageGuardianEvent>()
    val event = _event.asSharedFlow()

    init {
        startScan()
        analyticsManager.logEvent("guardian_opened")
    }

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            analyticsManager.logEvent("guardian_scan_started")
            try {
                val result = analyzeStorageUseCase()
                _uiState.update { it.copy(isLoading = false, result = result) }
                analyticsManager.logEvent("guardian_scan_completed")
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _event.emit(StorageGuardianEvent.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun onActionClicked(category: CategoryType) {
        viewModelScope.launch {
            analyticsManager.logEvent("guardian_action_clicked", android.os.Bundle().apply {
                putString("category", category.name)
            })
            _event.emit(StorageGuardianEvent.NavigateToBatch(category))
        }
    }

    fun onNavigateToBatch() {
        viewModelScope.launch {
            analyticsManager.logEvent("guardian_action_clicked", android.os.Bundle().apply {
                putString("action", "batch")
            })
            _event.emit(StorageGuardianEvent.NavigateToGallery)
        }
    }
}
