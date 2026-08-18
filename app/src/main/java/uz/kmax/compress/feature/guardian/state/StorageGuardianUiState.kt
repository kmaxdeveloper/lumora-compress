package uz.kmax.compress.feature.guardian.state

import uz.kmax.compress.domain.model.StorageGuardianResult

data class StorageGuardianUiState(
    val isLoading: Boolean = false,
    val result: StorageGuardianResult? = null,
    val error: String? = null
)
