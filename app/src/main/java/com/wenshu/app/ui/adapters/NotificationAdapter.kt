package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.NotificationItem
import com.wenshu.app.databinding.ItemNotificationBinding
import com.wenshu.app.util.ImageUtils

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationItem, NotificationAdapter.NotifViewHolder>(NotifDiffCallback()) {

    inner class NotifViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotifViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = getItem(position)
        with(holder.binding) {
            tvContent.text = notif.getTypeString()
            tvTime.text = notif.timeText
            dotUnread.visibility = if (notif.isRead) View.GONE else View.VISIBLE
            imgPostCover.visibility = View.GONE

            val systemTypes = setOf("system", "vip", "redeem_success", "signin", "reward")
            val isSystemNotification = notif.type in systemTypes
            val isTipNotification = notif.type == "tip"

            if (isSystemNotification) {
                tvUsername.visibility = View.GONE
                imgAvatar.setPadding(16, 16, 16, 16)
                imgAvatar.setBackgroundResource(R.color.paper)
                imgAvatar.setImageResource(R.drawable.ic_settings)
            } else if (isTipNotification) {
                if (notif.fromUser != null) {
                    tvUsername.visibility = View.VISIBLE
                    tvUsername.text = "@${notif.fromUser.username}"
                } else {
                    tvUsername.visibility = View.GONE
                }
                imgAvatar.setPadding(0, 0, 0, 0)
                imgAvatar.setBackgroundResource(R.drawable.bg_coin_filled)
                imgAvatar.setImageDrawable(null)
            } else {
                if (notif.fromUser != null) {
                    tvUsername.visibility = View.VISIBLE
                    tvUsername.text = "@${notif.fromUser.username}"
                } else {
                    tvUsername.visibility = View.GONE
                }
                imgAvatar.setPadding(0, 0, 0, 0)
                imgAvatar.setBackgroundResource(R.drawable.bg_avatar_placeholder)
                notif.fromUser?.let { user ->
                    Glide.with(imgAvatar.context)
                        .load(ImageUtils.normalizeUrl(user.avatar))
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .centerCrop()
                        .into(imgAvatar)
                }
            }

            root.setOnClickListener { onItemClick(notif) }
        }
    }

    class NotifDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}
