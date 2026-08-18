package uz.kmax.compress.domain.model

import android.net.Uri

data class StorageImage(
    val uri: Uri,
    val name: String,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val dateAdded: Long
)
