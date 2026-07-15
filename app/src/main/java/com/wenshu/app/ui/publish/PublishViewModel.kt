package com.wenshu.app.ui.publish

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wenshu.app.data.model.FileAttachment
import com.wenshu.app.data.model.UrlPreview
import com.wenshu.app.data.model.VideoAttachment
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.ui.adapters.PendingFile
import com.wenshu.app.ui.adapters.PendingVideo
import kotlinx.coroutines.launch
import java.io.File

class PublishViewModel : ViewModel() {

    private val repository = PostRepository.getInstance()

    private val _publishResult = MutableLiveData<Boolean>(false)
    val publishResult: LiveData<Boolean> = _publishResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _urlPreviewLoading = MutableLiveData<Boolean>(false)
    val urlPreviewLoading: LiveData<Boolean> = _urlPreviewLoading

    fun publish(
        content: String,
        title: String = "",
        imagePaths: List<String> = emptyList(),
        videoPaths: List<PendingVideo> = emptyList(),
        filePaths: List<PendingFile> = emptyList(),
        tags: List<String> = emptyList(),
        location: String = "",
        isLongPost: Boolean = false,
        urlPreviews: List<UrlPreview> = emptyList()
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val hasMedia = imagePaths.isNotEmpty() || videoPaths.isNotEmpty()
                if (!hasMedia) {
                    _error.postValue("请添加至少一张图片")
                    _isLoading.postValue(false)
                    return@launch
                }

                val uploadedImageUrls = mutableListOf<String>()
                for (path in imagePaths) {
                    if (path.startsWith("http")) {
                        uploadedImageUrls.add(path)
                    } else {
                        val file = File(path)
                        val mimeType = getMimeType(file)
                        val uploadResult = repository.uploadMedia(path, if (mimeType.startsWith("video/")) "video/*" else "image/*")
                        uploadResult.onSuccess { response ->
                            uploadedImageUrls.add(response.url)
                        }.onFailure { e ->
                            Log.e("PublishVM", "Failed to upload image: $path", e)
                            _error.postValue("图片上传失败: ${e.message}")
                            _isLoading.postValue(false)
                            return@launch
                        }
                    }
                }

                val uploadedVideos = mutableListOf<VideoAttachment>()
                for (video in videoPaths) {
                    val file = File(video.path)
                    val mimeType = getMimeType(file)
                    val uploadResult = repository.uploadMedia(video.path, mimeType)
                    uploadResult.onSuccess { response ->
                        uploadedVideos.add(VideoAttachment(
                            url = response.url,
                            thumbnail = response.thumbnail ?: "",
                            duration = video.duration
                        ))
                    }.onFailure { e ->
                        Log.e("PublishVM", "Failed to upload video: ${video.path}", e)
                        _error.postValue("视频上传失败: ${e.message}")
                        _isLoading.postValue(false)
                        return@launch
                    }
                }

                val uploadedFiles = mutableListOf<FileAttachment>()
                for (pendingFile in filePaths) {
                    val file = File(pendingFile.path)
                    val mimeType = pendingFile.mimeType.ifEmpty { getMimeType(file) }
                    val uploadResult = repository.uploadFile(pendingFile.path, mimeType)
                    uploadResult.onSuccess { response ->
                        uploadedFiles.add(FileAttachment(
                            id = response.id ?: "",
                            filename = pendingFile.filename,
                            size = pendingFile.size,
                            mimeType = mimeType,
                            url = response.url
                        ))
                    }.onFailure { e ->
                        Log.e("PublishVM", "Failed to upload file: ${pendingFile.path}", e)
                        _error.postValue("文件上传失败: ${e.message}")
                        _isLoading.postValue(false)
                        return@launch
                    }
                }

                val result = repository.createPost(
                    content = content,
                    title = title,
                    images = uploadedImageUrls.toList(),
                    videos = uploadedVideos.toList(),
                    files = uploadedFiles.toList(),
                    tags = tags,
                    location = location,
                    isLongPost = isLongPost,
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

    suspend fun fetchUrlPreview(url: String): Result<UrlPreview> {
        _urlPreviewLoading.postValue(true)
        val result = repository.getUrlPreview(url)
        _urlPreviewLoading.postValue(false)
        return result
    }

    private fun getMimeType(file: File): String {
        return try {
            val uri = Uri.fromFile(file)
            val contentResolver = com.wenshu.app.MainActivity.appContext?.contentResolver
            contentResolver?.getType(uri) ?: when {
                file.name.endsWith(".jpg", true) || file.name.endsWith(".jpeg", true) -> "image/jpeg"
                file.name.endsWith(".png", true) -> "image/png"
                file.name.endsWith(".gif", true) -> "image/gif"
                file.name.endsWith(".webp", true) -> "image/webp"
                file.name.endsWith(".mp4", true) -> "video/mp4"
                file.name.endsWith(".mov", true) -> "video/quicktime"
                file.name.endsWith(".pdf", true) -> "application/pdf"
                else -> "application/octet-stream"
            }
        } catch (e: Exception) {
            "application/octet-stream"
        }
    }

    fun resetPublishResult() {
        _publishResult.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
