package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.databinding.ItemImageViewerBinding
import com.wenshu.app.util.ImageUtils

class ImagePagerAdapter(
    private var images: List<String> = emptyList()
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemImageViewerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageViewerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = ImageUtils.normalizeUrl(images[position])
        Glide.with(holder.binding.imgContent.context)
            .load(imageUrl)
            .placeholder(R.color.paper)
            .error(R.color.paper)
            .fitCenter()
            .into(holder.binding.imgContent)
    }

    override fun getItemCount() = images.size

    fun updateImages(newImages: List<String>) {
        images = newImages
        notifyDataSetChanged()
    }
}
