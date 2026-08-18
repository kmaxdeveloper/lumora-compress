package uz.kmax.compress.core.smart

import uz.kmax.compress.core.compressor.CompressionFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartCompressionPrediction @Inject constructor() {

    /**
     * Estimates the output size of the compression.
     */
    fun estimate(
        metrics: SmartCompressionAnalyzer.ImageMetrics,
        format: CompressionFormat,
        quality: Int,
        resizeFactor: Float
    ): PredictionResult {
        
        val originalSize = metrics.fileSize
        val pixelCount = (metrics.width * resizeFactor) * (metrics.height * resizeFactor)
        
        // Bits per pixel estimation based on format and quality
        val bpp = when (format) {
            CompressionFormat.JPEG -> {
                when {
                    quality >= 95 -> 2.5f
                    quality >= 85 -> 1.5f
                    quality >= 70 -> 1.0f
                    else -> 0.6f
                }
            }
            CompressionFormat.PNG -> {
                // PNG is lossless, depends heavily on complexity
                if (metrics.colorCount < 256) 0.5f else 4.0f
            }
            CompressionFormat.WEBP_LOSSY -> {
                when {
                    quality >= 95 -> 1.8f
                    quality >= 85 -> 1.0f
                    quality >= 70 -> 0.7f
                    else -> 0.4f
                }
            }
            CompressionFormat.WEBP_LOSSLESS -> 3.0f
            CompressionFormat.AVIF -> {
                when {
                    quality >= 95 -> 1.2f
                    quality >= 85 -> 0.7f
                    quality >= 70 -> 0.4f
                    else -> 0.2f
                }
            }
            else -> 1.0f
        }

        // Adjust BPP based on entropy and texture complexity
        val complexityMultiplier = 0.5f + (metrics.entropy / 8.0f) * 0.5f + metrics.textureComplexity * 0.5f
        
        val estimatedSize = (pixelCount * bpp * complexityMultiplier / 8f).toLong()
        
        val finalEstimate = estimatedSize

        val savingPercent = if (originalSize > 0) {
            ((originalSize - finalEstimate).toFloat() / originalSize.toFloat() * 100f).coerceAtMost(99f)
        } else 0f

        val qualityScore = calculateQualityScore(quality, format, resizeFactor)

        return PredictionResult(
            estimatedSize = finalEstimate,
            savingPercent = savingPercent,
            qualityScore = qualityScore
        )
    }

    private fun calculateQualityScore(quality: Int, format: CompressionFormat, resizeFactor: Float): Int {
        var score = quality.toFloat()
        
        // Deduct for resizing
        if (resizeFactor < 1.0f) {
            score -= (1.0f - resizeFactor) * 20f
        }
        
        // Boost for more efficient formats at same quality
        if (format == CompressionFormat.AVIF) score += 5
        if (format == CompressionFormat.WEBP_LOSSY) score += 2
        
        return score.toInt().coerceIn(1, 100)
    }

    data class PredictionResult(
        val estimatedSize: Long,
        val savingPercent: Float,
        val qualityScore: Int
    )
}
