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

class AvifCompressionAlgorithm @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CompressionAlgorithm {

    override suspend fun compress(bitmap: Bitmap, request: CompressionRequest): CompressedImageData = withContext(ioDispatcher) {
        if (Build.VERSION.SDK_INT < 34) {
            throw CompressionException.UnsupportedFormat("AVIF is only supported on Android 14+")
        }

        val outputStream = ByteArrayOutputStream()
        // Note: As of Android 14, AVIF compression is supported via specific vendor implementations 
        // or newer system decoders. Standard Bitmap.CompressFormat might not have AVIF yet 
        // in all SDK stubs, but we'll prepare the contract.
        
        val success = try {
            // Placeholder for actual AVIF encoding if supported by system
            // On most devices this might need a 3rd party library, but we stick to system contracts.
            // For now, we report as unsupported if not available in CompressFormat.
            throw CompressionException.UnsupportedFormat("System AVIF encoder not found")
        } catch (e: Exception) {
            false
        }

        if (!success) {
            throw CompressionException.Unknown(Exception("AVIF compression failed"))
        }

        CompressedImageData(
            data = outputStream.toByteArray(),
            format = CompressionFormat.AVIF
        )
    }
}
