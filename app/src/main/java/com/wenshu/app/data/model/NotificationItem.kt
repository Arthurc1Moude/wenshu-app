package com.wenshu.app.data.model

enum class NotificationType {
    LIKE,
    COMMENT,
    COLLECT,
    FOLLOW,
    MENTION,
    SYSTEM
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val user: User? = null,
    val postId: String? = null,
    val postCoverUrl: String? = null,
    val content: String = "",
    val commentContent: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
