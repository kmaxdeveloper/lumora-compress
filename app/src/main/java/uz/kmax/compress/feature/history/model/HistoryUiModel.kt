package uz.kmax.compress.feature.history.model

import android.net.Uri

data class HistoryUiModel(
    val id: Long,
    val originalUri: Uri,
    val compressedUri: Uri,
    val originalSize: String,
    val compressedSize: String,
    val savedBytes: String,
    val savedPercent: String,
    val format: String,
    val resolution: String,
    val date: String,
    val favorite: Boolean,
    val isSelected: Boolean = false
)
