package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.model.CategoryType
import uz.kmax.compress.domain.model.StorageImage
import uz.kmax.compress.domain.repository.StorageGuardianRepository
import javax.inject.Inject

class GetImagesByCategoryUseCase @Inject constructor(
    private val repository: StorageGuardianRepository
) {
    suspend operator fun invoke(type: CategoryType): List<StorageImage> {
        return repository.getImagesByCategory(type)
    }
}
