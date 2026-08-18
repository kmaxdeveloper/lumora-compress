package uz.kmax.compress.feature.premium.event

sealed interface PremiumEvent {
    data object NavigateBack : PremiumEvent
    data class ShowSnackbar(val message: String) : PremiumEvent
}
