package uz.kmax.compress.core.monetization

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.kmax.compress.core.analytics.AnalyticsManager
import uz.kmax.compress.core.config.ConfigManager
import uz.kmax.compress.core.premium.PremiumFeature
import uz.kmax.compress.core.premium.PremiumManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class YandexAdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configManager: ConfigManager,
    private val premiumManager: PremiumManager,
    private val analyticsManager: AnalyticsManager
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    
    private var appOpenAdLoader: AppOpenAdLoader? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null

    private var loadTime: Long = 0
    private var lastInterstitialTime: Long = 0
    private var compressionCount: Int = 0
    private var currentActivity: Activity? = null

    init {
        MobileAds.initialize(context) {
            // Initialization complete
        }
        setupAppOpenAdLoader()
        setupInterstitialAdLoader()
        
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        (context as? Application)?.registerActivityLifecycleCallbacks(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let { showAppOpenAdIfAvailable(it) }
    }

    fun isAdFree(): Boolean = premiumManager.isFeatureUnlocked(PremiumFeature.AD_FREE)

    private fun setupAppOpenAdLoader() {
        appOpenAdLoader = AppOpenAdLoader(context).apply {
            setAdLoadListener(object : AppOpenAdLoadListener {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = Date().time
                    analyticsManager.logEvent("yandex_app_open_loaded")
                }
                override fun onAdFailedToLoad(error: AdRequestError) {
                    analyticsManager.logEvent("yandex_app_open_failed")
                }
            })
        }
    }

    private fun setupInterstitialAdLoader() {
        interstitialAdLoader = InterstitialAdLoader(context).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    analyticsManager.logEvent("yandex_interstitial_loaded")
                }
                override fun onAdFailedToLoad(error: AdRequestError) {
                    analyticsManager.logEvent("yandex_interstitial_failed")
                }
            })
        }
    }

    fun loadAppOpenAd() {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled || isAppOpenAdAvailable()) return

        val adRequestConfiguration = AdRequestConfiguration.Builder("R-M-19761691-3").build()
        appOpenAdLoader?.loadAd(adRequestConfiguration)
    }

    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && (Date().time - loadTime) < 4 * 3600000 // 4 hours
    }

    private fun showAppOpenAdIfAvailable(activity: Activity) {
        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd()
            return
        }

        appOpenAd?.setAdEventListener(object : AppOpenAdEventListener {
            override fun onAdShown() {}
            override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {
                appOpenAd = null
                loadAppOpenAd()
            }
            override fun onAdDismissed() {
                appOpenAd = null
                loadAppOpenAd()
            }
            override fun onAdClicked() {
                analyticsManager.logAdClicked("yandex_app_open")
            }
            override fun onAdImpression(data: ImpressionData?) {
                analyticsManager.logEvent("yandex_app_open_impression")
            }
        })
        appOpenAd?.show(activity)
    }

    fun loadInterstitial() {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled) return

        val adRequestConfiguration = AdRequestConfiguration.Builder("R-M-19761691-4").build()
        interstitialAdLoader?.loadAd(adRequestConfiguration)
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        if (isAdFree()) {
            onDismiss()
            return
        }

        compressionCount++
        val config = configManager.configFlow.value
        val cooldown = 90 * 1000L
        val frequency = config.adInterstitialFrequency

        if (interstitialAd != null && 
            compressionCount % frequency == 0 && 
            (System.currentTimeMillis() - lastInterstitialTime) > cooldown) {
            
            interstitialAd?.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}
                override fun onAdFailedToShow(error: com.yandex.mobile.ads.common.AdError) {
                    onDismiss()
                }
                override fun onAdDismissed() {
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    loadInterstitial()
                    onDismiss()
                }
                override fun onAdClicked() {
                    analyticsManager.logAdClicked("yandex_interstitial")
                }
                override fun onAdImpression(data: ImpressionData?) {
                    analyticsManager.logEvent("yandex_interstitial_impression")
                }
            })
            interstitialAd?.show(activity)
        } else {
            onDismiss()
            if (interstitialAd == null) loadInterstitial()
        }
    }

    fun createBannerView(activity: Activity): BannerAdView {
        return BannerAdView(activity).apply {
            setAdUnitId("R-M-19761691-1")
            setAdSize(getAdSize(activity))
            val adRequest = com.yandex.mobile.ads.common.AdRequest.Builder().build()
            
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    analyticsManager.logEvent("yandex_banner_loaded")
                }
                override fun onAdFailedToLoad(error: AdRequestError) {
                    analyticsManager.logEvent("yandex_banner_failed")
                }
                override fun onAdClicked() {
                    analyticsManager.logAdClicked("yandex_banner")
                }
                override fun onLeftApplication() {}
                override fun onReturnedToApplication() {}
                override fun onImpression(data: ImpressionData?) {
                    analyticsManager.logEvent("yandex_banner_impression")
                }
            })
            loadAd(adRequest)
        }
    }

    fun loadNativeAd(onAdLoaded: (com.yandex.mobile.ads.nativeads.NativeAd) -> Unit, onAdFailed: () -> Unit = {}) {
        if (isAdFree() || !configManager.configFlow.value.adsEnabled) return

        val nativeAdLoader = com.yandex.mobile.ads.nativeads.NativeAdLoader(context)
        nativeAdLoader.setNativeAdLoadListener(object : com.yandex.mobile.ads.nativeads.NativeAdLoadListener {
            override fun onAdLoaded(nativeAd: com.yandex.mobile.ads.nativeads.NativeAd) {
                analyticsManager.logEvent("yandex_native_loaded")
                onAdLoaded(nativeAd)
            }
            override fun onAdFailedToLoad(error: AdRequestError) {
                analyticsManager.logEvent("yandex_native_failed")
                onAdFailed()
            }
        })
        
        val adRequestConfiguration = com.yandex.mobile.ads.nativeads.NativeAdRequestConfiguration.Builder("R-M-19761691-2").build()
        nativeAdLoader.loadAd(adRequestConfiguration)
    }

    private fun getAdSize(activity: Activity): BannerAdSize {
        val displayMetrics = activity.resources.displayMetrics
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).roundToInt()
        return BannerAdSize.stickySize(activity, screenWidthDp)
    }

    // ActivityLifecycleCallbacks
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
