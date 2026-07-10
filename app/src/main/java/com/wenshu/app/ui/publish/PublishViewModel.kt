package com.wenshu.app.ui.publish

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class PublishViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    private val _publishResult = MutableLiveData<Boolean>(false)
    val publishResult: LiveData<Boolean> = _publishResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun publish(content: String, title: String = "", imagePaths: List<String> = emptyList(), tags: List<String> = emptyList()) {
        if (content.isBlank()) {
            _error.value = "内容不能为空"
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val uploadedImageUrls = mutableListOf<String>()
                for (path in imagePaths) {
                    if (path.startsWith("http")) {
                        uploadedImageUrls.add(path)
                    } else {
                        val uploadResult = repository.uploadImage(path)
                        uploadResult.onSuccess { url ->
                            uploadedImageUrls.add(url)
                        }.onFailure { e ->
                            Log.e("PublishVM", "Failed to upload image: $path", e)
                        }
                    }
                }

                var finalContent = content
                if (title.isNotBlank()) {
                    finalContent = "## $title\n\n$content"
                }
                val tagsWithHash = tags.map { if (it.startsWith("#")) it else "#$it" }

                val result = repository.createPost(finalContent, uploadedImageUrls.toList(), tagsWithHash)
                result.onSuccess {
                    _publishResult.postValue(true)
                }.onFailure {
                    _error.postValue(it.message)
                }
            } catch (e: Exception) {
                Log.e("PublishVM", "Publish error", e)
                _error.postValue(e.message ?: "发布失败")
            }
            _isLoading.postValue(false)
        }
    }

    fun resetPublishResult() {
        _publishResult.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
