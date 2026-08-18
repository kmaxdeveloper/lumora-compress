package uz.kmax.compress.core.smart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log2

@Singleton
class ImageEntropyAnalyzer @Inject constructor() {

    /**
     * Estimates the entropy of the image. 
     * Higher values indicate more complex textures or noise.
     * Scale: 0.0 to 8.0 (for 8-bit channels)
     */
    suspend fun estimateEntropy(context: Context, uri: Uri): Float = withContext(Dispatchers.Default) {
        try {
            val decodeOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = 8 // Subsample significantly for entropy estimation
            }
            
            val bitmap = context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext 0f

            try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                
                // Calculate histogram for the luminance (Y) channel
                val histogram = LongArray(256)
                for (pixel in pixels) {
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    // Simple luminance approximation
                    val y = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
                    histogram[y]++
                }

                var entropy = 0.0
                val totalPixels = pixels.size.toDouble()
                
                for (count in histogram) {
                    if (count > 0) {
                        val probability = count / totalPixels
                        entropy -= probability * log2(probability)
                    }
                }
                
                entropy.toFloat()
            } finally {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            0f
        }
    }
}
