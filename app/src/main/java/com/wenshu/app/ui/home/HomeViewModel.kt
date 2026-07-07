package com.wenshu.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentCategory = MutableLiveData("recommend")
    val currentCategory: LiveData<String> = _currentCategory

    private val repository = PostRepository

    init {
        loadPosts("recommend")
    }

    fun loadPosts(category: String) {
        _currentCategory.value = category
        _isLoading.value = true
        viewModelScope.launch {
            delay(300)
            _posts.value = repository.getPostsByCategory(category)
            _isLoading.value = false
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            delay(500)
            _posts.value = repository.getPostsByCategory(_currentCategory.value ?: "recommend")
            _isLoading.value = false
        }
    }

    fun toggleLike(post: Post) {
        repository.toggleLike(post.id)
        val updated = repository.getPostById(post.id) ?: return
        val current = _posts.value?.toMutableList() ?: return
        val index = current.indexOfFirst { it.id == post.id }
        if (index != -1) {
            current[index] = updated
            _posts.value = current
        }
    }
}
