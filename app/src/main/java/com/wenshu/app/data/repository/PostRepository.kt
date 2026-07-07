package com.wenshu.app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wenshu.app.data.model.Comment
import com.wenshu.app.data.model.NotificationItem
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.model.User
import com.wenshu.app.util.MockDataGenerator

object PostRepository {

    private val users = MockDataGenerator.generateUsers(30)
    private var posts = MockDataGenerator.generatePosts(users, 50).toMutableList()
    private val _postsLiveData = MutableLiveData<List<Post>>(posts)
    val postsLiveData: LiveData<List<Post>> = _postsLiveData

    fun getPostsByCategory(category: String): List<Post> {
        return if (category == "recommend") posts
        else posts.filter { it.category == category }
    }

    fun getPostById(postId: String): Post? {
        return posts.find { it.id == postId }
    }

    fun getComments(postId: String): List<Comment> {
        return MockDataGenerator.generateComments(users, postId, 15)
    }

    fun getNotifications(): List<NotificationItem> {
        return MockDataGenerator.generateNotifications(users, 30)
    }

    fun getCurrentUser(): User = MockDataGenerator.getCurrentUser()

    fun getCategories() = MockDataGenerator.getCategories()

    fun getHotSearches() = MockDataGenerator.getHotSearches()

    fun toggleLike(postId: String) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = posts[index]
            posts[index] = post.copy(
                isLiked = !post.isLiked,
                likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
            )
            _postsLiveData.postValue(posts)
        }
    }

    fun toggleCollect(postId: String) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = posts[index]
            posts[index] = post.copy(
                isCollected = !post.isCollected,
                collectCount = if (post.isCollected) post.collectCount - 1 else post.collectCount + 1
            )
            _postsLiveData.postValue(posts)
        }
    }

    fun toggleFollow(userId: String) {
        val index = posts.indexOfFirst { it.author.id == userId }
        if (index != -1) {
            val post = posts[index]
            val newFollowed = !post.isFollowed
            for (i in posts.indices) {
                if (posts[i].author.id == userId) {
                    posts[i] = posts[i].copy(isFollowed = newFollowed)
                }
            }
            _postsLiveData.postValue(posts)
        }
    }

    fun addComment(postId: String, content: String): Comment {
        val comment = Comment(
            id = "comment_${System.currentTimeMillis()}",
            postId = postId,
            author = getCurrentUser(),
            content = content,
            likeCount = 0,
            isLiked = false,
            createdAt = System.currentTimeMillis(),
            replies = emptyList(),
            replyToUser = null
        )
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            posts[index] = posts[index].copy(commentCount = posts[index].commentCount + 1)
            _postsLiveData.postValue(posts)
        }
        return comment
    }

    fun sharePost(postId: String) {
        val index = posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            posts[index] = posts[index].copy(shareCount = posts[index].shareCount + 1)
            _postsLiveData.postValue(posts)
        }
    }

    fun publishPost(title: String, content: String, tags: List<String>, location: String?): Post {
        val newPost = Post(
            id = "post_${System.currentTimeMillis()}",
            author = getCurrentUser(),
            title = title.ifBlank { content.take(20) },
            content = content,
            coverImageUrl = "https://picsum.photos/seed/new${System.currentTimeMillis()}/400/500",
            imageUrls = emptyList(),
            tags = tags,
            location = location,
            likeCount = 0,
            commentCount = 0,
            collectCount = 0,
            shareCount = 0,
            category = "recommend",
            coverWidth = 400,
            coverHeight = 500,
            createdAt = System.currentTimeMillis()
        )
        posts.add(0, newPost)
        _postsLiveData.postValue(posts)
        return newPost
    }

    fun getUserPosts(userId: String): List<Post> {
        return posts.filter { it.author.id == userId }
    }

    fun searchPosts(query: String): List<Post> {
        return posts.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
        }
    }
}
