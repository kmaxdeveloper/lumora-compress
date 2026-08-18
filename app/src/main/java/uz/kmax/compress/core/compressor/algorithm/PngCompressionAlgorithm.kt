package uz.kmax.compress.core.compressor.algorithm

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.compressor.CompressionException
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class PngCompressionAlgorithm @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CompressionAlgorithm {

    override suspend fun compress(bitmap: Bitmap, request: CompressionRequest): CompressedImageData = withContext(ioDispatcher) {
        val outputStream = ByteArrayOutputStream()
        // PNG is lossless, so quality is ignored in standard Bitmap.compress
        val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        
        if (!success) {
            throw CompressionException.Unknown(Exception("PNG compression failed"))
        }

        CompressedImageData(
            data = outputStream.toByteArray(),
            format = CompressionFormat.PNG
        )
    }
}
