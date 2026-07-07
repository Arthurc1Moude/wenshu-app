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

    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val repository = PostRepository

    fun loadPost(postId: String) {
        viewModelScope.launch {
            val p = repository.getPostById(postId)
            _post.value = p
            p?.let { _comments.value = repository.getComments(postId) }
        }
    }

    fun toggleLike() {
        val post = _post.value ?: return
        repository.toggleLike(post.id)
        _post.value = repository.getPostById(post.id)
    }

    fun toggleCollect() {
        val post = _post.value ?: return
        repository.toggleCollect(post.id)
        _post.value = repository.getPostById(post.id)
    }

    fun toggleFollow() {
        val post = _post.value ?: return
        repository.toggleFollow(post.author.id)
        _post.value = repository.getPostById(post.id)
    }

    fun addComment(content: String) {
        val post = _post.value ?: return
        val newComment = repository.addComment(post.id, content)
        val currentList = _comments.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, newComment)
        _comments.value = currentList
        _post.value = repository.getPostById(post.id)
    }

    fun sharePost() {
        val post = _post.value ?: return
        repository.sharePost(post.id)
        _post.value = repository.getPostById(post.id)
    }
}
