package uz.kmax.compress.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.monetization.AdsManager
import uz.kmax.compress.core.preferences.PreferencesManager
import uz.kmax.compress.domain.usecase.GetRecentFilesUseCase
import uz.kmax.compress.domain.usecase.GetStatisticsUseCase
import uz.kmax.compress.feature.batch.viewmodel.BatchViewModel
import uz.kmax.compress.feature.home.event.HomeEvent
import uz.kmax.compress.feature.home.model.RecentFileUiModel
import uz.kmax.compress.feature.home.state.HomeUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val getRecentFilesUseCase: GetRecentFilesUseCase,
    private val analyticsManager: AnalyticsManager,
    private val adsManager: AdsManager,
    private val yandexAdsManager: uz.kmax.compress.core.monetization.YandexAdsManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<HomeEvent>()
    val event: SharedFlow<HomeEvent> = _event.asSharedFlow()

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd = _nativeAd.asStateFlow()

    private val _yandexNativeAd = MutableStateFlow<com.yandex.mobile.ads.nativeads.NativeAd?>(null)
    val yandexNativeAd = _yandexNativeAd.asStateFlow()

    init {
        loadContent()
    }

    private fun loadContent() {
        combine(
            getStatisticsUseCase(),
            getRecentFilesUseCase(5),
            preferencesManager.preferencesFlow
        ) { stats, recentHistory, prefs ->
            val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            val recentFiles = recentHistory.map { history ->
                RecentFileUiModel(
                    id = history.id.toString(),
                    name = history.compressedUri.substringAfterLast("/"),
                    originalSize = formatSize(history.originalSize),
                    compressedSize = formatSize(history.compressedSize),
                    reductionPercentage = String.format("%.0f%%", history.savedPercent),
                    date = dateFormat.format(Date(history.createdAt)),
                    uri = history.compressedUri
                )
            }
            
            HomeUiState.Content(
                userName = "User",
                greeting = "Good Morning",
                compressedCount = stats.totalFilesCount,
                storageSaved = formatSize(stats.totalSavedBytes),
                averageReduction = String.format("%.1f%%", stats.averageReduction),
                recentFiles = recentFiles,
                remainingFreeBatch = (BatchViewModel.FREE_BATCH_LIMIT - prefs.batchCount).coerceAtLeast(0),
                isPremium = prefs.isPremium
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun loadNativeAd(activity: android.app.Activity) {
        if (_nativeAd.value != null || _yandexNativeAd.value != null) return
        
        adsManager.loadNativeAd(activity, onAdLoaded = { ad ->
            _nativeAd.value = ad
        }, onAdFailed = {
            // Fallback to Yandex
            yandexAdsManager.loadNativeAd(onAdLoaded = { yandexAd ->
                _yandexNativeAd.value = yandexAd
            })
        })
    }

    fun onCompressClick() {
        viewModelScope.launch { _event.emit(HomeEvent.OpenGallery) }
    }

    fun onBatchCompressClick() {
        viewModelScope.launch { _event.emit(HomeEvent.OpenBatch) }
    }

    fun onHistoryClick() {
        viewModelScope.launch { _event.emit(HomeEvent.OpenHistory) }
    }

    fun onSettingsClick() {
        viewModelScope.launch { _event.emit(HomeEvent.OpenSettings) }
    }

    fun onPremiumClick() {
        analyticsManager.logPremiumClicked("home_banner")
        viewModelScope.launch { _event.emit(HomeEvent.OpenPremium) }
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024
        return if (kb > 1024) {
            String.format("%.1f MB", kb / 1024f)
        } else {
            "$kb KB"
        }
    }

    override fun onCleared() {
        _nativeAd.value?.destroy()
        super.onCleared()
    }
}
