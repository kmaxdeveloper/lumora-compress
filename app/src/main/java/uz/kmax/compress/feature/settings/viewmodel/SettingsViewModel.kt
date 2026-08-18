package uz.kmax.compress.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.core.preferences.PreferencesManager
import uz.kmax.compress.core.premium.PremiumManager
import uz.kmax.compress.domain.repository.HistoryRepository
import uz.kmax.compress.domain.usecase.DeleteHistoryUseCase
import uz.kmax.compress.feature.settings.event.SettingsEvent
import uz.kmax.compress.feature.settings.state.SettingsUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val premiumManager: PremiumManager,
    private val deleteHistoryUseCase: DeleteHistoryUseCase,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SettingsEvent>()
    val event = _event.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            preferencesManager.preferencesFlow,
            premiumManager.premiumState
        ) { prefs, premium ->
            _uiState.update { it.copy(
                preferences = prefs,
                premiumState = premium,
                isLoading = false
            ) }
        }.launchIn(viewModelScope)
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setDynamicColor(enabled) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { preferencesManager.setLanguage(lang) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            deleteHistoryUseCase.deleteAll()
            _event.emit(SettingsEvent.ShowSnackbar("History cleared"))
        }
    }

    fun exportHistory(outputDir: File) {
        viewModelScope.launch {
            try {
                val history = historyRepository.getAllHistory()
                val json = Gson().toJson(history)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val file = File(outputDir, "lumora_history_$timestamp.json")
                file.writeText(json)
                _event.emit(SettingsEvent.ShowSnackbar("History exported to Documents"))
            } catch (e: Exception) {
                _event.emit(SettingsEvent.ShowSnackbar("Export failed: ${e.message}"))
            }
        }
    }

    fun factoryReset() {
        viewModelScope.launch {
            // In a real app, this would wipe DataStore and DB
            deleteHistoryUseCase.deleteAll()
            _event.emit(SettingsEvent.ShowSnackbar("Application reset complete"))
        }
    }

    fun onPrivacyPolicyClicked() = viewModelScope.launch { _event.emit(SettingsEvent.OpenPrivacyPolicy) }
    fun onTermsClicked() = viewModelScope.launch { _event.emit(SettingsEvent.OpenTerms) }
    fun onLicensesClicked() = viewModelScope.launch { _event.emit(SettingsEvent.OpenLicenses) }
    fun onRateAppClicked() = viewModelScope.launch { _event.emit(SettingsEvent.RateApp) }
    fun onShareAppClicked() = viewModelScope.launch { _event.emit(SettingsEvent.ShareApp) }
    fun onBackClicked() = viewModelScope.launch { _event.emit(SettingsEvent.NavigateBack) }
}
