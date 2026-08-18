package uz.kmax.compress.feature.dashboard.model
import uz.kmax.compress.domain.prediction.PredictionData
import javax.inject.Inject
class PredictionMapper @Inject constructor() { fun map(d: PredictionData) = PredictionUiModel(d.originalSize.format(), d.estimatedSize.format(), "${d.savingPercent.toInt()}%", d.quality, d.format, "${d.width} × ${d.height}", String.format("%.1f sec", d.seconds), d.imageType.lowercase().replaceFirstChar { it.uppercase() }, d.reason); private fun Long.format() = if (this >= 1024 * 1024) String.format("%.1f MB", this / 1048576f) else "${this / 1024} KB" }
