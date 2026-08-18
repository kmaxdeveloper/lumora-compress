package uz.kmax.compress.core.compressor.writer

data class OutputRequest(
    val data: ByteArray,
    val mimeType: String,
    val fileName: String,
    val destination: OutputDestination,
    val relativePath: String? = null,
    val overwrite: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OutputRequest
        if (!data.contentEquals(other.data)) return false
        if (mimeType != other.mimeType) return false
        if (fileName != other.fileName) return false
        if (destination != other.destination) return false
        if (relativePath != other.relativePath) return false
        if (overwrite != other.overwrite) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + (relativePath?.hashCode() ?: 0)
        result = 31 * result + overwrite.hashCode()
        return result
    }
}
