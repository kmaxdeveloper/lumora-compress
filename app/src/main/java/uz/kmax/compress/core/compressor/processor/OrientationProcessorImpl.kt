package uz.kmax.compress.core.compressor.processor

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import uz.kmax.compress.core.compressor.CompressionException
import uz.kmax.compress.core.compressor.decoder.DecodedBitmap
import uz.kmax.compress.core.di.qualifier.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrientationProcessorImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : OrientationProcessor {

    override suspend fun process(decodedBitmap: DecodedBitmap): Bitmap = withContext(ioDispatcher) {
        if (decodedBitmap.rotation == 0) {
            return@withContext decodedBitmap.bitmap
        }

        val matrix = Matrix().apply {
            postRotate(decodedBitmap.rotation.toFloat())
        }

        try {
            Bitmap.createBitmap(
                decodedBitmap.bitmap,
                0,
                0,
                decodedBitmap.bitmap.width,
                decodedBitmap.bitmap.height,
                matrix,
                true
            )
        } catch (e: OutOfMemoryError) {
            throw CompressionException.OutOfMemory()
        } catch (e: Exception) {
            throw CompressionException.Unknown(e)
        }
    }
}
