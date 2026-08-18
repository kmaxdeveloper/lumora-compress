package uz.kmax.compress.domain.usecase

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(query: String = "", sortOrder: String = "NEWEST"): Flow<PagingData<CompressionHistory>> {
        return repository.getHistory(query, sortOrder)
    }
}
