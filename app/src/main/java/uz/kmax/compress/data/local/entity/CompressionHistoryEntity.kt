package uz.kmax.compress.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compression_history")
data class CompressionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalUri: String,
    val compressedUri: String,
    val originalSize: Long,
    val compressedSize: Long,
    val savedBytes: Long,
    val savedPercent: Float,
    val format: String,
    val resolution: String,
    val createdAt: Long = System.currentTimeMillis(),
    val metadataMode: String,
    val quality: Int,
    val favorite: Boolean = false
)
