package com.wenshu.app.ui.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.model.SearchResponse
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class DiscoverViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    val isLoading: LiveData<Boolean> = repository.isLoading
    val error: LiveData<String?> = repository.error

    private val _trendingTags = MutableLiveData<List<TrendingTopic>>(emptyList())
    val trendingTags: LiveData<List<TrendingTopic>> = _trendingTags

    private val _activities = MutableLiveData<List<TrendingTopic>>(emptyList())
    val activities: LiveData<List<TrendingTopic>> = _activities

    init {
        loadTrendingTags()
    }

    fun loadTrendingTags() {
        _trendingTags.value = listOf(
            TrendingTopic("夏日生活", "12.5w", 1),
            TrendingTopic("日常打卡", "8.2w", 2),
            TrendingTopic("读书分享", "5.6w", 3),
            TrendingTopic("美食探店", "4.3w", 0),
            TrendingTopic("摄影日记", "3.8w", 0),
            TrendingTopic("穿搭分享", "3.1w", 0)
        )
        _activities.value = listOf(
            TrendingTopic("夏日生活记录", "参与人数 1234", 1),
            TrendingTopic("日常打卡挑战", "参与人数 5678", 2),
            TrendingTopic("读书分享月", "即将开始", 0)
        )
    }

    fun search(query: String, onResult: (SearchResponse) -> Unit) {
        viewModelScope.launch {
            val result = repository.search(query)
            result.onSuccess { onResult(it) }
        }
    }

    fun loadPostsByTag(tag: String, onResult: (List<Post>) -> Unit) {
        viewModelScope.launch {
            val result = repository.loadPosts(tag = tag)
            result.onSuccess { onResult(it) }
        }
    }

    fun clearError() {
        repository.clearError()
    }
}

data class TrendingTopic(
    val title: String,
    val heat: String,
    val rank: Int
)
