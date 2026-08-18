package uz.kmax.compress.core.compressor.pipeline

import android.graphics.Bitmap
import uz.kmax.compress.core.compressor.algorithm.CompressedImageData
import uz.kmax.compress.core.compressor.decoder.DecodedBitmap
import java.io.File

data class PipelineContext(
    var decodedBitmap: DecodedBitmap? = null,
    var rotatedBitmap: Bitmap? = null,
    var resizedBitmap: Bitmap? = null,
    var compressedImageData: CompressedImageData? = null,
    var outputFile: File? = null,
    var startTime: Long = 0
)
