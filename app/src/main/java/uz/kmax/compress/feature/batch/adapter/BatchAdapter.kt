package uz.kmax.compress.feature.batch.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.color.MaterialColors
import uz.kmax.compress.R
import uz.kmax.compress.databinding.ItemBatchQueueBinding
import uz.kmax.compress.feature.batch.model.BatchItemUiModel

class BatchAdapter : ListAdapter<BatchItemUiModel, BatchAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBatchQueueBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBatchQueueBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BatchItemUiModel) {
            binding.apply {
                ivThumbnail.load(item.uri) {
                    crossfade(true)
                }
                tvFileName.text = item.name
                progressItem.progress = item.progress
                tvStatus.text = when (item.status) {
                    BatchItemUiModel.Status.QUEUED -> "Queued"
                    BatchItemUiModel.Status.PROCESSING -> "Processing... ${item.progress}%"
                    BatchItemUiModel.Status.COMPLETED -> "Finished ✓"
                    BatchItemUiModel.Status.FAILED -> "Failed ✗"
                }
                
                tvStatus.setTextColor(
                    if (item.status == BatchItemUiModel.Status.COMPLETED)
                        MaterialColors.getColor(binding.root, R.attr.lumoraSecondary)
                    else if (item.status == BatchItemUiModel.Status.FAILED)
                        MaterialColors.getColor(binding.root, R.attr.lumoraError)
                    else
                        MaterialColors.getColor(binding.root, R.attr.lumoraOnSurfaceVariant)
                )

                ivStatusIcon.apply {
                    visibility = if (item.status == BatchItemUiModel.Status.COMPLETED || item.status == BatchItemUiModel.Status.FAILED) View.VISIBLE else View.GONE
                    setImageResource(if (item.status == BatchItemUiModel.Status.COMPLETED) R.drawable.ic_check else R.drawable.ic_warning_amber) 
                    imageTintList = android.content.res.ColorStateList.valueOf(
                        if (item.status == BatchItemUiModel.Status.COMPLETED)
                            MaterialColors.getColor(binding.root, R.attr.lumoraSecondary)
                        else
                            MaterialColors.getColor(binding.root, R.attr.lumoraError)
                    )
                }

                root.strokeWidth = if (item.status == BatchItemUiModel.Status.PROCESSING) 4 else 0
                root.setCardBackgroundColor(
                    if (item.status == BatchItemUiModel.Status.COMPLETED) 
                        MaterialColors.getColor(binding.root, R.attr.lumoraPrimaryContainer)
                    else 
                        MaterialColors.getColor(binding.root, R.attr.lumoraSurfaceVariant)
                )
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<BatchItemUiModel>() {
        override fun areItemsTheSame(oldItem: BatchItemUiModel, newItem: BatchItemUiModel): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BatchItemUiModel, newItem: BatchItemUiModel): Boolean =
            oldItem == newItem
    }
}
