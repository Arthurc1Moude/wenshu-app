package com.wenshu.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.model.User
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.util.TimeUtils

class ProfileViewModel : ViewModel() {

    private val _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val _myPosts = MutableLiveData<List<Post>>()
    val myPosts: LiveData<List<Post>> = _myPosts

    private val _likedPosts = MutableLiveData<List<Post>>()
    val likedPosts: LiveData<List<Post>> = _likedPosts

    private val _collectedPosts = MutableLiveData<List<Post>>()
    val collectedPosts: LiveData<List<Post>> = _collectedPosts

    private val repository = PostRepository

    init {
        loadData()
    }

    private fun loadData() {
        val user = User(
            id = "me",
            username = "wenshu_user",
            nickname = "文书用户",
            avatarUrl = "https://ui-avatars.com/api/?name=W&background=000&color=fff&size=200",
            bio = "记录生活，分享美好 ✨",
            followersCount = 256,
            followingCount = 128,
            postsCount = 12,
            totalLikesCount = 1892,
            isVerified = false,
            isFollowing = false
        )
        _currentUser.value = user

        val allPosts = repository.postsLiveData.value ?: emptyList()
        _myPosts.value = allPosts.take(12)
        _likedPosts.value = allPosts.filter { it.isLiked }.take(10)
        _collectedPosts.value = allPosts.filter { it.isCollected }.take(8)
    }

    fun getCurrentTabPosts(tab: String): List<Post> {
        return when (tab) {
            "my" -> _myPosts.value ?: emptyList()
            "liked" -> _likedPosts.value ?: emptyList()
            "collected" -> _collectedPosts.value ?: emptyList()
            else -> emptyList()
        }
    }
}
