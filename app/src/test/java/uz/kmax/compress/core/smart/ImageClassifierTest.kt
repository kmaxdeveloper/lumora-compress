package uz.kmax.compress.core.smart

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageClassifierTest {

    private val classifier = ImageClassifier()

    @Test
    fun `classify transparent image`() {
        val metrics = createMockMetrics(hasTransparency = true)
        val classification = classifier.classify(metrics)
        assertEquals(ImageClassification.TRANSPARENT_IMAGE, classification)
    }

    @Test
    fun `classify logo`() {
        val metrics = createMockMetrics(megapixels = 0.2f, colorCount = 2000, aspectRatio = 2.0f)
        val classification = classifier.classify(metrics)
        assertEquals(ImageClassification.LOGO, classification)
    }

    @Test
    fun `classify screenshot`() {
        val metrics = createMockMetrics(extension = "png", aspectRatio = 0.5f, sharpness = 0.8f)
        val classification = classifier.classify(metrics)
        assertEquals(ImageClassification.SCREENSHOT, classification)
    }

    private fun createMockMetrics(
        width: Int = 4000,
        height: Int = 3000,
        aspectRatio: Float = 1.33f,
        fileSize: Long = 5_000_000L,
        extension: String = "jpg",
        mimeType: String = "image/jpeg",
        hasTransparency: Boolean = false,
        orientation: Int = 1,
        megapixels: Float = 12f,
        entropy: Float = 5.0f,
        textureComplexity: Float = 0.5f,
        noiseLevel: Float = 0.05f,
        sharpness: Float = 0.5f,
        colorCount: Int = 100_000
    ) = SmartCompressionAnalyzer.ImageMetrics(
        width, height, aspectRatio, fileSize, extension, mimeType, hasTransparency,
        orientation, megapixels, entropy, textureComplexity, noiseLevel, sharpness, colorCount
    )
}
