package com.wenshu.app.data.model

data class User(
    val id: String,
    val username: String,
    val nickname: String,
    val avatarUrl: String,
    val bio: String = "",
    val gender: String = "female",
    val coverImageUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val totalLikesCount: Int = 0,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false
)
