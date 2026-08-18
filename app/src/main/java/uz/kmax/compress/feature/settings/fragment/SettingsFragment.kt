package uz.kmax.compress.feature.settings.fragment

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.BuildConfig
import uz.kmax.compress.R
import uz.kmax.compress.core.premium.PremiumState
import uz.kmax.compress.databinding.FragmentSettingsBinding
import uz.kmax.compress.feature.settings.event.SettingsEvent
import uz.kmax.compress.feature.settings.state.SettingsUiState
import uz.kmax.compress.feature.settings.viewmodel.SettingsViewModel

@AndroidEntryPoint
class SettingsFragment : BaseFragmentNV<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated() {
        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            viewModel.onBackClicked()
        }
    }

    private fun setupListeners() {
        binding.apply {
            cardPremium.setOnClickListener { 
                navController.navigate(R.id.action_global_premiumFragment)
            }
            itemTheme.setOnClickListener { showThemeDialog() }
            itemLanguage.setOnClickListener { showLanguageDialog() }
            itemPrivacy.setOnClickListener { viewModel.onPrivacyPolicyClicked() }
            itemTerms.setOnClickListener { viewModel.onTermsClicked() }
            itemLicenses.setOnClickListener { viewModel.onLicensesClicked() }
            itemExport.setOnClickListener { 
                viewModel.exportHistory(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!)
            }
            itemReset.setOnClickListener { 
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.factory_reset)
                    .setMessage(R.string.factory_reset_desc)
                    .setPositiveButton(R.string.reset) { _, _ -> viewModel.factoryReset() }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
            
            // New Listeners
            tvVersion.setOnClickListener { 
                viewModel.onRateAppClicked()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        renderState(state)
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

    private fun renderState(state: SettingsUiState) {
        binding.apply {
            state.preferences?.let { prefs ->
                tvCurrentTheme.text = prefs.themeMode
                tvCurrentLanguage.text = when(prefs.language) {
                    "ru" -> "Русский"
                    "uz" -> "O'zbek"
                    else -> "English"
                }
            }

            tvPremiumStatus.text = if (state.premiumState is PremiumState.Premium) {
                getString(R.string.premium_active)
            } else {
                getString(R.string.upgrade_premium)
            }

            tvVersion.text = getString(R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        }
    }

    private fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.NavigateBack -> navController.popBackStack()
            is SettingsEvent.ShowSnackbar -> Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
            is SettingsEvent.OpenPrivacyPolicy -> openUrl("https://kmax.uz/privacy-policy")
            is SettingsEvent.OpenTerms -> openUrl("https://kmax.uz/terms")
            is SettingsEvent.OpenLicenses -> {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
            }
            is SettingsEvent.RateApp -> {
                openUrl("market://details?id=${requireContext().packageName}")
            }
            is SettingsEvent.ShareApp -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out Lumora Compress on Play Store: https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                }
                startActivity(Intent.createChooser(shareIntent, "Share Lumora Compress"))
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Snackbar.make(binding.root, "No browser found", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("System", "Light", "Dark")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.theme)
            .setItems(themes) { _, which ->
                viewModel.setThemeMode(themes[which].uppercase())
            }
            .show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Русский", "O'zbek")
        val codes = arrayOf("en", "ru", "uz")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language)
            .setItems(languages) { _, which ->
                viewModel.setLanguage(codes[which])
            }
            .show()
    }
}
