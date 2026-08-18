package uz.kmax.compress.feature.history.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import uz.kmax.compress.R
import uz.kmax.compress.databinding.ItemHistoryBinding
import uz.kmax.compress.feature.history.model.HistoryUiModel

class HistoryPagingAdapter(
    private val onFavoriteClick: (HistoryUiModel) -> Unit,
    private val onShareClick: (HistoryUiModel) -> Unit,
    private val onItemClick: (HistoryUiModel) -> Unit,
    private val onItemLongClick: (HistoryUiModel) -> Unit
) : PagingDataAdapter<HistoryUiModel, HistoryPagingAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryUiModel) {
            binding.apply {
                ivThumbnail.load(item.compressedUri) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(24f))
                }
                tvFileName.text = item.compressedUri.lastPathSegment
                tvResolution.text = item.resolution
                
                tvSizeOriginal.text = item.originalSize
                tvSizeOriginal.paintFlags = tvSizeOriginal.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                
                tvSizeCompressed.text = item.compressedSize
                tvSavings.text = item.savedPercent
                tvDate.text = item.date
                
                ivFavorite.setImageResource(
                    if (item.favorite) R.drawable.ic_favorite 
                    else R.drawable.ic_favorite_border
                )

                root.setOnClickListener { onItemClick(item) }
                root.setOnLongClickListener {
                    onItemLongClick(item)
                    true
                }
                ivFavorite.setOnClickListener { onFavoriteClick(item) }
                ivShare.setOnClickListener { onShareClick(item) }
                
                cardContainer.isChecked = item.isSelected
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<HistoryUiModel>() {
        override fun areItemsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HistoryUiModel, newItem: HistoryUiModel): Boolean =
            oldItem == newItem
    }
}
