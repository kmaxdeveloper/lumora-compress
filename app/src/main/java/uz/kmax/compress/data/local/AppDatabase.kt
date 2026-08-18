package uz.kmax.compress.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.kmax.compress.data.local.dao.HistoryDao
import uz.kmax.compress.data.local.entity.CompressionHistoryEntity

@Database(
    entities = [CompressionHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
