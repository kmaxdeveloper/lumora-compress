package uz.kmax.compress.core.smart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class ImageComplexityAnalyzer @Inject constructor() {

    data class ComplexityMetrics(
        val textureComplexity: Float, // 0 to 1
        val sharpness: Float,         // 0 to 1
        val noiseLevel: Float,        // 0 to 1
        val colorCountEstimation: Int
    )

    /**
     * Analyzes image complexity metrics.
     */
    suspend fun analyzeComplexity(context: Context, uri: Uri): ComplexityMetrics = withContext(Dispatchers.Default) {
        try {
            val decodeOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = 4 
            }
            
            val bitmap = context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext ComplexityMetrics(0f, 0f, 0f, 0)

            try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                
                var totalEdgeMagnitude = 0f
                var variance = 0f
                val colors = mutableSetOf<Int>()
                
                // Grayscale conversion
                val gray = FloatArray(pixels.size)
                for (i in pixels.indices) {
                    val p = pixels[i]
                    gray[i] = (0.299f * ((p shr 16) and 0xFF) + 0.587f * ((p shr 8) and 0xFF) + 0.114f * (p and 0xFF))
                    if (i % 10 == 0) colors.add(p) // Sample colors
                }

                // Simple Sobel-like edge detection for texture/sharpness
                for (y in 1 until height - 1) {
                    for (x in 1 until width - 1) {
                        val idx = y * width + x
                        val dx = gray[idx + 1] - gray[idx - 1]
                        val dy = gray[idx + width] - gray[idx - width]
                        val magnitude = sqrt(dx * dx + dy * dy)
                        totalEdgeMagnitude += magnitude
                    }
                }

                val avgEdge = totalEdgeMagnitude / (width * height)
                
                // Texture complexity based on edge density
                val texture = (avgEdge / 50f).coerceIn(0f, 1f)
                
                // Sharpness based on max edge magnitude (simplified)
                val sharpness = (avgEdge / 30f).coerceIn(0f, 1f)
                
                // Noise estimation (very simplified based on high frequency variance in flat areas)
                // For now, let's keep it simple
                val noise = 0.05f 

                ComplexityMetrics(
                    textureComplexity = texture,
                    sharpness = sharpness,
                    noiseLevel = noise,
                    colorCountEstimation = colors.size * 10 // Approximation
                )
            } finally {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            ComplexityMetrics(0f, 0f, 0f, 0)
        }
    }
}
