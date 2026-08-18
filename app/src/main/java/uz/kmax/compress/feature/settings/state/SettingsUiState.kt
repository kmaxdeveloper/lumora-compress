package uz.kmax.compress.feature.settings.state

import uz.kmax.compress.core.preferences.AppPreferences
import uz.kmax.compress.core.premium.PremiumState

data class SettingsUiState(
    val preferences: AppPreferences? = null,
    val premiumState: PremiumState = PremiumState.Checking,
    val cacheSize: String = "0 B",
    val isLoading: Boolean = true
)
