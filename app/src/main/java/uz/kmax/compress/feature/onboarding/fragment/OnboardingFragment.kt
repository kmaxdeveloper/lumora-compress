package uz.kmax.compress.feature.onboarding.fragment

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentOnboardingBinding
import uz.kmax.compress.feature.onboarding.viewmodel.OnboardingEvent
import uz.kmax.compress.feature.onboarding.viewmodel.OnboardingViewModel

@AndroidEntryPoint
class OnboardingFragment : BaseFragmentNV<FragmentOnboardingBinding>(FragmentOnboardingBinding::inflate) {

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onViewCreated() {
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnGetStarted.setOnClickListener {
            viewModel.onFinishClicked()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun handleEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.NavigateHome -> {
                navController.navigate(R.id.action_onboardingFragment_to_homeFragment)
            }
        }
    }
}
