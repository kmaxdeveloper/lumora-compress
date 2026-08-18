package uz.kmax.compress.core.smart

import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionQuality
import uz.kmax.compress.core.compressor.metadata.MetadataOptions

/**
 * Data class representing the decisions made by the Smart Compression Engine.
 */
data class SmartCompressionDecision(
    val format: CompressionFormat,
    val quality: CompressionQuality,
    val resizeFactor: Float?,
    val metadataStrategy: MetadataOptions.Strategy,
    val classification: ImageClassification,
    val estimatedOutputSize: Long,
    val estimatedSavingPercent: Float,
    val estimatedQualityScore: Int, // 0-100
    val reason: String
)

enum class ImageClassification {
    PHOTO,
    PORTRAIT,
    SELFIE,
    LANDSCAPE,
    DOCUMENT,
    SCREENSHOT,
    SOCIAL_MEDIA,
    ANIME,
    ILLUSTRATION,
    ICON,
    LOGO,
    TRANSPARENT_IMAGE,
    OPTIMIZED // For images already well compressed
}
