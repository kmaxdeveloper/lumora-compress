package uz.kmax.compress.core.compressor.algorithm

import android.graphics.Bitmap
import uz.kmax.compress.core.compressor.CompressionRequest

interface CompressionAlgorithm {
    /**
     * Encodes the provided [bitmap] based on the [request].
     *
     * @param bitmap The bitmap to encode.
     * @param request The compression configuration.
     * @return [CompressedImageData] containing the encoded bytes and format info.
     * @throws uz.kmax.compress.core.compressor.CompressionException if encoding fails.
     */
    suspend fun compress(bitmap: Bitmap, request: CompressionRequest): CompressedImageData
}
