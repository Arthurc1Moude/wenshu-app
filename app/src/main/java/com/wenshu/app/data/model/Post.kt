package com.wenshu.app.data.model

import java.io.Serializable

data class FileAttachment(
    val id: String = "",
    val url: String = "",
    val originalName: String = "",
    val ext: String = "",
    val size: Long = 0,
    val sizeFormatted: String = "",
    val iconType: String = "unknown",
    val expiresAt: Long? = null,
    val isPermanent: Boolean = false
) : Serializable

data class MediaItem(
    val url: String = "",
    val type: String = "image",
    val thumbnailUrl: String? = null
) : Serializable

data class UrlPreview(
    val url: String = "",
    val title: String = "",
    val favicon: String? = null,
    val description: String? = null
) : Serializable

data class Post(
    val id: String,
    val authorId: String? = null,
    val content: String = "",
    val title: String = "",
    val images: List<String> = emptyList(),
    val media: List<MediaItem> = emptyList(),
    val files: List<FileAttachment> = emptyList(),
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val collectCount: Int = 0,
    val coinCount: Int = 0,
    val createdAt: Long = 0,
    val author: User? = null,
    val isLiked: Boolean = false,
    val isCollected: Boolean = false,
    val isTipped: Boolean = false,
    val isLongText: Boolean = false,
    val location: String? = null,
    val urlPreviews: List<UrlPreview> = emptyList()
) {
    val firstImage: String? get() = images.firstOrNull() ?: media.firstOrNull { it.type == "image" }?.url
    val imageCount: Int get() = images.size + media.count { it.type == "image" || it.type == "gif" }
    val hasVideo: Boolean get() = media.any { it.type == "video" }
    val displayTitle: String get() = title.ifBlank {
        val firstLine = content.substringBefore("\n").trimStart('#', ' ')
        if (firstLine.length > 50) firstLine.take(50) + "..." else firstLine
    }
    val displayContent: String get() {
        if (title.isNotBlank()) return content
        val lines = content.split("\n")
        if (lines.size <= 1) return content
        val firstLine = lines[0].trim()
        return if (firstLine.startsWith("#")) {
            lines.drop(1).joinToString("\n").trimStart('\n', ' ')
        } else {
            content
        }
    }
}
