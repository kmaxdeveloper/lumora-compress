package uz.kmax.compress.core.compressor.algorithm

import android.graphics.Bitmap
import android.os.Build
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionRequest
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class CompressionAlgorithmFactory @Inject constructor(
    private val jpegAlgorithm: Provider<JpegCompressionAlgorithm>,
    private val pngAlgorithm: Provider<PngCompressionAlgorithm>,
    private val webpAlgorithm: Provider<WebpCompressionAlgorithm>,
    private val avifAlgorithm: Provider<AvifCompressionAlgorithm>
) {

    fun getAlgorithm(bitmap: Bitmap, request: CompressionRequest): CompressionAlgorithm {
        val targetFormat = if (request.format == CompressionFormat.AUTO) {
            determineAutoFormat(bitmap)
        } else {
            request.format
        }

        return when (targetFormat) {
            CompressionFormat.JPEG -> jpegAlgorithm.get()
            CompressionFormat.PNG -> pngAlgorithm.get()
            CompressionFormat.WEBP_LOSSY, CompressionFormat.WEBP_LOSSLESS -> webpAlgorithm.get()
            // The platform has no dependable AVIF encoder contract; never route users to
            // the placeholder encoder until a verified implementation is shipped.
            CompressionFormat.AVIF -> jpegAlgorithm.get()
            CompressionFormat.AUTO -> jpegAlgorithm.get() // Should not happen after determineAutoFormat
        }
    }

    private fun determineAutoFormat(bitmap: Bitmap): CompressionFormat {
        // Transparent images -> WEBP Lossless or PNG
        if (bitmap.hasAlpha()) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                CompressionFormat.WEBP_LOSSLESS
            } else {
                CompressionFormat.PNG
            }
        }

        // Android 14+ might prefer AVIF for high quality/low size, but we'll stick to WEBP/JPEG for now
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            CompressionFormat.WEBP_LOSSY
        } else {
            CompressionFormat.JPEG
        }
    }
}
