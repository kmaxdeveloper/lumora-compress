package uz.kmax.compress.core.premium

import kotlinx.coroutines.flow.StateFlow
import uz.kmax.compress.domain.repository.BillingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumManager @Inject constructor(
    private val repository: BillingRepository
) {
    val premiumState: StateFlow<PremiumState> = repository.premiumState

    fun isPremium(): Boolean {
        return premiumState.value is PremiumState.Premium || premiumState.value is PremiumState.GracePeriod
    }

    fun isFeatureUnlocked(feature: PremiumFeature): Boolean {
        val isPremiumUser = isPremium()
        return when (feature) {
            PremiumFeature.BATCH_COMPRESSION -> isPremiumUser
            PremiumFeature.AVIF_FORMAT -> isPremiumUser
            PremiumFeature.AD_FREE -> isPremiumUser
            PremiumFeature.UNLIMITED_FILES -> isPremiumUser
            PremiumFeature.CUSTOM_QUALITY -> true
            PremiumFeature.SOCIAL_MEDIA_PRESETS -> isPremiumUser
            PremiumFeature.TARGET_SIZE_COMPRESSION -> isPremiumUser
        }
    }
}
