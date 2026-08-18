package uz.kmax.compress.core.compressor.metadata

data class MetadataOptions(
    val strategy: Strategy = Strategy.KEEP_ALL,
    val customRules: List<String> = emptyList()
) {
    enum class Strategy {
        KEEP_ALL,
        REMOVE_ALL,
        KEEP_ONLY_ORIENTATION,
        KEEP_DATE_AND_ORIENTATION,
        REMOVE_GPS,
        CUSTOM
    }
}
