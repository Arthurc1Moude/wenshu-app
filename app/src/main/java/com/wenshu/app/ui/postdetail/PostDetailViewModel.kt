package com.wenshu.app.ui.postdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.Comment
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    val post: LiveData<Post?> = repository.postDetail
    val comments: LiveData<List<Comment>> = repository.comments
    val isLoading: LiveData<Boolean> = repository.isLoading
    val error: LiveData<String?> = repository.error

    private val _commentAdded = MutableLiveData<Boolean>()
    val commentAdded: LiveData<Boolean> = _commentAdded

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    fun loadPost(postId: String) {
        viewModelScope.launch {
            repository.loadPostDetail(postId)
        }
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            repository.loadComments(postId)
        }
    }

    fun toggleLike() {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            val result = repository.toggleLike(currentPost.id)
            result.onFailure { _actionResult.postValue(it.message) }
        }
    }

    fun toggleCollect() {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            val result = repository.toggleCollect(currentPost.id)
            result.onFailure { _actionResult.postValue(it.message) }
        }
    }

    fun addComment(content: String, replyToId: String? = null) {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            val result = repository.addComment(currentPost.id, content, replyToId)
            result.onSuccess { _commentAdded.postValue(true) }
                .onFailure { _actionResult.postValue(it.message) }
        }
    }

    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            val result = repository.toggleCommentLike(commentId)
            result.onFailure { _actionResult.postValue(it.message) }
        }
    }

    fun resetCommentAdded() {
        _commentAdded.value = false
    }

    fun clearActionResult() {
        _actionResult.value = null
    }

    fun clearError() {
        repository.clearError()
    }
}
