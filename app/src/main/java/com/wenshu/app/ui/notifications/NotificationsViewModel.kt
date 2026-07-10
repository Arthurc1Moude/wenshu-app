package com.wenshu.app.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.NotificationItem
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    val notifications: LiveData<List<NotificationItem>> = repository.notifications
    val unreadCount: LiveData<Int> = repository.unreadCount
    val isLoading: LiveData<Boolean> = repository.isLoading
    val error: LiveData<String?> = repository.error

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.loadNotifications()
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repository.markNotificationsRead()
        }
    }

    fun refresh() {
        loadNotifications()
    }

    fun clearError() {
        repository.clearError()
    }
}
