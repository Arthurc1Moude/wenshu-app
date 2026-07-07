package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.Comment
import com.wenshu.app.databinding.ItemCommentBinding
import com.wenshu.app.databinding.ItemCommentReplyBinding
import com.wenshu.app.util.TimeUtils

class CommentAdapter(
    private var comments: List<Comment> = emptyList(),
    private val onLikeClick: (Comment) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val onUserClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        with(holder.binding) {
            tvUsername.text = comment.author.nickname
            tvContent.text = comment.content
            tvTime.text = TimeUtils.formatRelativeTime(comment.createdAt)
            tvLikeCount.text = if (comment.likeCount > 0) TimeUtils.formatCount(comment.likeCount) else ""

            Glide.with(imgAvatar.context)
                .load(comment.author.avatarUrl)
                .placeholder(R.drawable.bg_circle_placeholder)
                .error(R.drawable.bg_circle_placeholder)
                .circleCrop()
                .into(imgAvatar)

            imgLike.setImageResource(if (comment.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            imgLike.setColorFilter(
                root.context.getColor(if (comment.isLiked) R.color.like_red else R.color.text_hint)
            )

            if (comment.replies.isNotEmpty()) {
                layoutReplies.visibility = View.VISIBLE
                layoutReplies.removeAllViews()
                comment.replies.forEach { reply ->
                    val replyBinding = ItemCommentReplyBinding.inflate(
                        LayoutInflater.from(root.context), layoutReplies, false
                    )
                    replyBinding.tvUsername.text = reply.author.nickname
                    replyBinding.tvUsername.setTextColor(root.context.getColor(R.color.text_secondary))
                    val replyText = if (reply.replyToUser != null) {
                        "回复 ${reply.replyToUser}: ${reply.content}"
                    } else {
                        reply.content
                    }
                    replyBinding.tvContent.text = replyText
                    layoutReplies.addView(replyBinding.root)
                }
            } else {
                layoutReplies.visibility = View.GONE
            }

            layoutLike.setOnClickListener { onLikeClick(comment) }
            tvReply.setOnClickListener { onReplyClick(comment) }
            imgAvatar.setOnClickListener { onUserClick(comment) }
            tvUsername.setOnClickListener { onUserClick(comment) }
        }
    }

    override fun getItemCount() = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}
