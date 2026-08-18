package uz.kmax.compress.common.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.DialogFragment
import uz.kmax.compress.databinding.DialogPermissionBinding

class PermissionDialog : DialogFragment() {

    private var _binding: DialogPermissionBinding? = null
    private val binding get() = _binding!!

    private var iconRes: Int = 0
    private var title: String = ""
    private var description: String = ""
    private var onGrant: (() -> Unit)? = null

    fun setup(
        @DrawableRes icon: Int,
        title: String,
        description: String,
        onGrant: () -> Unit
    ) {
        this.iconRes = icon
        this.title = title
        this.description = description
        this.onGrant = onGrant
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivPermissionIcon.setImageResource(iconRes)
        binding.tvTitle.text = title
        binding.tvDescription.text = description

        binding.btnGrant.setOnClickListener {
            onGrant?.invoke()
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PermissionDialog"
    }
}