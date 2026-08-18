package uz.kmax.compress.domain.usecase

import kotlinx.coroutines.flow.Flow
import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject

class GetRecentFilesUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(limit: Int = 5): Flow<List<CompressionHistory>> {
        return repository.getRecentHistory(limit)
    }
}
