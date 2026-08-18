package uz.kmax.compress.core.compressor

enum class CompressionFormat(val mimeType: String, val fileExtension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP_LOSSY("image/webp", "webp"),
    WEBP_LOSSLESS("image/webp", "webp"),
    AVIF("image/avif", "avif"),
    AUTO("application/octet-stream", "img")
}
