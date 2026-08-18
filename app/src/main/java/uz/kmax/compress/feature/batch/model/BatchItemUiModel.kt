package uz.kmax.compress.feature.batch.model

import android.net.Uri

data class BatchItemUiModel(
    val id: String,
    val uri: Uri,
    val name: String,
    val status: Status = Status.QUEUED,
    val progress: Int = 0,
    val originalSize: Long = 0,
    val compressedSize: Long = 0
) {
    enum class Status {
        QUEUED,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
