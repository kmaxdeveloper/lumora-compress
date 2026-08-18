package uz.kmax.compress.core.smart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageTransparencyAnalyzer @Inject constructor() {

    /**
     * Checks if the image at the given Uri has transparency.
     */
    suspend fun hasTransparency(context: Context, uri: Uri): Boolean = withContext(Dispatchers.Default) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options)
            }
            
            // Fast check: if it's not a format that supports transparency, return false
            val mimeType = options.outMimeType?.lowercase() ?: ""
            if (mimeType != "image/png" && mimeType != "image/webp" && mimeType != "image/gif") {
                return@withContext false
            }

            // Deep check: analyze bitmap pixels
            val decodeOptions = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = 4 // Subsample for performance
            }
            
            val bitmap = context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext false

            try {
                if (!bitmap.hasAlpha()) return@withContext false
                
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                
                for (pixel in pixels) {
                    if ((pixel shr 24) and 0xFF < 255) {
                        return@withContext true
                    }
                }
                false
            } finally {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            false
        }
    }
}
