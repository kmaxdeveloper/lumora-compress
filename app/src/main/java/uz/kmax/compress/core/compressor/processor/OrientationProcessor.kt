package uz.kmax.compress.core.compressor.processor

import android.graphics.Bitmap
import uz.kmax.compress.core.compressor.decoder.DecodedBitmap

interface OrientationProcessor {
    /**
     * Corrects the orientation of the bitmap based on its rotation and metadata.
     */
    suspend fun process(decodedBitmap: DecodedBitmap): Bitmap
}
