package uz.kmax.compress.feature.settings.event

sealed interface SettingsEvent {
    data object NavigateBack : SettingsEvent
    data object OpenPrivacyPolicy : SettingsEvent
    data object OpenTerms : SettingsEvent
    data object OpenLicenses : SettingsEvent
    data object RateApp : SettingsEvent
    data object ShareApp : SettingsEvent
    data class ShowSnackbar(val message: String) : SettingsEvent
}
