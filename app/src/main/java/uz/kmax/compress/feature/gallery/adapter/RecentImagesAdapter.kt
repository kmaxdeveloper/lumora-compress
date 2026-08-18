package uz.kmax.compress.feature.gallery.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import uz.kmax.compress.databinding.ItemGalleryRecentBinding
import uz.kmax.compress.feature.gallery.model.GalleryImageUiModel

class RecentImagesAdapter(
    private val onItemClick: (GalleryImageUiModel) -> Unit
) : ListAdapter<GalleryImageUiModel, RecentImagesAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryRecentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemGalleryRecentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GalleryImageUiModel) {
            binding.apply {
                ivThumbnail.load(item.uri) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(
                        radius = binding.root.context.resources.getDimension(uz.kmax.compress.R.dimen.radius_24)
                    ))
                }
                tvResolution.text = binding.root.context.getString(
                    uz.kmax.compress.R.string.resolution_placeholder,
                    item.width,
                    item.height
                )
                tvSize.text = item.size

                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<GalleryImageUiModel>() {
        override fun areItemsTheSame(oldItem: GalleryImageUiModel, newItem: GalleryImageUiModel): Boolean {
            return oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: GalleryImageUiModel, newItem: GalleryImageUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
