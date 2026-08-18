package uz.kmax.compress.common.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import uz.kmax.compress.databinding.ViewNativeAdBinding

class NativeAdComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewNativeAdBinding

    init {
        val visualContext = findActivity(context) ?: context
        binding = ViewNativeAdBinding.inflate(LayoutInflater.from(visualContext), this, true)
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

    fun setNativeAd(nativeAd: NativeAd) {
        val adView = binding.root as NativeAdView
        
        adView.headlineView = binding.adHeadline
        adView.bodyView = binding.adBody
        adView.callToActionView = binding.adCallToAction
        adView.iconView = binding.adAppIcon
        adView.mediaView = binding.adMedia

        binding.adHeadline.text = nativeAd.headline
        binding.adBody.text = nativeAd.body
        binding.adCallToAction.text = nativeAd.callToAction
        nativeAd.icon?.let {
            binding.adAppIcon.setImageDrawable(it.drawable)
        }
        
        try {
            adView.setNativeAd(nativeAd)
        } catch (e: Exception) {
            // Log or handle the exception to prevent crash
            e.printStackTrace()
        }
    }
}
