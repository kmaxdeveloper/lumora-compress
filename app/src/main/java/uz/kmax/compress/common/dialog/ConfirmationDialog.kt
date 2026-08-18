package uz.kmax.compress.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import uz.kmax.compress.databinding.DialogConfirmationBinding

class ConfirmationDialog : DialogFragment() {

    private var _binding: DialogConfirmationBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var message: String = ""
    private var positiveText: String = "OK"
    private var negativeText: String = "Cancel"
    private var onPositive: (() -> Unit)? = null
    private var onNegative: (() -> Unit)? = null

    fun setup(
        title: String,
        message: String,
        positiveText: String = "OK",
        negativeText: String = "Cancel",
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null
    ) {
        this.title = title
        this.message = message
        this.positiveText = positiveText
        this.negativeText = negativeText
        this.onPositive = onPositive
        this.onNegative = onNegative
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = title
        binding.tvMessage.text = message
        binding.btnPositive.text = positiveText
        binding.btnNegative.text = negativeText

        binding.btnPositive.setOnClickListener {
            onPositive?.invoke()
            dismiss()
        }
        binding.btnNegative.setOnClickListener {
            onNegative?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ConfirmationDialog"
    }
}