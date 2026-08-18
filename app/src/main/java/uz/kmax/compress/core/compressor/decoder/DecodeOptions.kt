package uz.kmax.compress.core.compressor.decoder

import android.graphics.Bitmap

data class DecodeOptions(
    val requiredSize: Pair<Int, Int>? = null,
    val preferredConfig: Bitmap.Config = Bitmap.Config.ARGB_8888,
    val allowHardwareBitmap: Boolean = false,
    val maxResolution: Int? = null,
    val samplingStrategy: SamplingStrategy = SamplingStrategy.PRESERVE_ASPECT_RATIO
) {
    enum class SamplingStrategy {
        PRESERVE_ASPECT_RATIO,
        EXACT_FIT,
        MAX_BOUNDS
    }
}
