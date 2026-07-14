package com.wenshu.app.ui.publish

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.FileAttachment
import com.wenshu.app.data.model.MediaItem
import com.wenshu.app.data.model.UrlPreview
import com.wenshu.app.data.repository.PostRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class PendingFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val ext: String,
    val localPath: String? = null
)

class PublishViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    private val _publishResult = MutableLiveData<Boolean>(false)
    val publishResult: LiveData<Boolean> = _publishResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _uploadProgress = MutableLiveData<Pair<Int, Int>>()
    val uploadProgress: LiveData<Pair<Int, Int>> = _uploadProgress

    fun publish(
        content: String,
        title: String = "",
        imagePaths: List<String> = emptyList(),
        videoPaths: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        files: List<PendingFile> = emptyList(),
        isLongText: Boolean = false,
        location: String? = null,
        urlPreviews: List<UrlPreview> = emptyList()
    ) {
        if (content.isBlank()) {
            _error.value = "内容不能为空"
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val totalItems = imagePaths.size + videoPaths.size + files.size
                var completed = 0

                val uploadedImageUrls = mutableListOf<String>()
                val uploadedMedia = mutableListOf<MediaItem>()

                for (path in imagePaths) {
                    if (path.startsWith("http")) {
                        uploadedImageUrls.add(path)
                    } else {
                        val isGif = path.endsWith(".gif", true)
                        if (isGif) {
                            val uploadResult = repository.uploadMedia(path)
                            uploadResult.onSuccess { resp ->
                                uploadedMedia.add(MediaItem(url = resp.url, type = "gif"))
                            }.onFailure { e ->
                                Log.e("PublishVM", "Failed to upload gif: $path", e)
                            }
                        } else {
                            val uploadResult = repository.uploadImage(path)
                            uploadResult.onSuccess { url ->
                                uploadedImageUrls.add(url)
                            }.onFailure { e ->
                                Log.e("PublishVM", "Failed to upload image: $path", e)
                            }
                        }
                    }
                    completed++
                    _uploadProgress.postValue(Pair(completed, totalItems))
                }

                for (path in videoPaths) {
                    if (path.startsWith("http")) {
                        uploadedMedia.add(MediaItem(url = path, type = "video"))
                    } else {
                        val uploadResult = repository.uploadMedia(path)
                        uploadResult.onSuccess { resp ->
                            uploadedMedia.add(MediaItem(url = resp.url, type = resp.type))
                        }.onFailure { e ->
                            Log.e("PublishVM", "Failed to upload video: $path", e)
                            _error.postValue("视频上传失败: ${e.message}")
                        }
                    }
                    completed++
                    _uploadProgress.postValue(Pair(completed, totalItems))
                }

                val uploadedFiles = mutableListOf<FileAttachment>()
                for (pendingFile in files) {
                    val localPath = pendingFile.localPath
                    if (localPath != null) {
                        val uploadResult = repository.uploadFile(localPath)
                        uploadResult.onSuccess { resp ->
                            uploadedFiles.add(
                                FileAttachment(
                                    id = resp.id,
                                    url = resp.url,
                                    originalName = resp.originalName,
                                    ext = resp.ext,
                                    size = resp.size,
                                    sizeFormatted = resp.sizeFormatted,
                                    iconType = resp.iconType,
                                    expiresAt = resp.expiresAt,
                                    isPermanent = resp.isPermanent
                                )
                            )
                        }.onFailure { e ->
                            Log.e("PublishVM", "Failed to upload file: ${pendingFile.name}", e)
                            _error.postValue("文件 ${pendingFile.name} 上传失败: ${e.message}")
                        }
                    }
                    completed++
                    _uploadProgress.postValue(Pair(completed, totalItems))
                }

                val tagsWithHash = tags.map { if (it.startsWith("#")) it else "#$it" }

                val result = repository.createPost(
                    content = content,
                    title = title,
                    images = uploadedImageUrls.toList(),
                    media = uploadedMedia.toList(),
                    files = uploadedFiles.toList(),
                    tags = tagsWithHash,
                    isLongText = isLongText,
                    location = location,
                    urlPreviews = urlPreviews
                )
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
