package uz.kmax.compress.core.compressor.decoder

import android.graphics.Bitmap

data class DecodedBitmap(
    val bitmap: Bitmap,
    val width: Int,
    val height: Int,
    val mimeType: String?,
    val rotation: Int = 0,
    val hasAlpha: Boolean = true
)
