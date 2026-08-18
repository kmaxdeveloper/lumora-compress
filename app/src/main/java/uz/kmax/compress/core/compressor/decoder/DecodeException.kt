package uz.kmax.compress.core.compressor.decoder

sealed class DecodeException(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    class InvalidUri(uri: String) : DecodeException("Invalid or inaccessible Uri: $uri")
    class OutOfMemory : DecodeException("System ran out of memory while decoding bitmap")
    class ImageTooLarge(width: Int, height: Int) : DecodeException("Image is too large to safely process (${width}x${height})")
    class DecoderNotFound : DecodeException("No suitable decoder found for the given format")
    class DecodingFailed(cause: Throwable) : DecodeException("Bitmap decoding failed", cause)
}
