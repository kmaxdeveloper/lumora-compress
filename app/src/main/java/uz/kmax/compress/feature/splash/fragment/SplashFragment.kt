package uz.kmax.compress.feature.splash.fragment

import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.databinding.FragmentSplashBinding
import uz.kmax.compress.feature.splash.event.SplashEvent
import uz.kmax.compress.feature.splash.viewmodel.SplashViewModel

import uz.kmax.compress.core.monetization.ConsentManager
import uz.kmax.compress.core.monetization.AdsManager
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragmentNV<FragmentSplashBinding>(FragmentSplashBinding::inflate) {

    @Inject
    lateinit var consentManager: ConsentManager

    @Inject
    lateinit var adsManager: AdsManager

    private val viewModel: SplashViewModel by viewModels()

    override fun onViewCreated() {
        startFadeAnimation()
        checkConsentAndStart()
    }

    private fun checkConsentAndStart() {
        consentManager.gatherConsent(
            requireActivity(),
            object : ConsentManager.OnConsentCheckListener {
                override fun onConsentRequired() {}
                override fun onConsentNotRequired() {
                    if (isAdded) {
                        adsManager.loadAppOpenAd(requireActivity())
                        observeViewModel()
                    }
                }
                override fun onError(error: String) {
                    if (isAdded) {
                        observeViewModel()
                    }
                }
            }
        )
    }

    private fun startFadeAnimation() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 500
            fillAfter = true
        }
        binding.splashContainer.startAnimation(fadeIn)
    }

    private fun observeViewModel() {
        if (view == null) return
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding.progressIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.event.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun handleEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.NavigateToOnboarding -> {
                navController.navigate(R.id.action_splashFragment_to_onboardingFragment)
            }
            is SplashEvent.NavigateToHome -> {
                navController.navigate(R.id.action_splashFragment_to_homeFragment)
            }
        }
    }
}
