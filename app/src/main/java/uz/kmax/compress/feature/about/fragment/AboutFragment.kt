package uz.kmax.compress.feature.about.fragment

import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.BuildConfig
import uz.kmax.compress.databinding.FragmentAboutBinding

@AndroidEntryPoint
class AboutFragment : BaseFragmentNV<FragmentAboutBinding>(FragmentAboutBinding::inflate) {
    override fun onViewCreated() {
        binding.toolbar.setNavigationOnClickListener { navController.popBackStack() }
        binding.version.text = getString(uz.kmax.compress.R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}
