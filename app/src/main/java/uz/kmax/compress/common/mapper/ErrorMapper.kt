package uz.kmax.compress.common.mapper

import uz.kmax.compress.core.compressor.CompressionException
import uz.kmax.compress.core.compressor.decoder.DecodeException
import uz.kmax.compress.core.compressor.writer.OutputException

data class UiError(
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val isRetryable: Boolean = true
)

object ErrorMapper {

    fun map(throwable: Throwable): UiError {
        return when (throwable) {
            is CompressionException -> when (throwable) {
                is CompressionException.InputFileNotFound -> UiError("File Not Found", "The selected image could not be located.")
                is CompressionException.OutOfMemory -> UiError("Out of Memory", "The image is too large to process. Try a smaller one.", isRetryable = false)
                is CompressionException.UnsupportedFormat -> UiError("Unsupported Format", "This image format is not supported for compression.")
                is CompressionException.PermissionDenied -> UiError("Permission Denied", "Storage access is required to compress images.")
                else -> UiError("Compression Failed", throwable.message ?: "An unexpected error occurred during compression.")
            }
            is DecodeException -> UiError("Image Loading Error", "We couldn't read the image data. The file might be corrupted.")
            is OutputException -> when (throwable) {
                is OutputException.DiskFull -> UiError("Storage Full", "Please free up some space on your device.", isRetryable = false)
                is OutputException.PermissionDenied -> UiError("Storage Permission", "Permission is needed to save the result.")
                else -> UiError("Saving Failed", "We couldn't save the compressed image to your device.")
            }
            else -> UiError("Something Went Wrong", throwable.localizedMessage ?: "A mysterious error occurred.")
        }
    }
}
