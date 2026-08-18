package uz.kmax.compress.core.compressor.algorithm

import uz.kmax.compress.core.compressor.CompressionFormat

data class CompressedImageData(
    val data: ByteArray,
    val format: CompressionFormat,
    val size: Long = data.size.toLong()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompressedImageData
        if (!data.contentEquals(other.data)) return false
        if (format != other.format) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + format.hashCode()
        return result
    }
}
