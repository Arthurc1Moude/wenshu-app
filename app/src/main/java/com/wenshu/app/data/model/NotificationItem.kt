package com.wenshu.app.data.model

data class NotificationItem(
    val id: String,
    val userId: String? = null,
    val type: String = "",
    val fromUserId: String? = null,
    val fromUser: NotificationFromUser? = null,
    val postId: String? = null,
    val content: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0
) {
    val timeText: String get() = com.wenshu.app.util.TimeUtils.formatRelativeTime(createdAt)
    val isUnread: Boolean get() = !isRead

    fun getTypeString(): String = when (type) {
        "like" -> "赞了你的帖子"
        "comment" -> "评论/回复了你"
        "comment_like" -> "赞了你的评论"
        "follow" -> "关注了你"
        "tip" -> content.ifEmpty { "向你的帖子投入了文书币" }
        "vip" -> content.ifEmpty { "会员通知" }
        "redeem_success" -> content.ifEmpty { "兑换成功" }
        "signin" -> content.ifEmpty { "签到成功" }
        "reward" -> content.ifEmpty { "获得奖励" }
        "group_invite" -> content.ifEmpty { "邀请你加入群聊" }
        "group_join" -> content.ifEmpty { "加入了你的群聊" }
        "system" -> content.ifEmpty { "系统通知" }
        else -> content
    }
}

data class NotificationFromUser(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val isVip: Boolean = false,
    val vipLevel: Int = 0
)

data class NotificationsResponse(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0
)
