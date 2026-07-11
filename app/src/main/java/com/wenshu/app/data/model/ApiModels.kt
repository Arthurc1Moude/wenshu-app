package com.wenshu.app.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val phone: String? = null
)

data class AuthResponse(
    val user: User,
    val token: String
)

data class ApiError(
    val error: String,
    val code: String? = null,
    val suggestions: List<String>? = null
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
    val location: String? = null,
    val phone: String? = null
)

data class UploadResponse(
    val url: String
)

data class SendCodeRequest(
    val phone: String,
    val purpose: String
)

data class SendCodeResponse(
    val success: Boolean,
    val message: String,
    val devCode: String? = null
)

data class BindPhoneRequest(
    val phone: String,
    val code: String
)

data class ChangePasswordRequest(
    val oldPassword: String? = null,
    val newPassword: String,
    val confirmPassword: String,
    val phone: String? = null,
    val code: String? = null
)

data class SimpleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val ok: Boolean? = null
)

data class SignInResponse(
    val coins: Int,
    val vipDays: Int = 0,
    val consecutiveDays: Int,
    val cycleDay: Int = 0,
    val rewardDesc: String = "",
    val totalCoins: Int,
    val isVip: Boolean = false,
    val vipExpiresAt: Long? = null
)

data class GroupChat(
    val id: String,
    val groupNumber: String,
    val name: String,
    val avatar: String? = null,
    val ownerId: String,
    val joinCode: String = "",
    val joinCodeExpiresAt: Long? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val memberCount: Int = 1,
    val role: String = "member",
    val isOwner: Boolean = false,
    val membersPreview: List<User> = emptyList(),
    val alreadyJoined: Boolean = false,
    val joined: Boolean = false,
    val createdAt: Long = 0
)

data class GroupMember(
    val userId: String,
    val role: String,
    val username: String,
    val avatar: String? = null,
    val isVip: Boolean = false
)

data class CreateGroupRequest(
    val name: String,
    val memberIds: List<String> = emptyList()
)

data class JoinGroupRequest(
    val code: String? = null,
    val groupNumber: String? = null
)

data class RenameGroupRequest(
    val name: String
)

data class AdminBanRequest(
    val duration: Long? = null,
    val reason: String? = null
)

data class AdminRewardRequest(
    val coins: Int? = null,
    val vipDays: Int? = null,
    val reason: String? = null
)
