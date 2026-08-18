package uz.kmax.compress.core.config.model

data class AppConfig(
    val adsEnabled: Boolean = true,
    val adInterstitialFrequency: Int = 2,
    val adRewardValue: Int = 1,
    val compressionPresetQuality: Int = 80,
    val premiumDiscountEnabled: Boolean = false,
    val seasonalBannerUrl: String? = null,
    val experimentalAvifEnabled: Boolean = false,
    val minAppVersion: Int = 1,
    val forceUpdateEnabled: Boolean = false,
    val maintenanceMode: Boolean = false
)
