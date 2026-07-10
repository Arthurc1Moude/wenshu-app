package com.wenshu.app.data.model

data class Comment(
    val id: String,
    val postId: String,
    val authorId: String? = null,
    val content: String = "",
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = 0,
    val replyToId: String? = null,
    val author: User? = null,
    val replyToUser: ReplyTarget? = null
) {
    val isReply: Boolean get() = replyToId != null
    val replyToName: String? get() = replyToUser?.username
}

data class ReplyTarget(
    val id: String,
    val username: String
)
