package uz.kmax.compress.core.smart

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartCompressionAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transparencyAnalyzer: ImageTransparencyAnalyzer,
    private val entropyAnalyzer: ImageEntropyAnalyzer,
    private val complexityAnalyzer: ImageComplexityAnalyzer
) {

    data class ImageMetrics(
        val width: Int,
        val height: Int,
        val aspectRatio: Float,
        val fileSize: Long,
        val extension: String,
        val mimeType: String,
        val hasTransparency: Boolean,
        val orientation: Int,
        val megapixels: Float,
        val entropy: Float,
        val textureComplexity: Float,
        val noiseLevel: Float,
        val sharpness: Float,
        val colorCount: Int
    )

    suspend fun analyze(uri: Uri): ImageMetrics = coroutineScope {
        val basicInfoDeferred = async { getBasicInfo(uri) }
        val transparencyDeferred = async { transparencyAnalyzer.hasTransparency(context, uri) }
        val entropyDeferred = async { entropyAnalyzer.estimateEntropy(context, uri) }
        val complexityDeferred = async { complexityAnalyzer.analyzeComplexity(context, uri) }

        val basicInfo = basicInfoDeferred.await()
        val complexity = complexityDeferred.await()

        ImageMetrics(
            width = basicInfo.width,
            height = basicInfo.height,
            aspectRatio = if (basicInfo.height > 0) basicInfo.width.toFloat() / basicInfo.height else 1f,
            fileSize = basicInfo.fileSize,
            extension = basicInfo.extension,
            mimeType = basicInfo.mimeType,
            hasTransparency = transparencyDeferred.await(),
            orientation = basicInfo.orientation,
            megapixels = (basicInfo.width * basicInfo.height) / 1_000_000f,
            entropy = entropyDeferred.await(),
            textureComplexity = complexity.textureComplexity,
            noiseLevel = complexity.noiseLevel,
            sharpness = complexity.sharpness,
            colorCount = complexity.colorCountEstimation
        )
    }

    private fun getBasicInfo(uri: Uri): BasicInfo {
        var width = 0
        var height = 0
        var mimeType = ""
        var orientation = ExifInterface.ORIENTATION_NORMAL
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(input, null, options)
            width = options.outWidth
            height = options.outHeight
            mimeType = options.outMimeType ?: ""
        }

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                // Swap width/height if rotated 90 or 270 degrees
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    val temp = width
                    width = height
                    height = temp
                }
            }
        } catch (e: Exception) {
            // Ignore EXIF errors
        }

        val fileSize = getFileSize(uri)
        val extension = getExtensionFromMime(mimeType, uri)

        return BasicInfo(width, height, mimeType, orientation, fileSize, extension)
    }

    private fun getFileSize(uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                cursor.getLong(sizeIndex)
            } else 0L
        } ?: 0L
    }

    private fun getExtensionFromMime(mimeType: String, uri: Uri): String {
        return mimeType.substringAfter("/", "").ifEmpty {
            uri.path?.substringAfterLast(".", "") ?: ""
        }
    }

    private data class BasicInfo(
        val width: Int,
        val height: Int,
        val mimeType: String,
        val orientation: Int,
        val fileSize: Long,
        val extension: String
    )
}
