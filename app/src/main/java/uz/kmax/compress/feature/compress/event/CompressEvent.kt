package uz.kmax.compress.feature.compress.event

import uz.kmax.compress.feature.compare.model.CompareResultUiModel

sealed class CompressEvent {
    data object Compress : CompressEvent()
    data object Cancel : CompressEvent()
    data object Retry : CompressEvent()
    data class NavigateCompare(val result: CompareResultUiModel) : CompressEvent()
    data class ShowError(val message: String) : CompressEvent()
    data object ShowPremiumPaywall : CompressEvent()
}
