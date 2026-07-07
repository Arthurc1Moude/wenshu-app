package com.wenshu.app.ui.publish

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch

class PublishViewModel : ViewModel() {

    private val _selectedImages = MutableLiveData<MutableList<Uri>>(mutableListOf())
    val selectedImages: LiveData<MutableList<Uri>> = _selectedImages

    private val _selectedTags = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedTags: LiveData<MutableList<String>> = _selectedTags

    private val _selectedLocation = MutableLiveData<String?>(null)
    val selectedLocation: LiveData<String?> = _selectedLocation

    private val _publishSuccess = MutableLiveData<Post?>()
    val publishSuccess: LiveData<Post?> = _publishSuccess

    private val repository = PostRepository

    fun addImage(uri: Uri) {
        val list = _selectedImages.value ?: mutableListOf()
        if (list.size < 9) {
            list.add(uri)
            _selectedImages.value = list
        }
    }

    fun removeImage(uri: Uri) {
        val list = _selectedImages.value ?: mutableListOf()
        list.remove(uri)
        _selectedImages.value = list
    }

    fun toggleTag(tag: String) {
        val list = _selectedTags.value ?: mutableListOf()
        if (list.contains(tag)) list.remove(tag) else list.add(tag)
        _selectedTags.value = list
    }

    fun setLocation(location: String?) {
        _selectedLocation.value = location
    }

    fun publish(title: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val post = repository.publishPost(title, content, _selectedTags.value ?: emptyList(), _selectedLocation.value)
            _publishSuccess.value = post
        }
    }

    fun resetPublish() {
        _publishSuccess.value = null
        _selectedImages.value = mutableListOf()
        _selectedTags.value = mutableListOf()
        _selectedLocation.value = null
    }
}
