package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import uz.kmax.compress.databinding.ViewErrorStateBinding

class ErrorStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewErrorStateBinding.inflate(LayoutInflater.from(context), this, true)

    fun setState(
        @DrawableRes icon: Int,
        title: String,
        description: String,
        onRetry: () -> Unit
    ) {
        binding.ivIcon.setImageResource(icon)
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.btnRetry.setOnClickListener { onRetry() }
    }
}