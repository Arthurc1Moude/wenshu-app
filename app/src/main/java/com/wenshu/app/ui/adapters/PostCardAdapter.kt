package com.wenshu.app.ui.adapters

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.wenshu.app.R
import com.wenshu.app.data.model.Post
import com.wenshu.app.databinding.ItemPostCardBinding
import com.wenshu.app.util.ImageUtils

class PostCardAdapter(
    private val onPostClick: (Post) -> Unit,
    private val onLikeClick: (Post) -> Unit,
    private val onCoinClick: (Post) -> Unit,
    private val onUserClick: (Post) -> Unit
) : ListAdapter<Post, PostCardAdapter.PostViewHolder>(PostDiffCallback()) {

    inner class PostViewHolder(val binding: ItemPostCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        with(holder.binding) {
            tvTitle.text = post.displayTitle
            tvUsername.text = post.author?.displayName ?: ""
            tvLikeCount.text = post.likeCount.toString()
            tvLikeCountOverlay.text = post.likeCount.toString()
            tvCoinCount.text = if (post.coinCount > 0) post.coinCount.toString() else ""

            val firstImage = ImageUtils.normalizeUrl(post.firstImage)
            if (firstImage != null) {
                imgCover.visibility = View.VISIBLE
                Glide.with(imgCover.context)
                    .load(firstImage)
                    .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .placeholder(R.color.paper)
                    .error(R.color.paper)
                    .into(imgCover)
            } else {
                imgCover.visibility = View.GONE
            }

            Glide.with(imgAvatar.context)
                .load(ImageUtils.normalizeUrl(post.author?.avatar))
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .centerCrop()
                .into(imgAvatar)

            imgVerified.visibility = if (post.author?.isVip == true) View.VISIBLE else View.GONE

            val hasMultipleImages = post.imageCount > 1
            layoutImgCount.visibility = if (hasMultipleImages) View.VISIBLE else View.GONE
            tvImgCount.text = post.imageCount.toString()

            layoutVideoBadge.visibility = View.GONE

            imgLike.setImageResource(if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            imgLike.setColorFilter(root.context.getColor(if (post.isLiked) R.color.seal else R.color.text_secondary))

            if (post.isTipped) {
                bgCoinIcon.setBackgroundResource(R.drawable.bg_coin_filled)
                tvCoinSymbol.setTextColor(ContextCompat.getColor(root.context, R.color.background))
            } else {
                bgCoinIcon.setBackgroundResource(R.drawable.bg_coin_outline)
                tvCoinSymbol.setTextColor(ContextCompat.getColor(root.context, R.color.ink))
            }
            tvCoinCount.setTextColor(ContextCompat.getColor(root.context,
                if (post.isTipped) R.color.ink else R.color.text_secondary))

            root.setOnClickListener { onPostClick(post) }
            imgAvatar.setOnClickListener { onUserClick(post) }
            tvUsername.setOnClickListener { onUserClick(post) }
            layoutLike.setOnClickListener {
                toggleLikeAnimation(imgLike)
                onLikeClick(post)
            }
            layoutCoin.setOnClickListener {
                toggleCoinAnimation(bgCoinIcon)
                onCoinClick(post)
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

    private fun toggleCoinAnimation(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.4f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.4f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
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
