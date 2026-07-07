package com.wenshu.app.ui.adapters

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wenshu.app.R
import com.wenshu.app.data.model.Post
import com.wenshu.app.databinding.ItemPostCardBinding
import com.wenshu.app.util.TimeUtils

class PostCardAdapter(
    private var posts: List<Post> = emptyList(),
    private val onPostClick: (Post) -> Unit,
    private val onLikeClick: (Post) -> Unit,
    private val onUserClick: (Post) -> Unit
) : RecyclerView.Adapter<PostCardAdapter.PostViewHolder>() {

    inner class PostViewHolder(val binding: ItemPostCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        with(holder.binding) {
            tvTitle.text = post.title
            tvUsername.text = post.author.nickname
            tvLikeCount.text = TimeUtils.formatCount(post.likeCount.toLong())
            tvLikeCountOverlay.text = TimeUtils.formatCount(post.likeCount.toLong())

            Glide.with(imgCover.context)
                .load(post.coverImageUrl)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .placeholder(R.color.surface_variant)
                .into(imgCover)

            Glide.with(imgAvatar.context)
                .load(post.author.avatarUrl)
                .placeholder(R.drawable.bg_circle_placeholder)
                .error(R.drawable.bg_circle_placeholder)
                .into(imgAvatar)

            imgVerified.visibility = if (post.author.isVerified) View.VISIBLE else View.GONE

            val hasMultipleImages = post.imageUrls.size > 1
            layoutImgCount.visibility = if (hasMultipleImages) View.VISIBLE else View.GONE
            tvImgCount.text = post.imageUrls.size.toString()

            layoutVideoBadge.visibility = if (post.isVideo) View.VISIBLE else View.GONE

            imgLike.setImageResource(if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            imgLike.setColorFilter(root.context.getColor(if (post.isLiked) R.color.liked else R.color.text_secondary))

            root.setOnClickListener { onPostClick(post) }
            imgAvatar.setOnClickListener { onUserClick(post) }
            tvUsername.setOnClickListener { onUserClick(post) }
            layoutLike.setOnClickListener {
                toggleLikeAnimation(imgLike)
                onLikeClick(post)
            }

            root.alpha = 0f
            root.animate().cancel()
            root.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((position % 10) * 30L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun toggleLikeAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.3f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
