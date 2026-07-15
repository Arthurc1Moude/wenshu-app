package com.wenshu.app.data.model

data class Post(
    val id: String,
    val authorId: String? = null,
    val content: String = "",
    val images: List<String> = emptyList(),
    val videos: List<VideoAttachment> = emptyList(),
    val files: List<FileAttachment> = emptyList(),
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val collectCount: Int = 0,
    val coinCount: Int = 0,
    val createdAt: Long = 0,
    val title: String = "",
    val location: String = "",
    val isLongPost: Boolean = false,
    val urlPreviews: List<UrlPreview> = emptyList(),
    val author: User? = null,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val isTipped: Boolean = false
) {
    val firstImage: String? get() = images.firstOrNull()
    val imageCount: Int get() = images.size
    val titlePreview: String get() = content.take(50).let { if (content.length > 50) "$it..." else it }
    val hasTitle: Boolean get() = title.isNotBlank()
    val hasMedia: Boolean get() = images.isNotEmpty() || videos.isNotEmpty()
}
