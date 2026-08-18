package uz.kmax.compress.core.smart

import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionQuality
import uz.kmax.compress.core.compressor.metadata.MetadataOptions
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements the rule-based decision logic for the Smart Compression Engine.
 * It applies specific heuristics based on image classification, resolution, and complexity.
 */
@Singleton
class SmartCompressionRules @Inject constructor() {

    /**
     * Decides the optimal compression settings for an image based on its metrics and classification.
     */
    fun decide(
        metrics: SmartCompressionAnalyzer.ImageMetrics,
        classification: ImageClassification
    ): DecisionResult {
        
        var format = CompressionFormat.WEBP_LOSSY
        var quality = 80
        var resizeFactor = 1.0f
        var metadataStrategy = MetadataOptions.Strategy.REMOVE_GPS
        var reason = "Optimized for $classification"

        // 1. Format Decision
        format = when (classification) {
            ImageClassification.TRANSPARENT_IMAGE -> CompressionFormat.PNG
            ImageClassification.SCREENSHOT -> CompressionFormat.PNG
            ImageClassification.DOCUMENT -> CompressionFormat.PNG
            ImageClassification.LOGO, ImageClassification.ICON -> CompressionFormat.PNG
            ImageClassification.ANIME, ImageClassification.ILLUSTRATION -> CompressionFormat.WEBP_LOSSY
            else -> CompressionFormat.JPEG
        }

        // 2. Resize Decision
        resizeFactor = when {
            classification in setOf(ImageClassification.SCREENSHOT, ImageClassification.DOCUMENT, ImageClassification.LOGO, ImageClassification.ICON) -> 1.0f
            metrics.megapixels > 24f -> 0.7f
            metrics.megapixels >= 12f -> 0.85f
            metrics.megapixels > 8f -> 0.95f
            metrics.megapixels < 1f -> 1.0f
            else -> 1.0f
        }

        // 3. Quality Decision
        quality = when (classification) {
            ImageClassification.PORTRAIT, ImageClassification.SELFIE -> 88
            ImageClassification.LANDSCAPE -> 82
            ImageClassification.DOCUMENT -> 95
            ImageClassification.SCREENSHOT -> 100
            ImageClassification.ANIME -> 85
            ImageClassification.PHOTO -> 80
            ImageClassification.SOCIAL_MEDIA -> 75
            ImageClassification.LOGO, ImageClassification.ICON -> 90
            else -> 80
        }

        // Adjust quality based on entropy (more entropy -> higher quality to avoid artifacts)
        if (metrics.entropy > 7.0f && classification in setOf(
                ImageClassification.PHOTO,
                ImageClassification.PORTRAIT,
                ImageClassification.SELFIE,
                ImageClassification.LANDSCAPE
            )
        ) {
            quality = (quality + 5).coerceAtMost(100)
        }

        // 4. Metadata Policy
        metadataStrategy = when (classification) {
            ImageClassification.PHOTO, ImageClassification.PORTRAIT, ImageClassification.LANDSCAPE -> MetadataOptions.Strategy.KEEP_ALL
            else -> MetadataOptions.Strategy.REMOVE_ALL
        }

        // 5. Check if already optimized
        val isSmallFile = metrics.fileSize < 200 * 1024
        val isLowRes = metrics.megapixels < 1.5f
        
        if (isSmallFile && isLowRes) {
            // For already small and low-res images, be very conservative
            // to prevent size increase (container overhead)
            return DecisionResult(
                format = if (metrics.extension.lowercase() == "png") CompressionFormat.PNG else CompressionFormat.WEBP_LOSSY,
                quality = 75,
                resizeFactor = 1.0f,
                metadataStrategy = MetadataOptions.Strategy.REMOVE_ALL,
                reason = "Image is already small. Using conservative settings to prevent size increase."
            )
        }

        return DecisionResult(
            format = format,
            quality = quality,
            resizeFactor = resizeFactor,
            metadataStrategy = metadataStrategy,
            reason = reason
        )
    }

    data class DecisionResult(
        val format: CompressionFormat,
        val quality: Int,
        val resizeFactor: Float,
        val metadataStrategy: MetadataOptions.Strategy,
        val reason: String
    )
}
