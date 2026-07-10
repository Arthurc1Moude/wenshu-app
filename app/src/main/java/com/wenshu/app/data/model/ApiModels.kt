package com.wenshu.app.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val user: User,
    val token: String
)

data class ApiError(
    val error: String
)

data class CommentRequest(
    val content: String,
    val replyToId: String? = null
)

data class PostRequest(
    val content: String,
    val images: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

data class SearchResponse(
    val posts: List<Post> = emptyList(),
    val users: List<User> = emptyList()
)

data class LikeResponse(
    val likeCount: Int,
    val isLiked: Boolean
)

data class CollectResponse(
    val collectCount: Int,
    val isCollected: Boolean
)

data class FollowResponse(
    val isFollowing: Boolean,
    val isMutual: Boolean = false,
    val followingCount: Int,
    val followersCount: Int
)

data class FollowStatusResponse(
    val isFollowing: Boolean
)

data class SignInResponse(
    val coins: Int,
    val consecutiveDays: Int,
    val totalCoins: Int
)

data class RedeemRequest(
    val code: String
)

data class RedeemResponse(
    val coins: Int = 0,
    val vipGranted: Boolean = false,
    val description: String = "",
    val totalCoins: Int = 0,
    val vipExpiresAt: Long? = null
)

data class RedeemRecord(
    val id: String,
    val userId: String,
    val code: String,
    val coinValue: Int,
    val rewardType: String,
    val redeemedAt: Long
)

data class VipPurchaseResponse(
    val user: User
)

data class UpdateUserRequest(
    val username: String? = null,
    val bio: String? = null,
    val avatar: String? = null,
    val cover: String? = null,
    val location: String? = null
)

data class UploadResponse(
    val url: String
)
