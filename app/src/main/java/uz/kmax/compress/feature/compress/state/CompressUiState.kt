package uz.kmax.compress.feature.compress.state

import uz.kmax.compress.core.compressor.CompressionFormat
import uz.kmax.compress.core.compressor.CompressionQuality
import uz.kmax.compress.core.compressor.metadata.MetadataOptions
import uz.kmax.compress.core.compressor.processor.ResizeOptions
import uz.kmax.compress.core.smart.SmartCompressionDecision
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import uz.kmax.compress.core.social.SocialPreset

enum class CompressionMode { SMART, MANUAL, SOCIAL, TARGET_SIZE }

data class CompressUiState(
    val imageModel: GalleryImageUiModel,
    val isSmartMode: Boolean = true,
    val mode: CompressionMode = CompressionMode.SMART,
    val selectedSocialPreset: SocialPreset = SocialPreset.INSTAGRAM,
    val targetSizeBytes: Long = 500 * 1024L,
    val targetIteration: Int = 0,
    val targetResultBytes: Long? = null,
    val smartDecision: SmartCompressionDecision? = null,
    val selectedFormat: CompressionFormat = CompressionFormat.AUTO,
    val selectedQuality: Int = 80,
    val resizeEnabled: Boolean = false,
    val resizeOptions: ResizeOptions = ResizeOptions(),
    val metadataStrategy: MetadataOptions.Strategy = MetadataOptions.Strategy.KEEP_ALL,
    val estimatedSize: Long = 0,
    val isCompressing: Boolean = false,
    val isLoading: Boolean = true,
    val isPremium: Boolean = false
)
