package uz.kmax.compress.common.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.yandex.mobile.ads.nativeads.NativeAd
import com.yandex.mobile.ads.nativeads.NativeAdView
import uz.kmax.compress.databinding.ViewYandexNativeAdBinding

class YandexNativeAdComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewYandexNativeAdBinding

    init {
        val visualContext = findActivity(context) ?: context
        binding = ViewYandexNativeAdBinding.inflate(LayoutInflater.from(visualContext), this, true)
    }

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    fun setNativeAd(ad: NativeAd) {
        val adView = binding.root as NativeAdView
        
        try {
            val viewBinder = com.yandex.mobile.ads.nativeads.NativeAdViewBinder.Builder(adView)
                .setBodyView(binding.adBody)
                .setCallToActionView(binding.adCallToAction)
                .setIconView(binding.adAppIcon)
                .setMediaView(binding.adMedia)
                .setSponsoredView(binding.adAttribution)
                .setTitleView(binding.adHeadline)
                .build()

            ad.bindNativeAd(viewBinder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
