package com.wenshu.app.data.model

data class Post(
    val id: String,
    val author: User,
    val title: String,
    val content: String,
    val coverImageUrl: String,
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val videoDuration: Long? = null,
    val isVideo: Boolean = false,
    val tags: List<String> = emptyList(),
    val location: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val collectCount: Int = 0,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val isFollowed: Boolean = false,
    val category: String = "recommend",
    val coverWidth: Int = 0,
    val coverHeight: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
