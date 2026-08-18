package uz.kmax.compress.feature.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import uz.kmax.compress.databinding.ItemRecentFileBinding
import uz.kmax.compress.feature.home.model.RecentFileUiModel

class RecentFilesAdapter(
    private val onItemClick: (RecentFileUiModel) -> Unit
) : ListAdapter<RecentFileUiModel, RecentFilesAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecentFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRecentFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecentFileUiModel) {
            binding.apply {
                ivThumbnail.load(item.uri) {
                    crossfade(true)
                }
                tvFileName.text = item.name
                tvFileSize.text = "${item.originalSize} → ${item.compressedSize}"
                tvReduction.text = item.reductionPercentage
                
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<RecentFileUiModel>() {
            override fun areItemsTheSame(oldItem: RecentFileUiModel, newItem: RecentFileUiModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: RecentFileUiModel, newItem: RecentFileUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
