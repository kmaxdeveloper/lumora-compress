package uz.kmax.compress.core.social

import android.net.Uri
import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionQuality
import uz.kmax.compress.core.compressor.CompressionRequest
import uz.kmax.compress.core.compressor.metadata.MetadataOptions
import uz.kmax.compress.core.smart.SmartCompressionDecision
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialOptimizer @Inject constructor() {
    fun optimize(inputUri: Uri, outputUri: Uri, preset: SocialPreset, smart: SmartCompressionDecision?): CompressionRequest {
        val screenshot = smart?.classification?.name == "SCREENSHOT"
        val format = if (screenshot && preset.format == CompressionFormat.JPEG) CompressionFormat.PNG else preset.format
        val quality = if (screenshot && format == CompressionFormat.JPEG) 95 else preset.quality
        val width = preset.maxWidth
        val height = preset.maxHeight
        return CompressionRequest(inputUri, outputUri, format, CompressionQuality.Custom(quality),
            metadataOptions = MetadataOptions(MetadataOptions.Strategy.REMOVE_GPS),
            resizeOptions = if (width != null && height != null) CompressionRequest.ResizeOptions(width, height) else null)
    }
}
