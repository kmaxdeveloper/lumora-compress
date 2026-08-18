package uz.kmax.compress

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.compress.core.monetization.AdsManager
import uz.kmax.compress.core.monetization.PlayServicesManager
import uz.kmax.compress.core.preferences.PreferencesManager
import uz.kmax.compress.databinding.ActivityMainBinding
import javax.inject.Inject
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var playServicesManager: PlayServicesManager

    @Inject
    lateinit var adsManager: AdsManager

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
        // Permission result handled by system
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observePreferences()
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        setupEdgeToEdge()
        checkNotificationPermission()
        
        // Check for updates
        playServicesManager.checkUpdate(this)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun observePreferences() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferencesManager.preferencesFlow.collect { prefs ->
                    applyTheme(prefs.themeMode)
                }
            }
        }
    }

    private fun applyTheme(mode: String) {
        val appCompatMode = when (mode) {
            "LIGHT" -> AppCompatDelegate.MODE_NIGHT_NO
            "DARK" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(appCompatMode)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
