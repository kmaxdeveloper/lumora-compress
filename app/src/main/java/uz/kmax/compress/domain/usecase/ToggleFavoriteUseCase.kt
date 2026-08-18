package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(id: Long, isFavorite: Boolean) = repository.toggleFavorite(id, isFavorite)
}
