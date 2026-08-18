package uz.kmax.compress.domain.repository

import uz.kmax.compress.domain.model.CategoryType
import uz.kmax.compress.domain.model.StorageGuardianResult
import uz.kmax.compress.domain.model.StorageImage

interface StorageGuardianRepository {
    suspend fun analyzeStorage(): StorageGuardianResult
    suspend fun getImagesByCategory(type: CategoryType): List<StorageImage>
}
