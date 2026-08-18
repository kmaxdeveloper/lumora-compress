package uz.kmax.compress.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(): Flow<CompressionStats> = combine(
        repository.getTotalSavedBytes(),
        repository.getTotalFilesCount(),
        repository.getAverageCompression(),
        repository.getFavoriteCount(),
        repository.getLargestSaving()
    ) { saved, count, avg, favs, largest ->
        CompressionStats(
            totalSavedBytes = saved,
            totalFilesCount = count,
            averageReduction = avg,
            favoriteCount = favs,
            largestSaving = largest
        )
    }
}

data class CompressionStats(
    val totalSavedBytes: Long,
    val totalFilesCount: Int,
    val averageReduction: Float,
    val favoriteCount: Int,
    val largestSaving: Long
)
