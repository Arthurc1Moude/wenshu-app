package com.wenshu.app.ui.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.ui.adapters.TrendingTopic

class DiscoverViewModel : ViewModel() {

    private val _hotSearches = MutableLiveData<List<String>>()
    val hotSearches: LiveData<List<String>> = _hotSearches

    private val _trendingTopics = MutableLiveData<List<TrendingTopic>>()
    val trendingTopics: LiveData<List<TrendingTopic>> = _trendingTopics

    private val repository = PostRepository

    init {
        loadData()
    }

    private fun loadData() {
        _hotSearches.value = repository.getHotSearches()
        _trendingTopics.value = listOf(
            TrendingTopic(1, "夏日穿搭灵感", "128.5w人在讨论", "128.5w", true),
            TrendingTopic(2, "减脂餐食谱分享", "96.2w人在讨论", "96.2w", true),
            TrendingTopic(3, "租房改造大作战", "84.7w人在讨论", "84.7w", true),
            TrendingTopic(4, "考研上岸经验贴", "72.3w人在讨论", "72.3w", false),
            TrendingTopic(5, "咖啡探店合集", "65.8w人在讨论", "65.8w", false),
            TrendingTopic(6, "平价好物推荐", "58.1w人在讨论", "58.1w", false),
            TrendingTopic(7, "旅行攻略集合", "52.4w人在讨论", "52.4w", false),
            TrendingTopic(8, "化妆小白教程", "47.6w人在讨论", "47.6w", false),
            TrendingTopic(9, "副业赚钱思路", "43.2w人在讨论", "43.2w", false),
            TrendingTopic(10, "读书笔记分享", "38.9w人在讨论", "38.9w", false)
        )
    }

    fun searchPosts(query: String): List<Post> {
        return repository.searchPosts(query)
    }
}
