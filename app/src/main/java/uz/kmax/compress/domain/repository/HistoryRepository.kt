package uz.kmax.compress.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import uz.kmax.compress.domain.model.CompressionHistory

interface HistoryRepository {
    fun getHistory(query: String, sortOrder: String): Flow<PagingData<CompressionHistory>>
    fun getFavorites(): Flow<PagingData<CompressionHistory>>
    suspend fun insert(history: CompressionHistory): Long
    suspend fun delete(history: CompressionHistory)
    suspend fun deleteMultiple(ids: List<Long>)
    suspend fun deleteAll()
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun getAllHistory(): List<CompressionHistory>
    fun getRecentHistory(limit: Int): Flow<List<CompressionHistory>>

    fun getTotalSavedBytes(): Flow<Long>
    fun getTotalFilesCount(): Flow<Int>
    fun getAverageCompression(): Flow<Float>
    fun getFavoriteCount(): Flow<Int>
    fun getLargestSaving(): Flow<Long>
}
