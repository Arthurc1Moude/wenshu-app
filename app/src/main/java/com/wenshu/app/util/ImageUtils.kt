package com.wenshu.app.util

object ImageUtils {

    private const val BASE_URL = "https://wenshu-server.onrender.com"

    fun normalizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> "$BASE_URL$url"
            else -> "$BASE_URL/$url"
        }
    }
}
