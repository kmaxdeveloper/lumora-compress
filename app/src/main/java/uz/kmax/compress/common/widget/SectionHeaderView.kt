package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import uz.kmax.compress.databinding.ViewSectionHeaderBinding

class SectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSectionHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    fun setTitle(title: String) {
        binding.tvSectionTitle.text = title
    }

    fun setAction(text: String, onClick: () -> Unit) {
        binding.tvActionText.apply {
            isVisible = true
            this.text = text
            setOnClickListener { onClick() }
        }
    }
}