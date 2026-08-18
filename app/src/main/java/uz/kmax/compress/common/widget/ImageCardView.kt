package uz.kmax.compress.common.widget

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import uz.kmax.compress.databinding.ViewImageCardBinding

class ImageCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewImageCardBinding.inflate(LayoutInflater.from(context), this, true)

    fun setImage(uri: Uri) {
        binding.ivImage.setImageURI(uri)
    }

    fun setOverlayInfo(info: String) {
        binding.tvOverlayInfo.text = info
    }
}