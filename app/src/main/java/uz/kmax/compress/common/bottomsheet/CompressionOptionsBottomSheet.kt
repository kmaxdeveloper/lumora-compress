package uz.kmax.compress.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.kmax.compress.R
import uz.kmax.compress.databinding.BottomSheetCompressionOptionsBinding

class CompressionOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCompressionOptionsBinding? = null
    private val binding get() = _binding!!

    private var onApply: ((Int, String) -> Unit)? = null

    fun setListener(listener: (Int, String) -> Unit) {
        onApply = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCompressionOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnApply.setOnClickListener {
            val quality = binding.sliderQuality.value.toInt()
            val format = when (binding.toggleGroupFormat.checkedButtonId) {
                R.id.btnPng -> "PNG"
                R.id.btnWebp -> "WEBP"
                else -> "JPEG"
            }
            onApply?.invoke(quality, format)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CompressionOptionsBottomSheet"
        fun newInstance() = CompressionOptionsBottomSheet()
    }
}