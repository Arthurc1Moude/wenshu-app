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
            repository.toggleLike(currentPost.id)
        }
    }

    fun toggleCollect() {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            repository.toggleCollect(currentPost.id)
        }
    }

    fun tipPost(amount: Int) {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            val result = repository.tipPost(currentPost.id, amount)
            result.onSuccess { resp ->
                _actionResult.postValue("投入${resp.amount}文书币")
            }.onFailure { e ->
                _actionResult.postValue(e.message ?: "投币失败，请稍后再试")
            }
        }
    }

    fun addComment(content: String, replyToId: String? = null) {
        val currentPost = post.value ?: return
        viewModelScope.launch {
            val result = repository.addComment(currentPost.id, content, replyToId)
            result.onSuccess { _commentAdded.postValue(true) }
        }
    }

    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            repository.toggleCommentLike(commentId)
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
