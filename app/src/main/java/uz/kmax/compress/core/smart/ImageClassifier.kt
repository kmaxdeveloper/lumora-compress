package uz.kmax.compress.core.smart

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageClassifier @Inject constructor() {

    /**
     * Classifies the image based on its metrics.
     */
    fun classify(metrics: SmartCompressionAnalyzer.ImageMetrics): ImageClassification {
        // Rule-based classification
        
        // 1. Check for transparency first
        if (metrics.hasTransparency) {
            return ImageClassification.TRANSPARENT_IMAGE
        }

        // 2. Check for Logo/Icon (Small size, few colors, often sharp)
        if (metrics.megapixels < 0.5f && metrics.colorCount < 5000) {
            return if (metrics.aspectRatio in 0.8f..1.2f) ImageClassification.ICON else ImageClassification.LOGO
        }

        // 3. Check for Screenshot (often PNG, specific aspect ratios, high sharpness in text areas)
        if (metrics.extension.lowercase() == "png" || metrics.mimeType.contains("png")) {
            // Screenshots are typically portrait phone aspect ratio or landscape tablet/pc
            val isPhoneAspectRatio = metrics.aspectRatio in 0.4f..0.6f || metrics.aspectRatio in 1.6f..2.5f
            if (isPhoneAspectRatio && metrics.sharpness > 0.6f) {
                return ImageClassification.SCREENSHOT
            }
        }

        // 4. Check for Document (often high entropy if text-heavy, high sharpness, specific color distribution)
        if (metrics.entropy > 6.5f && metrics.sharpness > 0.7f && metrics.colorCount < 50000) {
            return ImageClassification.DOCUMENT
        }

        // 5. Check for Illustration/Anime (High texture complexity but lower noise/color count than photos)
        if (metrics.textureComplexity > 0.5f && metrics.colorCount < 100000 && metrics.noiseLevel < 0.1f) {
            return ImageClassification.ANIME
        }

        // 6. Landscape vs Portrait vs Selfie (Based on aspect ratio and megapixels)
        if (metrics.megapixels > 2.0f) {
            return if (metrics.aspectRatio > 1.2f) {
                ImageClassification.LANDSCAPE
            } else if (metrics.aspectRatio < 0.8f) {
                // Could be portrait or selfie. If high resolution and likely main camera -> PORTRAIT
                if (metrics.megapixels > 8.0f) ImageClassification.PORTRAIT else ImageClassification.SELFIE
            } else {
                ImageClassification.PHOTO
            }
        }

        // 7. Social Media (Medium resolution, likely already somewhat compressed)
        if (metrics.fileSize < 500 * 1024 && metrics.megapixels in 0.5f..2.0f) {
            return ImageClassification.SOCIAL_MEDIA
        }

        // Default
        return ImageClassification.PHOTO
    }
}
