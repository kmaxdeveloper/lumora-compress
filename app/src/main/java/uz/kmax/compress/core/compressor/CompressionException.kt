package uz.kmax.compress.core.compressor

sealed class CompressionException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    class InputFileNotFound(uri: String) : CompressionException("Input file not found at $uri")
    class OutputCreationFailed(message: String) : CompressionException(message)
    class UnsupportedFormat(format: String) : CompressionException("Unsupported format: $format")
    class OutOfMemory : CompressionException("Out of memory during compression")
    class PermissionDenied : CompressionException("Storage permission denied")
    class Cancelled : CompressionException("Compression task was cancelled")
    class Unknown(cause: Throwable) : CompressionException("An unknown error occurred", cause)
}
