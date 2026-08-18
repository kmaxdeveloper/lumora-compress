package uz.kmax.compress.core.compressor.processor

data class ResizeOptions(
    val width: Int? = null,
    val height: Int? = null,
    val strategy: ResizeStrategy = ResizeStrategy.KEEP_ASPECT_RATIO,
    val allowUpscale: Boolean = false,
    val maxResolution: Int? = null
)
