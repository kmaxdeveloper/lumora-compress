package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcherOwner
import uz.kmax.compress.databinding.ViewToolbarBinding

class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    fun setup(
        title: String,
        showBackButton: Boolean = true,
        onBackClick: (() -> Unit)? = null
    ) {
        binding.toolbar.title = title
        if (showBackButton) {
            binding.toolbar.setNavigationOnClickListener {
                onBackClick?.invoke() ?: (context as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher?.onBackPressed()
            }
        } else {
            binding.toolbar.navigationIcon = null
        }
    }
}