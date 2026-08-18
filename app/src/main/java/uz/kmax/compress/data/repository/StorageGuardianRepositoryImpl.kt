package uz.kmax.compress.data.repository

import uz.kmax.compress.core.guardian.StorageAnalyzer
import uz.kmax.compress.domain.model.CategoryType
import uz.kmax.compress.domain.model.StorageGuardianResult
import uz.kmax.compress.domain.model.StorageImage
import uz.kmax.compress.domain.repository.StorageGuardianRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageGuardianRepositoryImpl @Inject constructor(
    private val storageAnalyzer: StorageAnalyzer
) : StorageGuardianRepository {

    private var lastResult: StorageGuardianResult? = null

    override suspend fun analyzeStorage(): StorageGuardianResult {
        return storageAnalyzer.analyze().also { 
            lastResult = it
        }
    }

    override suspend fun getImagesByCategory(type: CategoryType): List<StorageImage> {
        return lastResult?.categories?.find { it.type == type }?.images ?: emptyList()
    }
}
