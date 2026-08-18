package uz.kmax.compress.core.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.kmax.compress.core.config.model.AppConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigManager @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig by lazy { Firebase.remoteConfig }

    private val _configFlow = MutableStateFlow(AppConfig())
    val configFlow = _configFlow.asStateFlow()

    init {
        // Initialize config lazily if needed, or wrap in a check
        try {
            setupRemoteConfig()
        } catch (e: Exception) {
            // Likely in a secondary process where Firebase is not initialized
            // We can ignore this as the secondary process shouldn't need the config
        }
    }

    private fun setupRemoteConfig() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Define default values
        val defaults = mapOf(
            "ads_enabled" to true,
            "ad_interstitial_frequency" to 2L,
            "ad_reward_value" to 1L,
            "compression_preset_quality" to 80L,
            "premium_discount_enabled" to false,
            "seasonal_banner_url" to "",
            "experimental_avif_enabled" to false,
            "min_app_version" to 1L,
            "force_update_enabled" to false,
            "maintenance_mode" to false
        )
        remoteConfig.setDefaultsAsync(defaults)

        fetchAndActivate()
    }

    fun fetchAndActivate() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateConfig()
            }
        }
    }

    private fun updateConfig() {
        _configFlow.value = AppConfig(
            adsEnabled = remoteConfig.getBoolean("ads_enabled"),
            adInterstitialFrequency = remoteConfig.getLong("ad_interstitial_frequency").toInt(),
            adRewardValue = remoteConfig.getLong("ad_reward_value").toInt(),
            compressionPresetQuality = remoteConfig.getLong("compression_preset_quality").toInt(),
            premiumDiscountEnabled = remoteConfig.getBoolean("premium_discount_enabled"),
            seasonalBannerUrl = remoteConfig.getString("seasonal_banner_url").takeIf { it.isNotBlank() },
            experimentalAvifEnabled = remoteConfig.getBoolean("experimental_avif_enabled"),
            minAppVersion = remoteConfig.getLong("min_app_version").toInt(),
            forceUpdateEnabled = remoteConfig.getBoolean("force_update_enabled"),
            maintenanceMode = remoteConfig.getBoolean("maintenance_mode")
        )
    }
}
