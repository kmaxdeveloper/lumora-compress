package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import uz.kmax.compress.databinding.ViewCompressionResultCardBinding

class CompressionResultCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCompressionResultCardBinding.inflate(LayoutInflater.from(context), this, true)

    fun setResults(sizeBefore: String, sizeAfter: String, percentage: String) {
        binding.tvSizeBefore.text = sizeBefore
        binding.tvSizeAfter.text = sizeAfter
        binding.tvPercentage.text = percentage
    }
}