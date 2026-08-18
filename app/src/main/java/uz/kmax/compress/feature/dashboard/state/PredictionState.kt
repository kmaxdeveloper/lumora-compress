package uz.kmax.compress.feature.dashboard.state
import uz.kmax.compress.feature.dashboard.model.PredictionUiModel
sealed interface PredictionState { data object Loading: PredictionState; data class Content(val model: PredictionUiModel): PredictionState; data class Error(val message: String): PredictionState }
