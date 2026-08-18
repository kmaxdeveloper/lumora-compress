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

class JpegCompressionAlgorithm @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CompressionAlgorithm {

    override suspend fun compress(bitmap: Bitmap, request: CompressionRequest): CompressedImageData = withContext(ioDispatcher) {
        val outputStream = ByteArrayOutputStream()
        val success = bitmap.compress(Bitmap.CompressFormat.JPEG, request.quality.value, outputStream)
        
        if (!success) {
            throw CompressionException.Unknown(Exception("JPEG compression failed"))
        }

        CompressedImageData(
            data = outputStream.toByteArray(),
            format = CompressionFormat.JPEG
        )
    }
}
