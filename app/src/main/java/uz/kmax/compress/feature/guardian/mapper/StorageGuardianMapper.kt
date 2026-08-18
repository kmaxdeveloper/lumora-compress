package uz.kmax.compress.feature.guardian.mapper

import uz.kmax.compress.domain.model.StorageImage
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import java.util.Locale

object StorageGuardianMapper {
    fun toUiModel(image: StorageImage): GalleryImageUiModel {
        return GalleryImageUiModel(
            uri = image.uri,
            name = image.name,
            size = formatSize(image.size),
            width = image.width,
            height = image.height,
            mimeType = image.mimeType,
            date = image.dateAdded
        )
    }

    fun formatSize(bytes: Long): String {
        val kb = bytes / 1024
        return if (kb > 1024) {
            String.format(Locale.getDefault(), "%.1f MB", kb / 1024f)
        } else {
            "$kb KB"
        }
    }
}
