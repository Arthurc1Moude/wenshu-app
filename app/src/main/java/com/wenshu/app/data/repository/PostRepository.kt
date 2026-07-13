package com.wenshu.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PostRepository {

    private val api = RetrofitClient.apiService

    private val _posts = MutableLiveData<List<Post>>(emptyList())
    val posts: LiveData<List<Post>> = _posts

    private val _userPosts = MutableLiveData<List<Post>>(emptyList())
    val userPosts: LiveData<List<Post>> = _userPosts

    private val _likedPosts = MutableLiveData<List<Post>>(emptyList())
    val likedPosts: LiveData<List<Post>> = _likedPosts

    private val _savedPosts = MutableLiveData<List<Post>>(emptyList())
    val savedPosts: LiveData<List<Post>> = _savedPosts

    private val _postDetail = MutableLiveData<Post?>()
    val postDetail: LiveData<Post?> = _postDetail

    private val _comments = MutableLiveData<List<Comment>>(emptyList())
    val comments: LiveData<List<Comment>> = _comments

    private val _notifications = MutableLiveData<List<NotificationItem>>(emptyList())
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _unreadCount = MutableLiveData<Int>(0)
    val unreadCount: LiveData<Int> = _unreadCount

    private val _searchResults = MutableLiveData<SearchResponse>()
    val searchResults: LiveData<SearchResponse> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun clearError() {
        _error.value = null
    }

    suspend fun loadPosts(sort: String? = "new", tag: String? = null, userId: String? = null): Result<List<Post>> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.getPosts(sort, tag, userId) }
        result.onSuccess { _posts.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun refreshPosts(sort: String? = "new", tag: String? = null): Result<List<Post>> {
        return loadPosts(sort, tag)
    }

    suspend fun loadPostDetail(postId: String): Result<Post> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.getPostById(postId) }
        result.onSuccess { _postDetail.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun loadUserPosts(userId: String): Result<List<Post>> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.getPosts(userId = userId) }
        result.onSuccess { _userPosts.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun loadLikedPosts(): Result<List<Post>> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.getLikedPosts() }
        result.onSuccess { _likedPosts.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun loadSavedPosts(): Result<List<Post>> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.getSavedPosts() }
        result.onSuccess { _savedPosts.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun loadComments(postId: String): Result<List<Comment>> {
        val result = safeApiCall { api.getComments(postId) }
        result.onSuccess { _comments.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        return result
    }

    suspend fun addComment(postId: String, content: String, replyToId: String? = null): Result<Comment> {
        val result = safeApiCall { api.addComment(postId, CommentRequest(content, replyToId)) }
        result.onSuccess { newComment ->
            val current = _comments.value?.toMutableList() ?: mutableListOf()
            current.add(newComment)
            _comments.postValue(current)
            val post = _postDetail.value
            if (post != null && post.id == postId) {
                _postDetail.postValue(post.copy(commentCount = post.commentCount + 1))
            }
        }.onFailure { _error.postValue(it.message) }
        return result
    }

    suspend fun toggleCommentLike(commentId: String): Result<LikeResponse> {
        val result = safeApiCall { api.toggleCommentLike(commentId) }
        result.onSuccess { response ->
            updateCommentLikeState(commentId, response.isLiked, response.likeCount)
        }.onFailure { _error.postValue(it.message) }
        return result
    }

    private fun updateCommentLikeState(commentId: String, isLiked: Boolean, likeCount: Int) {
        val current = _comments.value?.map { comment ->
            if (comment.id == commentId) {
                comment.copy(isLiked = isLiked, likeCount = likeCount)
            } else {
                comment
            }
        }
        _comments.postValue(current ?: emptyList())
    }

    suspend fun toggleLike(postId: String): Result<LikeResponse> {
        val result = safeApiCall { api.toggleLike(postId) }
        result.onSuccess { response ->
            updatePostLikeState(postId, response.isLiked, response.likeCount)
        }.onFailure { _error.postValue(it.message) }
        return result
    }

    suspend fun toggleCollect(postId: String): Result<CollectResponse> {
        val result = safeApiCall { api.toggleCollect(postId) }
        result.onSuccess { response ->
            updatePostCollectState(postId, response.isCollected, response.collectCount)
        }.onFailure { _error.postValue(it.message) }
        return result
    }

    suspend fun tipPost(postId: String, amount: Int): Result<TipResponse> {
        val result = safeApiCall { api.tipPost(postId, TipRequest(amount)) }
        result.onSuccess { response ->
            updatePostTipState(postId, response.isTipped, response.coinCount)
            if (response.totalCoins > 0) {
                val current = SharedPreferencesManager.getUser()
                if (current != null) {
                    SharedPreferencesManager.updateUser(current.copy(wenshuCoin = response.totalCoins))
                }
            }
        }
        return result
    }

    suspend fun uploadImage(imagePath: String): Result<String> {
        return try {
            val file = File(imagePath)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val result = safeApiCall { api.uploadImage(part) }
            result.map { it.url }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(content: String, images: List<String>, tags: List<String>): Result<Post> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.createPost(PostRequest(content, images, tags)) }
        result.onSuccess { newPost ->
            val current = _posts.value?.toMutableList() ?: mutableListOf()
            current.add(0, newPost)
            _posts.postValue(current)
        }.onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun search(query: String): Result<SearchResponse> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.search(query) }
        result.onSuccess { _searchResults.postValue(it) }
            .onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun loadNotifications(): Result<NotificationsResponse> {
        val result = safeApiCall { api.getNotifications() }
        result.onSuccess { response ->
            _notifications.postValue(response.notifications)
            _unreadCount.postValue(response.unreadCount)
        }.onFailure { _error.postValue(it.message) }
        return result
    }

    suspend fun markNotificationsRead(): Result<Unit> {
        val result = safeApiCall { api.markNotificationsRead() }
        result.onSuccess {
            _unreadCount.postValue(0)
            val current = _notifications.value?.map { it.copy(isRead = true) }
            _notifications.postValue(current ?: emptyList())
        }
        return result
    }

    suspend fun dailySignIn(): Result<SignInResponse> {
        return safeApiCall { api.dailySignIn() }
    }

    suspend fun purchaseVip(): Result<VipPurchaseResponse> {
        return safeApiCall { api.purchaseVip() }
    }

    suspend fun redeemCode(code: String): Result<RedeemResponse> {
        return safeApiCall { api.redeemCode(RedeemRequest(code)) }
    }

    suspend fun toggleFollow(userId: String): Result<FollowResponse> {
        return safeApiCall { api.toggleFollow(userId) }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return safeApiCall { api.getUserById(userId) }
    }

    suspend fun updateProfile(username: String? = null, bio: String? = null, location: String? = null, avatar: String? = null, cover: String? = null): Result<User> {
        _isLoading.postValue(true)
        val result = safeApiCall { api.updateProfile(UpdateUserRequest(username = username, bio = bio, location = location, avatar = avatar, cover = cover)) }
        result.onSuccess { user ->
            SharedPreferencesManager.updateUser(user)
        }.onFailure { _error.postValue(it.message) }
        _isLoading.postValue(false)
        return result
    }

    suspend fun refreshCurrentUser(): Result<User> {
        val result = safeApiCall { api.getCurrentUser() }
        result.onSuccess { SharedPreferencesManager.updateUser(it) }
        return result
    }

    private fun updatePostLikeState(postId: String, isLiked: Boolean, likeCount: Int) {
        fun updateList(list: List<Post>?): List<Post>? {
            return list?.map { if (it.id == postId) it.copy(isLiked = isLiked, likeCount = likeCount) else it }
        }
        _posts.postValue(updateList(_posts.value))
        _userPosts.postValue(updateList(_userPosts.value))
        _likedPosts.postValue(updateList(_likedPosts.value))
        _savedPosts.postValue(updateList(_savedPosts.value))
        _searchResults.postValue(_searchResults.value?.let { sr ->
            sr.copy(posts = updateList(sr.posts) ?: emptyList())
        })
        val detail = _postDetail.value
        if (detail != null && detail.id == postId) {
            _postDetail.postValue(detail.copy(isLiked = isLiked, likeCount = likeCount))
        }
    }

    private fun updatePostCollectState(postId: String, isCollected: Boolean, collectCount: Int) {
        fun updateList(list: List<Post>?): List<Post>? {
            return list?.map { if (it.id == postId) it.copy(isCollected = isCollected, collectCount = collectCount) else it }
        }
        _posts.postValue(updateList(_posts.value))
        _userPosts.postValue(updateList(_userPosts.value))
        _likedPosts.postValue(updateList(_likedPosts.value))
        _savedPosts.postValue(updateList(_savedPosts.value))
        _searchResults.postValue(_searchResults.value?.let { sr ->
            sr.copy(posts = updateList(sr.posts) ?: emptyList())
        })
        val detail = _postDetail.value
        if (detail != null && detail.id == postId) {
            _postDetail.postValue(detail.copy(isCollected = isCollected, collectCount = collectCount))
        }
    }

    private fun updatePostTipState(postId: String, isTipped: Boolean, coinCount: Int) {
        fun updateList(list: List<Post>?): List<Post>? {
            return list?.map { if (it.id == postId) it.copy(isTipped = isTipped, coinCount = coinCount) else it }
        }
        _posts.postValue(updateList(_posts.value))
        _userPosts.postValue(updateList(_userPosts.value))
        _likedPosts.postValue(updateList(_likedPosts.value))
        _savedPosts.postValue(updateList(_savedPosts.value))
        _searchResults.postValue(_searchResults.value?.let { sr ->
            sr.copy(posts = updateList(sr.posts) ?: emptyList())
        })
        val detail = _postDetail.value
        if (detail != null && detail.id == postId) {
            _postDetail.postValue(detail.copy(isTipped = isTipped, coinCount = coinCount))
        }
    }

    companion object {
        @Volatile
        private var instance: PostRepository? = null

        fun getInstance(): PostRepository {
            return instance ?: synchronized(this) {
                instance ?: PostRepository().also { instance = it }
            }
        }
    }
}
