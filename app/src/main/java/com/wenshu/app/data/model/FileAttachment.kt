package com.wenshu.app.data.model

import com.google.gson.annotations.SerializedName

data class FileAttachment(
    val id: String,
    val filename: String,
    val size: Long = 0,
    val mimeType: String = "application/octet-stream",
    val url: String = "",
    val expiresAt: Long? = null,
    val isPermanent: Boolean = false,
    val downloadCount: Int = 0,
    val createdAt: Long = 0
) {
    val displaySize: String get() = formatSize(size)

    val extension: String get() {
        val dotIdx = filename.lastIndexOf('.')
        return if (dotIdx >= 0) filename.substring(dotIdx + 1).uppercase() else ""
    }

    val fileTypeCategory: FileType get() = when {
        mimeType.startsWith("image/") -> FileType.IMAGE
        mimeType.startsWith("video/") -> FileType.VIDEO
        mimeType.startsWith("audio/") -> FileType.AUDIO
        mimeType == "application/pdf" -> FileType.PDF
        mimeType.contains("word") || mimeType.contains("document") -> FileType.DOC
        mimeType.contains("excel") || mimeType.contains("spreadsheet") -> FileType.EXCEL
        mimeType.contains("powerpoint") || mimeType.contains("presentation") -> FileType.PPT
        mimeType == "application/zip" || mimeType == "application/x-rar-compressed" -> FileType.ARCHIVE
        mimeType.startsWith("text/") -> FileType.TEXT
        else -> FileType.UNKNOWN
    }

    val expireText: String get() {
        if (isPermanent) return "永久保存"
        if (expiresAt == null) return ""
        val now = System.currentTimeMillis()
        val diff = expiresAt - now
        val days = diff / (24 * 60 * 60 * 1000)
        val hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
        return when {
            diff <= 0 -> "已过期"
            days > 0 -> "${days}天后过期"
            hours > 0 -> "${hours}小时后过期"
            else -> "即将过期"
        }
    }

    companion object {
        fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "${bytes}B"
                bytes < 1024 * 1024 -> String.format("%.1fKB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format("%.1fMB", bytes / (1024.0 * 1024.0))
                else -> String.format("%.2fGB", bytes / (1024.0 * 1024.0 * 1024.0))
            }
        }
    }
}

enum class FileType {
    IMAGE, VIDEO, AUDIO, PDF, DOC, EXCEL, XLS, PPT, ARCHIVE, TEXT, MARKDOWN, CODE, UNKNOWN
}

data class VideoAttachment(
    val url: String,
    val thumbnail: String? = null,
    val duration: Long? = null,
    val width: Int = 0,
    val height: Int = 0
)

data class UrlPreview(
    val url: String,
    val title: String = "",
    val description: String = "",
    val favicon: String? = null,
    val siteName: String = "",
    val fetchedAt: Long = 0
)

data class UploadResponse(
    val id: String? = null,
    val url: String,
    val filename: String? = null,
    val size: Long = 0,
    val mimeType: String? = null,
    val expiresAt: Long? = null,
    val isPermanent: Boolean = false,
    val thumbnail: String? = null
)

data class UrlPreviewRequest(val url: String)
data class ReportRequest(val targetType: String, val targetId: String, val reason: String = "")
