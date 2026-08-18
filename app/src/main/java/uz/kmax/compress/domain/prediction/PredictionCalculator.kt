package uz.kmax.compress.domain.prediction

import uz.kmax.compress.core.smart.SmartCompressionDecision
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import javax.inject.Inject

class PredictionCalculator @Inject constructor() {
    fun calculate(image: GalleryImageUiModel, decision: SmartCompressionDecision): PredictionData {
        val original = image.size.toByteCount()
        val scale = decision.resizeFactor ?: 1f
        val width = (image.width * scale).toInt()
        val height = (image.height * scale).toInt()
        val seconds = ((image.width.toLong() * image.height / 1_000_000f) * if (decision.format.name.startsWith("WEBP")) .10f else .13f).coerceIn(.3f, 4f)
        return PredictionData(original, decision.estimatedOutputSize, decision.estimatedSavingPercent, decision.estimatedQualityScore, decision.format.name, width, height, seconds, decision.classification.name, decision.reason)
    }
    private fun String.toByteCount(): Long { val n = substringBefore(' ').replace(',', '.').toFloatOrNull() ?: 0f; return when (substringAfter(' ', "B").uppercase()) { "MB" -> (n * 1024 * 1024).toLong(); "KB" -> (n * 1024).toLong(); else -> n.toLong() } }
}
data class PredictionData(val originalSize: Long, val estimatedSize: Long, val savingPercent: Float, val quality: Int, val format: String, val width: Int, val height: Int, val seconds: Float, val imageType: String, val reason: String)
