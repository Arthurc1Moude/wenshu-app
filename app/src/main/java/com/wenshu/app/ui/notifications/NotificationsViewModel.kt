package com.wenshu.app.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.NotificationItem
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private var allNotifications: List<NotificationItem> = emptyList()

    private val repository = PostRepository

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            allNotifications = repository.getNotifications()
            _notifications.value = allNotifications
        }
    }

    fun selectTab(type: String) {
        _notifications.value = when (type) {
            "all" -> allNotifications
            "likes" -> allNotifications.filter { it.type.name == "LIKE" || it.type.name == "COLLECT" }
            "comments" -> allNotifications.filter { it.type.name == "COMMENT" || it.type.name == "MENTION" }
            "follows" -> allNotifications.filter { it.type.name == "FOLLOW" }
            "mentions" -> allNotifications.filter { it.type.name == "MENTION" }
            else -> allNotifications
        }
    }
}
