package uz.kmax.compress.feature.splash.event

sealed class SplashEvent {
    data object NavigateToOnboarding : SplashEvent()
    data object NavigateToHome : SplashEvent()
}
