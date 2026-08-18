package uz.kmax.compress.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import uz.kmax.compress.R

class SecondaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {
    init {
        setBackgroundTintList(ColorStateList.valueOf(MaterialColors.getColor(this, R.attr.lumoraSecondary)))
        setTextColor(MaterialColors.getColor(this, R.attr.lumoraOnSecondary))
    }
}
