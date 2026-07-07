package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.Post
import com.wenshu.app.databinding.ItemPostGridBinding
import com.wenshu.app.util.TimeUtils

class PostGridAdapter(
    private var posts: List<Post> = emptyList(),
    private val onPostClick: (Post) -> Unit
) : RecyclerView.Adapter<PostGridAdapter.GridViewHolder>() {

    inner class GridViewHolder(val binding: ItemPostGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val binding = ItemPostGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val post = posts[position]
        with(holder.binding) {
            tvTitle.text = post.title
            tvLikeCount.text = TimeUtils.formatCount(post.likeCount)
            Glide.with(imgCover.context)
                .load(post.coverImageUrl)
                .placeholder(R.color.surface_variant)
                .centerCrop()
                .into(imgCover)
            root.setOnClickListener { onPostClick(post) }
        }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
