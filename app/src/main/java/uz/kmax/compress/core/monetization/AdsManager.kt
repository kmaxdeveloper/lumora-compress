package uz.kmax.compress.core.monetization

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.config.ConfigManager
import uz.kmax.compress.core.premium.PremiumFeature
import uz.kmax.compress.core.premium.PremiumManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: ConfigManager,
    private val consentManager: ConsentManager,
    private val premiumManager: PremiumManager,
    private val analyticsManager: AnalyticsManager
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    private var loadTime: Long = 0
    private var lastInterstitialTime: Long = 0
    private var compressionCount: Int = 0
    private var currentActivity: Activity? = null

    init {
        MobileAds.initialize(context) {}
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        (context as? Application)?.registerActivityLifecycleCallbacks(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let { showAppOpenAdIfAvailable(it) }
    }

    fun isAdFree(): Boolean = premiumManager.isFeatureUnlocked(PremiumFeature.AD_FREE)

    fun loadAppOpenAd(activity: Activity) {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled || !consentManager.canRequestAds() || isAppOpenAdAvailable()) return

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            activity,
            "ca-app-pub-4664801446868642/4769547268",
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = Date().time
                    analyticsManager.logEvent("ad_app_open_loaded")
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && (Date().time - loadTime) < 4 * 3600000
    }

    private fun showAppOpenAdIfAvailable(activity: Activity) {
        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity)
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                loadAppOpenAd(activity)
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                loadAppOpenAd(activity)
            }
        }
    }

    fun showAppOpenAd(activity: Activity) {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled || !consentManager.canRequestAds()) return
        if (isAppOpenAdAvailable()) {
            appOpenAd?.show(activity)
        } else {
            loadAppOpenAd(activity)
        }
    }

    fun destroyBanner(adView: AdView?) {
        try {
            adView?.destroy()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun loadInterstitial(activity: Activity? = null) {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled || !consentManager.canRequestAds()) return

        val loadContext = activity ?: currentActivity ?: context
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            loadContext,
            "ca-app-pub-4664801446868642/8712807490",
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    analyticsManager.logEvent("ad_interstitial_loaded")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        if (isAdFree()) {
            onDismiss()
            return
        }

        compressionCount++
        val config = configManager.configFlow.value
        val cooldown = 90 * 1000L // Default cooldown
        val frequency = config.adInterstitialFrequency

        if (interstitialAd != null && 
            compressionCount % frequency == 0 && 
            (System.currentTimeMillis() - lastInterstitialTime) > cooldown) {
            
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    analyticsManager.logAdClicked("interstitial")
                    loadInterstitial(activity)
                    onDismiss()
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    analyticsManager.logAdFailed("interstitial", error.code)
                    onDismiss()
                }
                override fun onAdImpression() {
                    analyticsManager.logEvent("ad_interstitial_impression")
                }
            }
            interstitialAd?.show(activity)
        } else {
            onDismiss()
            if (interstitialAd == null) loadInterstitial(activity)
        }
    }

    fun loadRewardedAd(activity: Activity? = null) {
        if (!consentManager.canRequestAds()) return
        val loadContext = activity ?: currentActivity ?: context
        val request = AdRequest.Builder().build()
        val rewardId = if (uz.kmax.compress.BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            "ca-app-pub-4664801446868642/8712807490"
        }
        RewardedAd.load(
            loadContext,
            rewardId,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    analyticsManager.logEvent("ad_rewarded_loaded")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: (RewardItem) -> Unit, onDismiss: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onDismiss()
                }
                override fun onAdImpression() {
                    analyticsManager.logEvent("ad_rewarded_impression")
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                analyticsManager.logRewardCompleted(rewardItem.type)
                onRewardEarned(rewardItem)
            }
        } else {
            loadRewardedAd(activity)
            onDismiss()
        }
    }

    fun createBannerView(activity: Activity): AdView {
        return AdView(activity).apply {
            adUnitId = "ca-app-pub-4664801446868642/6066462853"
            setAdSize(getAdSize(activity))
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    analyticsManager.logEvent("ad_banner_loaded")
                }
                override fun onAdImpression() {
                    analyticsManager.logEvent("ad_banner_impression")
                }
                override fun onAdClicked() {
                    analyticsManager.logAdClicked("banner")
                }
            }
        }
    }

    fun loadNativeAd(activity: Activity, onAdLoaded: (NativeAd) -> Unit, onAdFailed: () -> Unit = {}) {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled || !consentManager.canRequestAds()) return
        
        val adRequest = AdRequest.Builder().build()

        AdLoader.Builder(activity, "ca-app-pub-4664801446868642/1612900888")
            .forNativeAd { ad ->
                onAdLoaded(ad)
                analyticsManager.logEvent("ad_native_loaded")
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    analyticsManager.logAdFailed("native", error.code)
                    onAdFailed()
                }
            })
            .build()
            .loadAd(adRequest)
    }

    private fun getAdSize(activity: Activity): AdSize {
        if (isAdFree()) return AdSize.INVALID
        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = displayMetrics.widthPixels.toFloat()
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    // ActivityLifecycleCallbacks implementation
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
