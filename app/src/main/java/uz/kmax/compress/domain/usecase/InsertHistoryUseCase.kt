package uz.kmax.compress.domain.usecase

import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject

class InsertHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(history: CompressionHistory) = repository.insert(history)
}
