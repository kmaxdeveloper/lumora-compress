package uz.kmax.compress.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.kmax.compress.data.local.dao.HistoryDao
import uz.kmax.compress.data.mapper.toDomain
import uz.kmax.compress.data.mapper.toEntity
import uz.kmax.compress.domain.model.CompressionHistory
import uz.kmax.compress.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getHistory(query: String, sortOrder: String): Flow<PagingData<CompressionHistory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { historyDao.getHistory(query, sortOrder) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getFavorites(): Flow<PagingData<CompressionHistory>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { historyDao.getFavorites() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun insert(history: CompressionHistory): Long {
        return historyDao.insert(history.toEntity())
    }

    override suspend fun delete(history: CompressionHistory) {
        historyDao.delete(history.toEntity())
    }

    override suspend fun deleteMultiple(ids: List<Long>) {
        historyDao.deleteMultiple(ids)
    }

    override suspend fun deleteAll() {
        historyDao.deleteAll()
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        historyDao.toggleFavorite(id, isFavorite)
    }

    override suspend fun getAllHistory(): List<CompressionHistory> {
        return historyDao.getAllHistory().map { it.toDomain() }
    }

    override fun getRecentHistory(limit: Int): Flow<List<CompressionHistory>> {
        return historyDao.getRecentHistory(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTotalSavedBytes(): Flow<Long> = historyDao.getTotalSavedBytes().map { it ?: 0L }

    override fun getTotalFilesCount(): Flow<Int> = historyDao.getTotalFilesCount()

    override fun getAverageCompression(): Flow<Float> = historyDao.getAverageCompression().map { it ?: 0f }

    override fun getFavoriteCount(): Flow<Int> = historyDao.getFavoriteCount()

    override fun getLargestSaving(): Flow<Long> = historyDao.getLargestSaving().map { it ?: 0L }
}
