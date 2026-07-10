package com.wenshu.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.model.User
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _userPosts = MutableLiveData<List<Post>>(emptyList())
    val userPosts: LiveData<List<Post>> = _userPosts

    private val _likedPosts = MutableLiveData<List<Post>>(emptyList())
    val likedPosts: LiveData<List<Post>> = _likedPosts

    private val _savedPosts = MutableLiveData<List<Post>>(emptyList())
    val savedPosts: LiveData<List<Post>> = _savedPosts

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _signInResult = MutableLiveData<String?>()
    val signInResult: LiveData<String?> = _signInResult

    private val _followResult = MutableLiveData<String?>()
    val followResult: LiveData<String?> = _followResult

    private var isMyProfile = true

    init {
        loadMyProfile()
    }

    fun loadMyProfile() {
        isMyProfile = true
        val currentUser = SharedPreferencesManager.getUser()
        _user.value = currentUser
        viewModelScope.launch {
            val result = repository.refreshCurrentUser()
            result.onSuccess { _user.postValue(it) }
            val userId = currentUser?.id ?: ""
            if (userId.isNotEmpty()) {
                repository.loadUserPosts(userId).onSuccess { _userPosts.postValue(it) }
            }
        }
    }

    fun loadUserProfile() {
        if (isMyProfile) {
            loadMyProfile()
        }
    }

    fun loadUserProfile(userId: String) {
        isMyProfile = false
        _isLoading.value = true
        viewModelScope.launch {
            val userResult = repository.getUserById(userId)
            userResult.onSuccess { _user.postValue(it) }
                .onFailure { _error.postValue(it.message) }
            repository.loadUserPosts(userId).onSuccess { _userPosts.postValue(it) }
            _isLoading.postValue(false)
        }
    }

    fun loadLikedPosts() {
        viewModelScope.launch {
            repository.loadLikedPosts().onSuccess { _likedPosts.postValue(it) }
        }
    }

    fun loadSavedPosts() {
        viewModelScope.launch {
            repository.loadSavedPosts().onSuccess { _savedPosts.postValue(it) }
        }
    }

    fun dailySignIn() {
        viewModelScope.launch {
            val result = repository.dailySignIn()
            result.onSuccess { response ->
                _signInResult.postValue("签到成功！获得${response.coins}文书币，连续${response.consecutiveDays}天")
                refreshCurrentUser()
            }.onFailure { _error.postValue(it.message) }
        }
    }

    fun toggleFollow() {
        val targetUser = _user.value ?: return
        viewModelScope.launch {
            val result = repository.toggleFollow(targetUser.id)
            result.onSuccess {
                _followResult.postValue(if (it.isFollowing) "已关注" else "已取消关注")
            }.onFailure { _error.postValue(it.message) }
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            repository.refreshCurrentUser().onSuccess { _user.postValue(it) }
        }
    }

    fun clearSignInResult() { _signInResult.value = null }
    fun clearFollowResult() { _followResult.value = null }
    fun clearError() { _error.value = null }
}
