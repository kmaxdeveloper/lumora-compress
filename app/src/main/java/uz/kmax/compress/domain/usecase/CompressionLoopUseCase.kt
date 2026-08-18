package uz.kmax.compress.domain.usecase
import uz.kmax.compress.domain.adaptive.AdaptiveCompressionEngine
import javax.inject.Inject
class CompressionLoopUseCase @Inject constructor(private val engine: AdaptiveCompressionEngine) {
    operator fun invoke(request: uz.kmax.compress.core.compressor.CompressionRequest, targetBytes: Long, initialQuality: Int) =
        engine.compress(request, targetBytes, initialQuality)
}
