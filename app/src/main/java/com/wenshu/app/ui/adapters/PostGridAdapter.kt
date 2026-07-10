package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.Post
import com.wenshu.app.databinding.ItemPostGridBinding
import com.wenshu.app.util.ImageUtils

class PostGridAdapter(
    private val onPostClick: (Post) -> Unit
) : ListAdapter<Post, PostGridAdapter.GridViewHolder>(PostDiffCallback()) {

    inner class GridViewHolder(val binding: ItemPostGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemPostGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val post = getItem(position)
        with(holder.binding) {
            tvTitle.text = post.titlePreview
            tvLikeCount.text = post.likeCount.toString()
            val firstImage = ImageUtils.normalizeUrl(post.firstImage)
            if (firstImage != null) {
                imgCover.visibility = View.VISIBLE
                Glide.with(imgCover.context)
                    .load(firstImage)
                    .placeholder(R.color.paper)
                    .error(R.color.paper)
                    .centerCrop()
                    .into(imgCover)
            } else {
                imgCover.visibility = View.GONE
            }
            root.setOnClickListener { onPostClick(post) }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
