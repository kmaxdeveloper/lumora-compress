package uz.kmax.compress.common.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.kmax.compress.databinding.BottomSheetPremiumPaywallBinding

class PremiumPaywallBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPremiumPaywallBinding? = null
    private val binding get() = _binding!!

    private var onUpgradeClicked: (() -> Unit)? = null

    fun setOnUpgradeClickListener(listener: () -> Unit) {
        onUpgradeClicked = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPremiumPaywallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.benefit1.tvBenefit.text = getString(uz.kmax.compress.R.string.benefit_ads)
        binding.benefit2.tvBenefit.text = getString(uz.kmax.compress.R.string.benefit_batch)
        binding.benefit3.tvBenefit.text = getString(uz.kmax.compress.R.string.target_size)

        binding.btnUpgrade.setOnClickListener {
            onUpgradeClicked?.invoke()
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
        const val TAG = "PremiumPaywallBottomSheet"
        fun newInstance() = PremiumPaywallBottomSheet()
    }
}
