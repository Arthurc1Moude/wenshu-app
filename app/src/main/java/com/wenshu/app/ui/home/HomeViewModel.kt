package com.wenshu.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    val posts: LiveData<List<Post>> = repository.posts
    val isLoading: LiveData<Boolean> = repository.isLoading
    val error: LiveData<String?> = repository.error

    private val _currentTab = MutableLiveData(0)
    val currentTab: LiveData<Int> = _currentTab

    private val _tipResult = MutableLiveData<String?>()
    val tipResult: LiveData<String?> = _tipResult

    private var currentSort = "new"

    init {
        loadPosts()
    }

    fun setTab(tab: Int) {
        _currentTab.value = tab
        currentSort = when (tab) {
            0 -> "new"
            1 -> "hot"
            else -> "new"
        }
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            repository.loadPosts(sort = currentSort)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshPosts(sort = currentSort)
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
        }
    }

    fun toggleCollect(postId: String) {
        viewModelScope.launch {
            repository.toggleCollect(postId)
        }
    }

    fun tipPost(postId: String, amount: Int) {
        viewModelScope.launch {
            val result = repository.tipPost(postId, amount)
            result.onSuccess { resp ->
                _tipResult.postValue("投入${resp.amount}文书币")
            }.onFailure { e ->
                _tipResult.postValue(e.message ?: "投币失败，请稍后再试")
            }
        }
    }

    fun clearTipResult() {
        _tipResult.value = null
    }

    fun clearError() {
        repository.clearError()
    }
}
