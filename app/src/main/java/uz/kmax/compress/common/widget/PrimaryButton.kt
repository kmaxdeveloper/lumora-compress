package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import uz.kmax.compress.R

class PrimaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {
    init {
        // Primary styling is handled by the default materialButtonStyle in the theme,
        // which we mapped to Widget.Lumora.Button in themes.xml.
    }
}