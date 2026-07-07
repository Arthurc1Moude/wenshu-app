package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.model.NotificationItem
import com.wenshu.app.data.model.NotificationType
import com.wenshu.app.databinding.ItemNotificationBinding
import com.wenshu.app.util.TimeUtils

class NotificationAdapter(
    private var notifications: List<NotificationItem> = emptyList(),
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    inner class NotifViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotifViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = notifications[position]
        with(holder.binding) {
            tvContent.text = notif.content
            tvTime.text = TimeUtils.formatRelativeTime(notif.createdAt)
            dotUnread.visibility = if (notif.isRead) View.GONE else View.VISIBLE

            if (notif.type == NotificationType.SYSTEM) {
                imgAvatar.setImageResource(R.drawable.ic_settings)
                imgAvatar.setBackgroundResource(R.color.surface_variant)
                imgAvatar.setPadding(16, 16, 16, 16)
                imgPostCover.visibility = View.GONE
            } else {
                notif.user?.let { user ->
                    Glide.with(imgAvatar.context)
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .circleCrop()
                        .into(imgAvatar)
                }

                if (notif.postCoverUrl != null) {
                    imgPostCover.visibility = View.VISIBLE
                    Glide.with(imgPostCover.context)
                        .load(notif.postCoverUrl)
                        .placeholder(R.color.surface_variant)
                        .centerCrop()
                        .into(imgPostCover)
                } else {
                    imgPostCover.visibility = View.GONE
                }
            }

            root.setOnClickListener { onItemClick(notif) }
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifs: List<NotificationItem>) {
        notifications = newNotifs
        notifyDataSetChanged()
    }
}
