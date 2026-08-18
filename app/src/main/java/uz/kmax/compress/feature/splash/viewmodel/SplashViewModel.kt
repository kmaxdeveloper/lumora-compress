package uz.kmax.compress.feature.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uz.kmax.compress.BuildConfig
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.config.ConfigManager
import uz.kmax.compress.domain.usecase.IsFirstLaunchUseCase
import uz.kmax.compress.feature.splash.event.SplashEvent
import uz.kmax.compress.feature.splash.state.SplashUiState
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val isFirstLaunchUseCase: IsFirstLaunchUseCase,
    private val analyticsManager: AnalyticsManager,
    private val configManager: ConfigManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SplashEvent>()
    val event: SharedFlow<SplashEvent> = _event.asSharedFlow()

    init {
        startSplashFlow()
    }

    private fun startSplashFlow() {
        viewModelScope.launch {
            delay(700)
            
            // Observe config updates
            configManager.configFlow.collect { config ->
                if (config.maintenanceMode) {
                    // Handle Maintenance Mode
                    return@collect
                }
                
                if (config.forceUpdateEnabled && BuildConfig.VERSION_CODE < config.minAppVersion) {
                    // Handle Force Update
                    return@collect
                }

                val isFirstLaunch = isFirstLaunchUseCase()
                if (isFirstLaunch) {
                    analyticsManager.logEvent("first_open")
                    _event.emit(SplashEvent.NavigateToOnboarding)
                } else {
                    _event.emit(SplashEvent.NavigateToHome)
                }
            }
        }
    }
}
