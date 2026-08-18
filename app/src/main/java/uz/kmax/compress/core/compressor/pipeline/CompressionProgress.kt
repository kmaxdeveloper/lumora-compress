package uz.kmax.compress.core.compressor.pipeline

import uz.kmax.compress.core.compressor.CompressionResult
import uz.kmax.compress.core.compressor.algorithm.CompressedImageData

data class CompressionProgress(
    val stage: CompressionStage,
    val progress: Float = 0f,
    val message: String? = null,
    val result: CompressionResult? = null,
    val imageData: CompressedImageData? = null
)
