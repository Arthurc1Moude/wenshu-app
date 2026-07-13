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

data class TipResponse(
    val coinCount: Int,
    val isTipped: Boolean,
    val amount: Int = 0,
    val totalCoins: Int = 0
)

data class TipRequest(
    val amount: Int = 10
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

data class Conversation(
    val id: String = "",
    val type: String = "private",
    val name: String = "",
    val avatar: String? = null,
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val otherUser: User? = null,
    val isSystem: Boolean = false,
    val groupNumber: String? = null,
    val ownerId: String? = null,
    val memberCount: Int = 0
)

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val senderName: String? = null,
    val senderAvatar: String? = null,
    val sender: User? = null
)

data class SendMessageRequest(
    val content: String
)

data class Book(
    val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "book",
    val content: String = "",
    val fileUrl: String = "",
    val coverUrl: String = "",
    val isPrivate: Boolean = false,
    val readCount: Int = 0,
    val likeCount: Int = 0,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

data class CreateBookRequest(
    val title: String,
    val description: String = "",
    val category: String = "book",
    val content: String = "",
    val fileUrl: String = "",
    val coverUrl: String = "",
    val isPrivate: Boolean = false
)

data class MiniApp(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val url: String = "",
    val category: String = "other",
    val developerId: String = "",
    val developerName: String = "",
    val createdAt: Long = 0
)

data class Game(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val url: String = "",
    val category: String = "other",
    val developerId: String = "",
    val developerName: String = "",
    val plays: Int = 0,
    val createdAt: Long = 0
)

data class SecretPost(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val images: List<String> = emptyList(),
    val visibility: String = "private",
    val allowedUsers: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val createdAt: Long = 0
)

data class CreateSecretPostRequest(
    val content: String,
    val images: List<String> = emptyList(),
    val visibility: String = "private",
    val allowedUsers: List<String> = emptyList()
)

data class SecretVisibilityRequest(
    val visibility: String,
    val allowedUsers: List<String> = emptyList()
)

data class SecretVisit(
    val id: String = "",
    val spaceOwnerId: String = "",
    val visitorId: String = "",
    val visitorName: String? = null,
    val visitorAvatar: String? = null,
    val createdAt: Long = 0
)
