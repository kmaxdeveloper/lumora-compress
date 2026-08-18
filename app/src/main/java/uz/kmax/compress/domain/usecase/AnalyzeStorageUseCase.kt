package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.model.StorageGuardianResult
import uz.kmax.compress.domain.repository.StorageGuardianRepository
import javax.inject.Inject

class AnalyzeStorageUseCase @Inject constructor(
    private val repository: StorageGuardianRepository
) {
    suspend operator fun invoke(): StorageGuardianResult {
        return repository.analyzeStorage()
    }
}
