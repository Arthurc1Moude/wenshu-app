package com.wenshu.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val username: String,
    val avatar: String? = null,
    val cover: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val wenshuCoin: Int = 0,
    val isVip: Boolean = false,
    val vipLevel: Int = 0,
    val vipExp: Int = 0,
    val vipExpiresAt: Long? = null,
    val followingCount: Int = 0,
    val followersCount: Int = 0,
    val likesCount: Int = 0,
    val isMutual: Boolean = false,
    val isFollowing: Boolean = false,
    val registerRank: Int = 0,
    val isSignedInToday: Boolean = false,
    val lastSignInDate: String? = null,
    val consecutiveSignDays: Int = 0,
    val createdAt: Long = 0,
    val joinedQQGroup: Boolean = false
) {
    val displayName: String get() = username
    val isOnline: Boolean get() = false
    val levelTitle: String get() = if (isVip) "Lv.$vipLevel" else ""
}
