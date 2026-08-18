package uz.kmax.compress.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import uz.kmax.compress.domain.usecase.SetFirstLaunchCompletedUseCase
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setFirstLaunchCompletedUseCase: SetFirstLaunchCompletedUseCase
) : ViewModel() {

    private val _event = MutableSharedFlow<OnboardingEvent>()
    val event = _event.asSharedFlow()

    fun onFinishClicked() {
        viewModelScope.launch {
            setFirstLaunchCompletedUseCase()
            _event.emit(OnboardingEvent.NavigateHome)
        }
    }
}

sealed interface OnboardingEvent {
    data object NavigateHome : OnboardingEvent
}
