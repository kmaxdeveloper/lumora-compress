package uz.kmax.compress.feature.home.event

sealed class HomeEvent {
    data object OpenGallery : HomeEvent()
    data object OpenBatch : HomeEvent()
    data object OpenHistory : HomeEvent()
    data object OpenSettings : HomeEvent()
    data object OpenPremium : HomeEvent()
    data object OpenAbout : HomeEvent()
}
