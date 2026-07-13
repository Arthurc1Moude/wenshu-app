package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.ChatMessage
import com.wenshu.app.databinding.ItemMessageReceivedBinding
import com.wenshu.app.databinding.ItemMessageSentBinding
import com.wenshu.app.util.ImageUtils

class ChatMessageAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2
    }

    inner class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    inner class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isMine) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReceivedViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentViewHolder -> bindSentMessage(holder.binding, message)
            is ReceivedViewHolder -> bindReceivedMessage(holder.binding, message)
        }
    }

    private fun bindSentMessage(binding: ItemMessageSentBinding, message: ChatMessage) {
        binding.tvMessage.text = message.content
    }

    private fun bindReceivedMessage(binding: ItemMessageReceivedBinding, message: ChatMessage) {
        binding.tvMessage.text = message.content
        val name = message.senderName
        if (!name.isNullOrEmpty()) {
            binding.tvUsername.visibility = android.view.View.VISIBLE
            binding.tvUsername.text = "@$name"
        } else {
            binding.tvUsername.visibility = android.view.View.GONE
        }
        Glide.with(binding.imgAvatar.context)
            .load(ImageUtils.normalizeUrl(message.senderAvatar))
            .placeholder(R.drawable.bg_avatar_placeholder)
            .error(R.drawable.bg_avatar_placeholder)
            .circleCrop()
            .into(binding.imgAvatar)
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
