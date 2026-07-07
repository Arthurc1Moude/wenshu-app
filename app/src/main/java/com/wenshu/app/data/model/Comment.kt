package com.wenshu.app.data.model

data class Comment(
    val id: String,
    val postId: String,
    val author: User,
    val content: String,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false,
    val parentId: String? = null,
    val replyToUser: String? = null,
    val replies: List<Comment> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
