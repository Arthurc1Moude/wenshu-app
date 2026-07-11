package com.wenshu.app.data.api

import com.wenshu.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("users/me")
    suspend fun getCurrentUser(): User

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateUserRequest): User

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): User

    @GET("posts")
    suspend fun getPosts(
        @Query("sort") sort: String? = null,
        @Query("tag") tag: String? = null,
        @Query("userId") userId: String? = null
    ): List<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: String): Post

    @POST("posts")
    suspend fun createPost(@Body request: PostRequest): Post

    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): UploadResponse

    @POST("posts/{id}/like")
    suspend fun toggleLike(@Path("id") postId: String): LikeResponse

    @POST("posts/{id}/collect")
    suspend fun toggleCollect(@Path("id") postId: String): CollectResponse

    @GET("posts/{id}/comments")
    suspend fun getComments(@Path("id") postId: String): List<Comment>

    @POST("posts/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Body request: CommentRequest
    ): Comment

    @POST("comments/{id}/like")
    suspend fun toggleCommentLike(@Path("id") commentId: String): LikeResponse

    @GET("posts/liked/mine")
    suspend fun getLikedPosts(): List<Post>

    @GET("posts/saved/mine")
    suspend fun getSavedPosts(): List<Post>

    @GET("notifications")
    suspend fun getNotifications(): NotificationsResponse

    @POST("notifications/read")
    suspend fun markNotificationsRead()

    @GET("search")
    suspend fun search(@Query("q") query: String): SearchResponse

    @POST("coin/signin")
    suspend fun dailySignIn(): SignInResponse

    @POST("vip/purchase")
    suspend fun purchaseVip(): VipPurchaseResponse

    @POST("redeem")
    suspend fun redeemCode(@Body request: RedeemRequest): RedeemResponse

    @GET("redeem/records")
    suspend fun getRedeemRecords(): List<RedeemRecord>

    @POST("users/{id}/follow")
    suspend fun toggleFollow(@Path("id") userId: String): FollowResponse

    @GET("users/{id}/follow-status")
    suspend fun getFollowStatus(@Path("id") userId: String): FollowStatusResponse

    @POST("users/{id}/block")
    suspend fun blockUser(@Path("id") userId: String): SimpleResponse

    @POST("users/{id}/unblock")
    suspend fun unblockUser(@Path("id") userId: String): SimpleResponse

    @GET("users/{id}/block-status")
    suspend fun getBlockStatus(@Path("id") userId: String): Map<String, Boolean>

    @POST("auth/send-code")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): SendCodeResponse

    @POST("users/me/bind-phone")
    suspend fun bindPhone(@Body request: BindPhoneRequest): SimpleResponse

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): SimpleResponse

    @GET("health")
    suspend fun healthCheck(): Map<String, Any>

    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): GroupChat

    @GET("groups/mine")
    suspend fun getMyGroups(): List<GroupChat>

    @POST("groups/join")
    suspend fun joinGroup(@Body request: JoinGroupRequest): GroupChat

    @POST("groups/{id}/refresh-code")
    suspend fun refreshGroupCode(@Path("id") groupId: String): Map<String, Any>

    @POST("groups/{id}/rename")
    suspend fun renameGroup(@Path("id") groupId: String, @Body request: RenameGroupRequest): Map<String, Any>

    @GET("groups/{id}/members")
    suspend fun getGroupMembers(@Path("id") groupId: String): List<GroupMember>

    @POST("groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") groupId: String): SimpleResponse

    @GET("friends")
    suspend fun getFriends(): List<User>

    @POST("admin/ban/{userId}")
    suspend fun adminBanUser(@Path("userId") userId: String, @Body request: AdminBanRequest): SimpleResponse

    @POST("admin/unban/{userId}")
    suspend fun adminUnbanUser(@Path("userId") userId: String): SimpleResponse

    @POST("admin/reward/{userId}")
    suspend fun adminRewardUser(@Path("userId") userId: String, @Body request: AdminRewardRequest): SimpleResponse

    @GET("admin/users")
    suspend fun adminListUsers(@Query("search") search: String? = null): List<User>
}
