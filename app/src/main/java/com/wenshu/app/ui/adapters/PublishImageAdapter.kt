package com.wenshu.app.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.databinding.ItemPublishImageBinding
import java.io.File

class PublishImageAdapter(
    private val onAddClick: () -> Unit,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<PublishImageAdapter.ImageViewHolder>() {

    private val images = mutableListOf<String>()
    private val videoExtensions = setOf("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "3gp")
    private val maxImages = 9

    inner class ImageViewHolder(val binding: ItemPublishImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemPublishImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val itemWidth = (parent.width - parent.paddingStart - parent.paddingEnd - 32) / 3
        binding.root.layoutParams.width = itemWidth
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        with(holder.binding) {
            if (position < images.size) {
                val path = images[position]
                btnAdd.visibility = View.GONE
                btnRemove.visibility = View.VISIBLE
                imgPreview.visibility = View.VISIBLE

                val isVideo = isVideoFile(path)
                icPlayVideo.visibility = if (isVideo) View.VISIBLE else View.GONE

                try {
                    val uri: Uri = if (path.startsWith("http") || path.startsWith("content://")) {
                        Uri.parse(path)
                    } else {
                        Uri.fromFile(File(path))
                    }
                    Glide.with(imgPreview.context)
                        .load(if (isVideo) Uri.fromFile(File(path)) else uri)
                        .centerCrop()
                        .placeholder(R.color.paper)
                        .error(R.color.paper)
                        .into(imgPreview)
                } catch (e: Exception) {
                    imgPreview.setImageResource(R.color.paper)
                }

                btnRemove.setOnClickListener { onRemoveClick(holder.bindingAdapterPosition) }
            } else {
                imgPreview.setImageDrawable(null)
                imgPreview.visibility = View.GONE
                icPlayVideo.visibility = View.GONE
                btnRemove.visibility = View.GONE
                btnAdd.visibility = View.VISIBLE
                btnAdd.setOnClickListener { onAddClick() }
            }
        }
    }

    private fun isVideoFile(path: String): Boolean {
        val ext = path.substringAfterLast(".", "").lowercase()
        return videoExtensions.contains(ext)
    }

    override fun getItemCount(): Int {
        return if (images.size < maxImages) images.size + 1 else images.size
    }

    fun getImages(): List<String> = images.toList()

    fun addImage(path: String) {
        if (images.size < maxImages) {
            val insertPos = images.size
            images.add(path)
            notifyItemInserted(insertPos)
            if (images.size < maxImages) {
                notifyItemChanged(insertPos + 1)
            }
        }
    }

    fun removeImage(position: Int) {
        if (position in images.indices) {
            images.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }

    fun setImages(paths: List<String>) {
        images.clear()
        images.addAll(paths.take(maxImages))
        notifyDataSetChanged()
    }
}
