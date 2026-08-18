package uz.kmax.compress.core.compressor

data class CompressionStatistics(
    val compressionRatio: Float,
    val processingTimeMs: Long,
    val originalResolution: Pair<Int, Int>,
    val outputResolution: Pair<Int, Int>
)
