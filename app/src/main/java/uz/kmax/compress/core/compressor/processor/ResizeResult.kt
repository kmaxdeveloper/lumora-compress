package uz.kmax.compress.core.compressor.processor

import android.graphics.Bitmap

data class ResizeResult(
    val bitmap: Bitmap,
    val oldResolution: Pair<Int, Int>,
    val newResolution: Pair<Int, Int>,
    val scaleFactor: Float
)
