package uz.kmax.compress.core.premium

sealed interface PremiumState {
    data object Free : PremiumState
    data object Premium : PremiumState
    data object GracePeriod : PremiumState
    data object Checking : PremiumState
    data class Error(val message: String) : PremiumState
}
