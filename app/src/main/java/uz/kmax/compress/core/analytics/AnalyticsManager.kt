package uz.kmax.compress.core.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor() {

    private val analytics by lazy { Firebase.analytics }

    fun logEvent(eventName: String, params: Bundle? = null) {
        analytics.logEvent(eventName, params)
    }

    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    fun logCompressStarted(format: String, quality: Int) {
        val params = Bundle().apply {
            putString("selected_format", format)
            putInt("selected_quality", quality)
        }
        logEvent("compress_started", params)
    }

    fun logCompressFinished(ratio: Float, savedBytes: Long) {
        val params = Bundle().apply {
            putFloat("compression_ratio", ratio)
            putLong("saved_bytes", savedBytes)
        }
        logEvent("compress_finished", params)
    }

    fun logCompressFailed(error: String) {
        val params = Bundle().apply {
            putString("error_message", error)
        }
        logEvent("compress_failed", params)
    }

    fun logBatchStarted(count: Int) {
        val params = Bundle().apply {
            putInt("item_count", count)
        }
        logEvent("batch_started", params)
    }

    fun logBatchFinished(successCount: Int, failedCount: Int) {
        val params = Bundle().apply {
            putInt("success_count", successCount)
            putInt("failed_count", failedCount)
        }
        logEvent("batch_finished", params)
    }

    fun logPremiumClicked(source: String) {
        val params = Bundle().apply {
            putString("source", source)
        }
        logEvent("premium_clicked", params)
    }

    fun logPurchaseCompleted(productId: String) {
        val params = Bundle().apply {
            putString("product_id", productId)
        }
        logEvent("purchase_completed", params)
    }

    fun logRewardCompleted(rewardType: String) {
        val params = Bundle().apply {
            putString("reward_type", rewardType)
        }
        logEvent("reward_completed", params)
    }

    fun logAdClicked(adType: String) {
        val params = Bundle().apply {
            putString("ad_type", adType)
        }
        logEvent("ad_clicked", params)
    }

    fun logAdFailed(adType: String, errorCode: Int) {
        val params = Bundle().apply {
            putString("ad_type", adType)
            putInt("error_code", errorCode)
        }
        logEvent("ad_failed", params)
    }

    fun logHistoryOpened() {
        logEvent("history_opened")
    }

    fun logSocialPresetSelected(preset: String) { logEvent("social_preset_selected", Bundle().apply { putString("preset", preset) }) }
    fun logPredictionAccuracy(estimatedBytes: Long, actualBytes: Long) { logEvent("prediction_accuracy", Bundle().apply { putLong("estimated_bytes", estimatedBytes); putLong("actual_bytes", actualBytes) }) }
    fun logTargetLoop(event:String, target:Long, iterations:Int=0) { logEvent(event, Bundle().apply { putLong("target_bytes",target); putInt("iterations_count",iterations) }) }
}
