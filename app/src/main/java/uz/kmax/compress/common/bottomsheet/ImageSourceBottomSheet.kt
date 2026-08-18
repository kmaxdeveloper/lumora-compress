package uz.kmax.compress.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.kmax.compress.databinding.BottomSheetImageSourceBinding

class ImageSourceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetImageSourceBinding? = null
    private val binding get() = _binding!!

    private var onSourceSelected: ((Source) -> Unit)? = null

    enum class Source { GALLERY, CAMERA }

    fun setListener(listener: (Source) -> Unit) {
        onSourceSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetImageSourceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGallery.setOnClickListener {
            onSourceSelected?.invoke(Source.GALLERY)
            dismiss()
        }
        binding.btnCamera.setOnClickListener {
            onSourceSelected?.invoke(Source.CAMERA)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ImageSourceBottomSheet"
        fun newInstance() = ImageSourceBottomSheet()
    }
}