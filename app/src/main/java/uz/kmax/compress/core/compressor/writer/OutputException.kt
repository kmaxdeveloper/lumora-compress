package uz.kmax.compress.core.compressor.writer

sealed class OutputException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    class PermissionDenied : OutputException("Storage permission denied for output")
    class DiskFull : OutputException("Device storage is full")
    class WriteFailed(message: String, cause: Throwable? = null) : OutputException(message, cause)
    class InvalidDestination : OutputException("The requested output destination is invalid")
    class FileAlreadyExists : OutputException("Target file already exists and overwrite is disabled")
    class StorageUnavailable : OutputException("Storage media is currently unavailable")
}
