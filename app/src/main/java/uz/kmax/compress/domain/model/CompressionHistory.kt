package uz.kmax.compress.domain.model

data class CompressionHistory(
    val id: Long = 0,
    val originalUri: String,
    val compressedUri: String,
    val originalSize: Long,
    val compressedSize: Long,
    val savedBytes: Long,
    val savedPercent: Float,
    val format: String,
    val resolution: String,
    val createdAt: Long,
    val metadataMode: String,
    val quality: Int,
    val favorite: Boolean
)
