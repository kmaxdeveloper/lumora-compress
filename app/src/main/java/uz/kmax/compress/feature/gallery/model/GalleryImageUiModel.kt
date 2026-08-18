package uz.kmax.compress.feature.gallery.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryImageUiModel(
    val uri: Uri,
    val name: String,
    val size: String,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val date: Long
) : Parcelable
