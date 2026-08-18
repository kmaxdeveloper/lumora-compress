package uz.kmax.compress.core.compressor.algorithm

import uz.kmax.compress.core.compressor.CompressionFormat

enum class CompressionAlgorithmType {
    JPEG,
    PNG,
    WEBP_LOSSY,
    WEBP_LOSSLESS,
    AVIF;

    companion object {
        fun fromFormat(format: CompressionFormat): CompressionAlgorithmType? = when (format) {
            CompressionFormat.JPEG -> JPEG
            CompressionFormat.PNG -> PNG
            CompressionFormat.WEBP_LOSSY -> WEBP_LOSSY
            CompressionFormat.WEBP_LOSSLESS -> WEBP_LOSSLESS
            CompressionFormat.AVIF -> AVIF
            CompressionFormat.AUTO -> null
        }
    }
}
