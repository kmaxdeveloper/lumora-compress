package uz.kmax.compress.core.smart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.metadata.MetadataOptions

class SmartCompressionRulesTest {

    private val rules = SmartCompressionRules()

    @Test
    fun `decide for portrait should use high quality jpeg`() {
        val metrics = createMockMetrics(megapixels = 12f, aspectRatio = 0.7f)
        val decision = rules.decide(metrics, ImageClassification.PORTRAIT)

        assertEquals(CompressionFormat.JPEG, decision.format)
        assertEquals(88, decision.quality)
        assertEquals(0.85f, decision.resizeFactor)
    }

    @Test
    fun `decide for screenshot should use lossless png`() {
        val metrics = createMockMetrics(extension = "png", sharpness = 0.8f)
        val decision = rules.decide(metrics, ImageClassification.SCREENSHOT)

        assertEquals(CompressionFormat.PNG, decision.format)
        assertEquals(100, decision.quality)
        assertEquals(1.0f, decision.resizeFactor)
    }

    @Test
    fun `decide for document should use high quality png`() {
        val metrics = createMockMetrics(entropy = 7.5f, sharpness = 0.9f)
        val decision = rules.decide(metrics, ImageClassification.DOCUMENT)

        assertEquals(CompressionFormat.PNG, decision.format)
        assertEquals(95, decision.quality)
    }

    @Test
    fun `decide for landscape should use webp with good quality`() {
        val metrics = createMockMetrics(megapixels = 20f, aspectRatio = 1.5f)
        val decision = rules.decide(metrics, ImageClassification.LANDSCAPE)

        // On older Android it might be JPEG, but let's assume default in test is WEBP_LOSSY or JPEG
        assertTrue(decision.format == CompressionFormat.WEBP_LOSSY || decision.format == CompressionFormat.JPEG || decision.format == CompressionFormat.AVIF)
        assertEquals(82, decision.quality)
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
