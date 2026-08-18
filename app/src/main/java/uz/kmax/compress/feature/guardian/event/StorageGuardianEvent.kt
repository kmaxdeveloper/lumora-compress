package uz.kmax.compress.feature.guardian.event

import uz.kmax.compress.domain.model.CategoryType

sealed class StorageGuardianEvent {
    data object ScanStarted : StorageGuardianEvent()
    data class NavigateToBatch(val category: CategoryType) : StorageGuardianEvent()
    data object NavigateToGallery : StorageGuardianEvent()
    data class ShowError(val message: String) : StorageGuardianEvent()
}
