package uz.kmax.compress.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import uz.kmax.compress.databinding.DialogLoadingBinding

class LoadingDialog : DialogFragment() {

    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingView.setMessage(message)
    }

    fun setMessage(message: String?) {
        this.message = message
        if (_binding != null) {
            binding.loadingView.setMessage(message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LoadingDialog"
        fun newInstance(message: String? = null) = LoadingDialog().apply {
            setMessage(message)
        }
    }
}