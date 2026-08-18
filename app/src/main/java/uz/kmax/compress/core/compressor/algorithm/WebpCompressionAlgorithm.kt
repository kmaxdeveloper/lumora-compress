package uz.kmax.compress.core.compressor.algorithm

import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.compressor.CompressionException
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class WebpCompressionAlgorithm @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CompressionAlgorithm {

    override suspend fun compress(bitmap: Bitmap, request: CompressionRequest): CompressedImageData = withContext(ioDispatcher) {
        val outputStream = ByteArrayOutputStream()
        
        val outputFormat = when (request.format) {
            CompressionFormat.WEBP_LOSSLESS -> {
                CompressionFormat.WEBP_LOSSLESS
            }
            CompressionFormat.AUTO -> {
                if (bitmap.hasAlpha()) CompressionFormat.WEBP_LOSSLESS else CompressionFormat.WEBP_LOSSY
            }
            else -> CompressionFormat.WEBP_LOSSY
        }

        val webpFormat = when (outputFormat) {
            CompressionFormat.WEBP_LOSSLESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
        }

        val success = bitmap.compress(webpFormat, request.quality.value, outputStream)
        
        if (!success) {
            throw CompressionException.Unknown(Exception("WEBP compression failed"))
        }

        CompressedImageData(
            data = outputStream.toByteArray(),
            format = outputFormat
        )
    }
}
