package uz.kmax.compress.core.compressor.decoder

import android.net.Uri

interface BitmapDecoder {

    /**
     * Decodes a bitmap from the given [Uri] respecting the provided [DecodeOptions].
     *
     * @param uri The source image URI.
     * @param options Configuration for the decoding process.
     * @return [DecodedBitmap] containing the bitmap and its metadata.
     * @throws DecodeException if decoding fails.
     */
    suspend fun decode(uri: Uri, options: DecodeOptions = DecodeOptions()): DecodedBitmap

    /**
     * Reads only the dimensions and metadata of the image without loading the full bitmap into memory.
     *
     * @param uri The source image URI.
     * @return Basic info about the image.
     * @throws DecodeException if reading fails.
     */
    suspend fun getInfo(uri: Uri): ImageInfo

    data class ImageInfo(
        val width: Int,
        val height: Int,
        val mimeType: String?,
        val rotation: Int
    )
}
