package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import uz.kmax.compress.databinding.ViewLoadingBinding

class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)

    fun setMessage(message: String?) {
        binding.tvMessage.apply {
            text = message
            isVisible = !message.isNullOrBlank()
        }
    }
}