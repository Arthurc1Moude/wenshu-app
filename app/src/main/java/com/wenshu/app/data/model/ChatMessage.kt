package com.wenshu.app.data.model

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isMine: Boolean = false,
    val senderAvatar: String? = null,
    val senderName: String? = null
)
