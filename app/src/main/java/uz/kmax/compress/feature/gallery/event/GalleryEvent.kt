package uz.kmax.compress.feature.gallery.event

import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel

sealed interface GalleryEvent {
    data object OpenGallery : GalleryEvent
    data object OpenCamera : GalleryEvent
    data object OpenRecent : GalleryEvent
    data class NavigateCompress(val image: GalleryImageUiModel) : GalleryEvent
    data class NavigateBatch(val batchId: String) : GalleryEvent
    data class ShowLimitDialog(val limit: Int) : GalleryEvent
    data object ShowPermissionDialog : GalleryEvent
}
