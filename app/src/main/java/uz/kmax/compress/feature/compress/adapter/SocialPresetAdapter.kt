package uz.kmax.compress.feature.compress.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import uz.kmax.compress.R
import uz.kmax.compress.core.social.SocialPreset
import uz.kmax.compress.databinding.ItemSocialPresetBinding

class SocialPresetAdapter(
    private val onPresetSelected: (SocialPreset) -> Unit
) : RecyclerView.Adapter<SocialPresetAdapter.PresetViewHolder>() {

    private val presets = SocialPreset.values().sortedBy { it.requiresPremium }
    private var selectedPreset: SocialPreset = SocialPreset.INSTAGRAM
    private var isPremium: Boolean = false

    fun setPremium(premium: Boolean) {
        if (isPremium != premium) {
            isPremium = premium
            notifyDataSetChanged()
        }
    }

    fun setSelected(preset: SocialPreset) {
        val oldIndex = presets.indexOf(selectedPreset)
        selectedPreset = preset
        val newIndex = presets.indexOf(preset)
        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        val binding = ItemSocialPresetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PresetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        holder.bind(presets[position])
    }

    override fun getItemCount(): Int = presets.size

    inner class PresetViewHolder(private val binding: ItemSocialPresetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(preset: SocialPreset) {
            binding.apply {
                tvTitle.setText(preset.titleRes)
                tvSubtitle.setText(preset.descriptionRes)
                ivIcon.setImageResource(preset.iconRes)
                ivIcon.contentDescription = root.context.getString(preset.titleRes)
                radioButton.isChecked = preset == selectedPreset
                
                val showLock = preset.requiresPremium && !isPremium
                radioButton.visibility = if (showLock) View.GONE else View.VISIBLE
                ivLock.visibility = if (showLock) View.VISIBLE else View.GONE
                
                root.setOnClickListener {
                    onPresetSelected(preset)
                }
                
                // Visual feedback for selection
                cardSocial.strokeWidth = if (preset == selectedPreset) 4 else 1
                cardSocial.strokeColor = if (preset == selectedPreset)
                    MaterialColors.getColor(root, R.attr.lumoraPrimary)
                else MaterialColors.getColor(root, R.attr.lumoraOutline)
                
                cardSocial.alpha = if (preset.requiresPremium) 0.8f else 1.0f
            }
        }
    }
}
