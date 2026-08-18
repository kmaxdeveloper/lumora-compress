package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import uz.kmax.compress.databinding.ViewPremiumBadgeBinding

class PremiumBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        ViewPremiumBadgeBinding.inflate(LayoutInflater.from(context), this, true)
    }
}