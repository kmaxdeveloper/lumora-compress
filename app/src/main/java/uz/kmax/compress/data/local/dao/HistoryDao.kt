package uz.kmax.compress.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.kmax.compress.data.local.entity.CompressionHistoryEntity

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CompressionHistoryEntity): Long

    @Delete
    suspend fun delete(entity: CompressionHistoryEntity)

    @Query("DELETE FROM compression_history WHERE id IN (:ids)")
    suspend fun deleteMultiple(ids: List<Long>)

    @Query("DELETE FROM compression_history")
    suspend fun deleteAll()

    @Query("UPDATE compression_history SET favorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("""
        SELECT * FROM compression_history 
        WHERE format LIKE '%' || :query || '%' OR resolution LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN :sortOrder = 'NEWEST' THEN createdAt END DESC,
            CASE WHEN :sortOrder = 'OLDEST' THEN createdAt END ASC,
            CASE WHEN :sortOrder = 'SAVINGS' THEN savedBytes END DESC
    """)
    fun getHistory(query: String, sortOrder: String): PagingSource<Int, CompressionHistoryEntity>

    @Query("SELECT * FROM compression_history WHERE favorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): PagingSource<Int, CompressionHistoryEntity>

    @Query("SELECT * FROM compression_history WHERE id = :id")
    suspend fun getById(id: Long): CompressionHistoryEntity?

    @Query("SELECT SUM(savedBytes) FROM compression_history")
    fun getTotalSavedBytes(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM compression_history")
    fun getTotalFilesCount(): Flow<Int>

    @Query("SELECT AVG(savedPercent) FROM compression_history")
    fun getAverageCompression(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM compression_history WHERE favorite = 1")
    fun getFavoriteCount(): Flow<Int>

    @Query("SELECT MAX(savedBytes) FROM compression_history")
    fun getLargestSaving(): Flow<Long?>
    @Query("SELECT * FROM compression_history ORDER BY createdAt DESC")
    suspend fun getAllHistory(): List<CompressionHistoryEntity>

    @Query("SELECT * FROM compression_history ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<CompressionHistoryEntity>>
}
