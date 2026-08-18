package uz.kmax.compress.core.compressor

import android.net.Uri
import uz.kmax.compress.core.compressor.metadata.MetadataOptions

data class CompressionRequest(
    val inputUri: Uri,
    val outputUri: Uri,
    val format: CompressionFormat = CompressionFormat.AUTO,
    val quality: CompressionQuality = CompressionQuality.Medium,
    val keepMetadata: Boolean = false,
    val metadataOptions: MetadataOptions = MetadataOptions(if (keepMetadata) MetadataOptions.Strategy.KEEP_ALL else MetadataOptions.Strategy.REMOVE_ALL),
    val resizeOptions: ResizeOptions? = null,
    val overwriteExistingFile: Boolean = true
) {
    data class ResizeOptions(
        val width: Int,
        val height: Int,
        val maintainAspectRatio: Boolean = true
    )
}
