package uz.kmax.compress.feature.dashboard.viewmodel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.smart.SmartCompressionEngine
import uz.kmax.compress.domain.prediction.PredictionCalculator
import uz.kmax.compress.feature.dashboard.model.PredictionMapper
import uz.kmax.compress.feature.dashboard.state.PredictionState
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel
import javax.inject.Inject
@HiltViewModel class PredictionViewModel @Inject constructor(saved: SavedStateHandle, private val smart: SmartCompressionEngine, private val calculator: PredictionCalculator, private val mapper: PredictionMapper, private val analytics: AnalyticsManager): ViewModel() {
 private val image = requireNotNull(saved.get<GalleryImageUiModel>("imageModel")); private val _state=MutableStateFlow<PredictionState>(PredictionState.Loading); val state=_state.asStateFlow()
 init { calculate() }
 fun retry() { calculate() }
 private fun calculate() {
  viewModelScope.launch {
   _state.value=PredictionState.Loading
   runCatching { mapper.map(calculator.calculate(image, smart.makeDecision(image.uri))) }.onSuccess { _state.value=PredictionState.Content(it); analytics.logEvent("dashboard_opened"); analytics.logEvent("prediction_generated") }.onFailure { _state.value=PredictionState.Error(it.message ?: "Analysis failed") }
  }
 }
 fun image()=image
}
