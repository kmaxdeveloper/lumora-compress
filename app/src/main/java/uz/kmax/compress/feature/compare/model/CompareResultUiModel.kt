package uz.kmax.compress.feature.compare.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompareResultUiModel(
    val originalUri: Uri,
    val compressedUri: Uri,
    val originalSize: Long,
    val compressedSize: Long,
    val originalResolution: Pair<Int, Int>,
    val compressedResolution: Pair<Int, Int>,
    val format: String,
    val processingTime: Long,
    val savedBytes: Long,
    val savedPercentage: Float
) : Parcelable
