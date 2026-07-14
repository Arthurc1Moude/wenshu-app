package com.wenshu.app.ui.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.Comment
import com.wenshu.app.databinding.ItemCommentBinding
import com.wenshu.app.util.ImageUtils
import com.wenshu.app.util.LinkifyUtils
import com.wenshu.app.util.TimeUtils

class CommentAdapter(
    private val onLikeClick: (Comment) -> Unit,
    private val onReplyClick: (Comment) -> Unit,
    private val onUserClick: (Comment) -> Unit,
    private val onLoadReplies: ((parentComment: Comment) -> Unit)? = null
) : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val expandedComments = mutableSetOf<String>()
    private val replyExpansionLevels = mutableMapOf<String, Int>()
    private val repliesMap = mutableMapOf<String, List<Comment>>()

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        with(holder.binding) {
            tvUsername.text = comment.author?.displayName ?: "用户"

            val displayContent = if (comment.isReply && comment.replyToName != null) {
                "回复 @${comment.replyToName}: ${comment.content}"
            } else {
                comment.content
            }
            LinkifyUtils.setupClickableLinks(tvContent, root.context, displayContent)
            tvTime.text = TimeUtils.formatRelativeTime(comment.createdAt)
            tvLikeCount.text = if (comment.likeCount > 0) comment.likeCount.toString() else ""

            Glide.with(imgAvatar.context)
                .load(ImageUtils.normalizeUrl(comment.author?.avatar))
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .circleCrop()
                .into(imgAvatar)

            imgLike.setImageResource(if (comment.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            imgLike.setColorFilter(
                root.context.getColor(if (comment.isLiked) R.color.seal else R.color.text_hint)
            )

            layoutLike.setOnClickListener { onLikeClick(comment) }
            tvReply.setOnClickListener { onReplyClick(comment) }
            imgAvatar.setOnClickListener { onUserClick(comment) }
            tvUsername.setOnClickListener { onUserClick(comment) }

            root.setOnClickListener { onReplyClick(comment) }

            val replies = repliesMap[comment.id]
            if (replies.isNullOrEmpty()) {
                layoutReplies.visibility = View.GONE
                btnExpandReplies.visibility = View.GONE
            } else {
                renderReplies(layoutReplies, btnExpandReplies, comment, replies)
            }
        }
    }

    private fun renderReplies(
        container: LinearLayout,
        expandBtn: TextView,
        parentComment: Comment,
        allReplies: List<Comment>
    ) {
        container.removeAllViews()
        val context = container.context
        val expansionLevel = replyExpansionLevels[parentComment.id] ?: 0
        val showCount = when (expansionLevel) {
            0 -> 1
            1 -> 50
            2 -> 100
            else -> allReplies.size
        }
        val visibleReplies = allReplies.take(showCount)
        val hasMore = allReplies.size > showCount

        container.visibility = View.VISIBLE

        visibleReplies.forEach { reply ->
            val replyView = LayoutInflater.from(context)
                .inflate(R.layout.item_reply, container, false)
            val imgAvatar = replyView.findViewById<ImageView>(R.id.img_avatar)
            val tvContent = replyView.findViewById<TextView>(R.id.tv_content)
            val tvUsername = replyView.findViewById<TextView>(R.id.tv_username)
            val tvTime = replyView.findViewById<TextView>(R.id.tv_time)
            val imgLike = replyView.findViewById<ImageView>(R.id.img_like)
            val tvLikeCount = replyView.findViewById<TextView>(R.id.tv_like_count)
            val layoutLike = replyView.findViewById<LinearLayout>(R.id.layout_like)

            val replyText = if (reply.isReply && reply.replyToName != null) {
                "@${reply.replyToName} ${reply.content}"
            } else {
                reply.content
            }
            LinkifyUtils.setupClickableLinks(tvContent, context, replyText)
            tvUsername.text = reply.author?.displayName ?: "用户"
            tvTime.text = TimeUtils.formatRelativeTime(reply.createdAt)
            tvLikeCount.text = if (reply.likeCount > 0) reply.likeCount.toString() else ""

            Glide.with(imgAvatar.context)
                .load(ImageUtils.normalizeUrl(reply.author?.avatar))
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .circleCrop()
                .into(imgAvatar)

            imgLike.setImageResource(if (reply.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            imgLike.setColorFilter(
                context.getColor(if (reply.isLiked) R.color.seal else R.color.text_hint)
            )

            layoutLike.setOnClickListener { onLikeClick(reply) }
            replyView.setOnClickListener { onReplyClick(reply) }
            imgAvatar.setOnClickListener { onUserClick(reply) }
            tvUsername.setOnClickListener { onUserClick(reply) }

            container.addView(replyView)
        }

        if (hasMore) {
            expandBtn.visibility = View.VISIBLE
            val remaining = allReplies.size - showCount
            expandBtn.text = when (expansionLevel) {
                0 -> "展开更多回复 ($remaining)"
                1 -> "继续展开 ($remaining)"
                2 -> "查看全部回复"
                else -> ""
            }
            expandBtn.setOnClickListener {
                replyExpansionLevels[parentComment.id] = expansionLevel + 1
                notifyItemChanged(currentList.indexOf(parentComment))
            }
        } else {
            if (expansionLevel > 0) {
                expandBtn.visibility = View.VISIBLE
                expandBtn.text = "收起"
                expandBtn.setOnClickListener {
                    replyExpansionLevels[parentComment.id] = 0
                    notifyItemChanged(currentList.indexOf(parentComment))
                }
            } else {
                expandBtn.visibility = View.GONE
            }
        }
    }

    fun setReplies(commentId: String, replies: List<Comment>) {
        repliesMap[commentId] = replies
        val index = currentList.indexOfFirst { it.id == commentId }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun addReply(commentId: String, reply: Comment) {
        val current = repliesMap[commentId]?.toMutableList() ?: mutableListOf()
        current.add(reply)
        repliesMap[commentId] = current
        val index = currentList.indexOfFirst { it.id == commentId }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    fun expandReplies(commentId: String) {
        expandedComments.add(commentId)
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
