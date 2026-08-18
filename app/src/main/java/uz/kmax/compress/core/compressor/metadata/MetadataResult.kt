package uz.kmax.compress.core.compressor.metadata

data class MetadataResult(
    val tagsCopied: Int,
    val tagsRemoved: Int,
    val warnings: List<String> = emptyList()
)
