package uz.kmax.compress.core.compressor.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class ResizeProcessorImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ResizeProcessor {

    override suspend fun process(bitmap: Bitmap, options: ResizeOptions): ResizeResult = withContext(ioDispatcher) {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        val (targetWidth, targetHeight) = calculateTargetSize(originalWidth, originalHeight, options)

        if (targetWidth == originalWidth && targetHeight == originalHeight) {
            return@withContext ResizeResult(
                bitmap = bitmap,
                oldResolution = originalWidth to originalHeight,
                newResolution = targetWidth to targetHeight,
                scaleFactor = 1.0f
            )
        }

        val scaledBitmap = when (options.strategy) {
            ResizeStrategy.STRETCH -> {
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            }
            ResizeStrategy.CENTER_CROP -> {
                centerCrop(bitmap, targetWidth, targetHeight)
            }
            else -> {
                // FIT, FILL, and KEEP_ASPECT_RATIO use standard proportional scaling
                Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            }
        }

        ResizeResult(
            bitmap = scaledBitmap,
            oldResolution = originalWidth to originalHeight,
            newResolution = targetWidth to targetHeight,
            scaleFactor = targetWidth.toFloat() / originalWidth.toFloat()
        )
    }

    private fun calculateTargetSize(width: Int, height: Int, options: ResizeOptions): Pair<Int, Int> {
        val reqWidth = options.width ?: width
        val reqHeight = options.height ?: height

        var targetWidth = reqWidth
        var targetHeight = reqHeight

        when (options.strategy) {
            ResizeStrategy.FIT, ResizeStrategy.KEEP_ASPECT_RATIO -> {
                val ratio = width.toFloat() / height.toFloat()
                if (reqWidth.toFloat() / reqHeight.toFloat() > ratio) {
                    targetWidth = (reqHeight * ratio).toInt()
                } else {
                    targetHeight = (reqWidth / ratio).toInt()
                }
            }
            ResizeStrategy.FILL -> {
                val ratio = width.toFloat() / height.toFloat()
                if (reqWidth.toFloat() / reqHeight.toFloat() < ratio) {
                    targetWidth = (reqHeight * ratio).toInt()
                } else {
                    targetHeight = (reqWidth / ratio).toInt()
                }
            }
            ResizeStrategy.CENTER_CROP -> {
                // Target size is exactly reqWidth x reqHeight
            }
            ResizeStrategy.STRETCH -> {
                // Target size is exactly reqWidth x reqHeight
            }
        }

        // Apply upscale restriction
        if (!options.allowUpscale) {
            if (targetWidth > width || targetHeight > height) {
                val scale = min(width.toFloat() / targetWidth, height.toFloat() / targetHeight)
                targetWidth = (targetWidth * scale).toInt()
                targetHeight = (targetHeight * scale).toInt()
            }
        }

        // Apply max resolution restriction (e.g., 4K max)
        options.maxResolution?.let { maxRes ->
            if (targetWidth > maxRes || targetHeight > maxRes) {
                val scale = min(maxRes.toFloat() / targetWidth, maxRes.toFloat() / targetHeight)
                targetWidth = (targetWidth * scale).toInt()
                targetHeight = (targetHeight * scale).toInt()
            }
        }

        return targetWidth to targetHeight
    }

    private fun centerCrop(src: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val srcWidth = src.width
        val srcHeight = src.height
        
        val scale: Float
        val dx: Float
        val dy: Float

        if (srcWidth * targetHeight > targetWidth * srcHeight) {
            scale = targetHeight.toFloat() / srcHeight.toFloat()
            dx = (targetWidth - srcWidth * scale) * 0.5f
            dy = 0f
        } else {
            scale = targetWidth.toFloat() / srcWidth.toFloat()
            dx = 0f
            dy = (targetHeight - srcHeight * scale) * 0.5f
        }

        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }

        val result = Bitmap.createBitmap(targetWidth, targetHeight, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(src, matrix, paint)
        return result
    }
}
