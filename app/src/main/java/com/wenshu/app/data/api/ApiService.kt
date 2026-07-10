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

    @GET("health")
    suspend fun healthCheck(): Map<String, Any>
}
