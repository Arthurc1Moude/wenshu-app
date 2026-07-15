package com.wenshu.app.ui.adapters

import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.databinding.ItemPublishVideoBinding
import java.io.File
import java.util.Locale

data class PendingVideo(
    val path: String,
    val filename: String,
    val size: Long,
    val duration: Long = 0
) {
    val displaySize: String get() = formatSize(size)

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${String.format(Locale.US, "%.1f", bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${String.format(Locale.US, "%.1f", bytes / (1024.0 * 1024))} MB"
            else -> "${String.format(Locale.US, "%.2f", bytes / (1024.0 * 1024 * 1024))} GB"
        }
    }
}

class PublishVideoAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<PublishVideoAdapter.VideoViewHolder>() {

    private val videos = mutableListOf<PendingVideo>()

    inner class VideoViewHolder(val binding: ItemPublishVideoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemPublishVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        with(holder.binding) {
            tvVideoName.text = video.filename
            tvVideoInfo.text = video.displaySize

            try {
                val uri: Uri = if (video.path.startsWith("content://")) {
                    Uri.parse(video.path)
                } else {
                    Uri.fromFile(File(video.path))
                }
                Glide.with(imgVideoThumb.context)
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.color.divider)
                    .error(R.color.divider)
                    .into(imgVideoThumb)
            } catch (e: Exception) {
                imgVideoThumb.setImageResource(R.color.divider)
            }

            btnRemoveVideo.setOnClickListener {
                onRemoveClick(holder.bindingAdapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = videos.size

    fun getVideos(): List<PendingVideo> = videos.toList()

    fun addVideo(path: String, filename: String, size: Long) {
        videos.add(PendingVideo(path, filename, size))
        notifyItemInserted(videos.size - 1)
    }

    fun removeVideo(position: Int) {
        if (position in videos.indices) {
            videos.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }

    fun setVideos(newVideos: List<PendingVideo>) {
        videos.clear()
        videos.addAll(newVideos)
        notifyDataSetChanged()
    }

    fun clear() {
        val count = videos.size
        videos.clear()
        notifyItemRangeRemoved(0, count)
    }
}
