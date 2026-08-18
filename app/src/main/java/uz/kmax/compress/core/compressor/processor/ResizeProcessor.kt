package uz.kmax.compress.core.compressor.processor

import android.graphics.Bitmap

interface ResizeProcessor {
    /**
     * Resizes the bitmap based on the provided [ResizeOptions].
     */
    suspend fun process(bitmap: Bitmap, options: ResizeOptions): ResizeResult
}
