package uz.kmax.compress.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import uz.kmax.compress.databinding.ViewFileInfoBinding

class FileInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewFileInfoBinding.inflate(LayoutInflater.from(context), this, true)

    fun setInfo(name: String, size: String) {
        binding.tvFileName.text = name
        binding.tvFileSize.text = size
//        binding.tvFileType.text = type
//        binding.tvFilePath.text = path
    }
}