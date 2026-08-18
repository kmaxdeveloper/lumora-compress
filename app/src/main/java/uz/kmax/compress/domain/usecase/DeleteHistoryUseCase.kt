package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject

class DeleteHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(history: CompressionHistory) = repository.delete(history)
    suspend operator fun invoke(ids: List<Long>) = repository.deleteMultiple(ids)
    suspend fun deleteAll() = repository.deleteAll()
}
