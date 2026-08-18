package uz.kmax.compress.core.compressor

import android.net.Uri

sealed interface CompressionResult {
    val originalSize: Long
    val elapsedTime: Long

    data class Success(
        override val originalSize: Long,
        override val elapsedTime: Long,
        val compressedSize: Long,
        val savedBytes: Long,
        val savedPercent: Float,
        val outputUri: Uri,
        val statistics: CompressionStatistics,
        val warnings: List<String> = emptyList()
    ) : CompressionResult

    data class Failure(
        override val originalSize: Long,
        override val elapsedTime: Long,
        val exception: CompressionException
    ) : CompressionResult
}
