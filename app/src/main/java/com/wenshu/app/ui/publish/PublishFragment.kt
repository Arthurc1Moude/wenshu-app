package com.wenshu.app.ui.publish

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wenshu.app.BuildConfig
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.data.model.UrlPreview
import com.wenshu.app.data.repository.DraftRepository
import com.wenshu.app.databinding.DialogMediaPickerBinding
import com.wenshu.app.databinding.FragmentPublishBinding
import com.wenshu.app.databinding.ItemUrlPreviewBinding
import com.wenshu.app.ui.adapters.PublishFileAdapter
import com.wenshu.app.ui.adapters.PublishImageAdapter
import com.wenshu.app.ui.adapters.PublishVideoAdapter
import com.wenshu.app.ui.adapters.TopicItem
import com.wenshu.app.ui.adapters.TopicSearchAdapter
import com.wenshu.app.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PublishViewModel by viewModels()
    private lateinit var imageAdapter: PublishImageAdapter
    private lateinit var videoAdapter: PublishVideoAdapter
    private lateinit var fileAdapter: PublishFileAdapter
    private lateinit var draftRepository: DraftRepository

    private val defaultTags = listOf("夏日生活", "日常打卡", "读书分享", "美食探店", "摄影日记", "穿搭分享", "旅行日记", "心情随笔", "生活记录", "每日一思")
    private val selectedTags = mutableListOf<String>()
    private var hasTitle = false
    private var selectedLocation: String = ""
    private var isLongPost = false
    private val urlPreviews = mutableListOf<UrlPreview>()
    private var pendingCameraImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleMediaPick(uri, isVideo = false)
            }
        }
    }

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleMediaPick(uri, isVideo = true)
            }
        }
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFilePick(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingCameraImageUri?.let { uri ->
                handleMediaPick(uri, isVideo = false)
            }
        }
        pendingCameraImageUri = null
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showMediaPickerDialog()
        } else {
            Toast.makeText(requireContext(), "需要权限才能选择媒体", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            requestLocation()
        } else {
            Toast.makeText(requireContext(), "需要位置权限", Toast.LENGTH_SHORT).show()
        }
    }

    private val richEditorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val content = result.data?.getStringExtra("content") ?: ""
            val title = result.data?.getStringExtra("title") ?: ""
            val isLong = result.data?.getBooleanExtra("isLongPost", false) ?: false
            if (content.isNotEmpty()) {
                binding.etContent.setText(content)
            }
            if (title.isNotEmpty()) {
                hasTitle = true
                binding.etTitle.visibility = View.VISIBLE
                binding.etTitle.setText(title)
            }
            isLongPost = isLong
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        draftRepository = DraftRepository.getInstance(requireContext())
        setupImages()
        setupVideos()
        setupFiles()
        setupContent()
        setupTagSelection()
        setupButtons()
        setupToolBar()
        observeData()
        loadDraft()
    }

    private fun setupImages() {
        imageAdapter = PublishImageAdapter(
            onAddClick = { checkPermissionsAndShowPicker() },
            onRemoveClick = { position -> imageAdapter.removeImage(position) }
        )
        binding.recyclerImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imageAdapter
            itemAnimator = null
        }
    }

    private fun setupVideos() {
        videoAdapter = PublishVideoAdapter(
            onRemoveClick = { position ->
                videoAdapter.removeVideo(position)
                binding.layoutVideos.visibility = if (videoAdapter.itemCount > 0) View.VISIBLE else View.GONE
            }
        )
        binding.layoutVideos.apply {
            orientation = LinearLayout.VERTICAL
            removeAllViews()
        }
    }

    private fun addVideoItem(path: String, filename: String, size: Long) {
        videoAdapter.addVideo(path, filename, size)
        binding.layoutVideos.visibility = View.VISIBLE
        binding.layoutVideos.removeAllViews()
        for (i in 0 until videoAdapter.itemCount) {
            val holder = videoAdapter.onCreateViewHolder(binding.layoutVideos, 0)
            videoAdapter.onBindViewHolder(holder, i)
            binding.layoutVideos.addView(holder.itemView)
        }
    }

    private fun setupFiles() {
        fileAdapter = PublishFileAdapter(
            onRemoveClick = { position ->
                fileAdapter.removeFile(position)
                binding.layoutFiles.visibility = if (fileAdapter.itemCount > 0) View.VISIBLE else View.GONE
            }
        )
        binding.layoutFiles.apply {
            orientation = LinearLayout.VERTICAL
            removeAllViews()
        }
    }

    private fun addFileItem(path: String, filename: String, size: Long, mimeType: String) {
        fileAdapter.addFile(path, filename, size, mimeType)
        binding.layoutFiles.visibility = View.VISIBLE
        binding.layoutFiles.removeAllViews()
        for (i in 0 until fileAdapter.itemCount) {
            val bindingItem = com.wenshu.app.databinding.ItemPublishFileBinding.inflate(layoutInflater, binding.layoutFiles, false)
            val file = fileAdapter.getFiles()[i]
            bindingItem.tvFileName.text = file.filename
            bindingItem.tvFileInfo.text = "${file.displaySize} · ${file.expiresText}"
            bindingItem.tvFileExt.text = file.extension
            val iconRes = when (file.fileType) {
                com.wenshu.app.data.model.FileType.PDF -> R.drawable.ic_file_pdf
                com.wenshu.app.data.model.FileType.DOC -> R.drawable.ic_file_doc
                com.wenshu.app.data.model.FileType.XLS, com.wenshu.app.data.model.FileType.EXCEL -> R.drawable.ic_file_xls
                com.wenshu.app.data.model.FileType.PPT -> R.drawable.ic_file_ppt
                com.wenshu.app.data.model.FileType.ARCHIVE -> R.drawable.ic_file_archive
                com.wenshu.app.data.model.FileType.TEXT -> R.drawable.ic_file_txt
                com.wenshu.app.data.model.FileType.MARKDOWN -> R.drawable.ic_file_md
                com.wenshu.app.data.model.FileType.CODE -> R.drawable.ic_file_code
                else -> R.drawable.ic_file_unknown
            }
            bindingItem.imgFileIcon.setImageResource(iconRes)
            bindingItem.btnRemoveFile.setOnClickListener {
                val pos = binding.layoutFiles.indexOfChild(bindingItem.root)
                if (pos >= 0) {
                    fileAdapter.removeFile(pos)
                    binding.layoutFiles.removeView(bindingItem.root)
                    binding.layoutFiles.visibility = if (fileAdapter.itemCount > 0) View.VISIBLE else View.GONE
                }
            }
            binding.layoutFiles.addView(bindingItem.root)
        }
    }

    private fun checkPermissionsAndShowPicker() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            showMediaPickerDialog()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun showMediaPickerDialog() {
        val dialogBinding = DialogMediaPickerBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnGallery.setOnClickListener {
            dialog.dismiss()
            pickFromGallery()
        }

        dialogBinding.btnCamera.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }

        dialogBinding.btnFile.setOnClickListener {
            dialog.dismiss()
            pickFile()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/* video/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        pickImageLauncher.launch(intent)
    }

    private fun pickVideoOnly() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.setType("video/*")
        pickVideoLauncher.launch(intent)
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("*/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pickFileLauncher.launch(Intent.createChooser(intent, "选择文件"))
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                photoFile
            )
            pendingCameraImageUri = photoUri
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "无法启动相机", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun handleMediaPick(uri: Uri, isVideo: Boolean) {
        try {
            val mimeType = requireContext().contentResolver.getType(uri) ?: ""
            val isActualVideo = isVideo || mimeType.startsWith("video/")

            if (isActualVideo) {
                val projection = arrayOf(MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.SIZE)
                var filename = "video_${System.currentTimeMillis()}.mp4"
                var size = 0L
                requireContext().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                        if (nameIdx >= 0) filename = cursor.getString(nameIdx) ?: filename
                        if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                    }
                }
                val tempFile = copyUriToTemp(uri, filename)
                if (tempFile != null) {
                    addVideoItem(tempFile.absolutePath, filename, size)
                }
            } else {
                val tempFile = createTempFileFromUri(uri, "image_")
                if (tempFile != null) {
                    imageAdapter.addImage(tempFile.absolutePath)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "媒体选择失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFilePick(uri: Uri) {
        try {
            val mimeType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE)
            var filename = "file_${System.currentTimeMillis()}"
            var size = 0L
            requireContext().contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    if (nameIdx >= 0) filename = cursor.getString(nameIdx) ?: filename
                    if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                }
            }
            if (!filename.contains(".")) {
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                if (ext != null) filename += ".$ext"
            }
            val tempFile = copyUriToTemp(uri, filename)
            if (tempFile != null) {
                addFileItem(tempFile.absolutePath, filename, size, mimeType)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "文件选择失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToTemp(uri: Uri, filename: String): File? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File(requireContext().cacheDir, "pub_${System.currentTimeMillis()}_$filename")
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun createTempFileFromUri(uri: Uri, prefix: String): File? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val ext = when (requireContext().contentResolver.getType(uri)) {
                "image/png" -> ".png"
                "image/gif" -> ".gif"
                "image/webp" -> ".webp"
                else -> ".jpg"
            }
            val tempFile = File(requireContext().cacheDir, "$prefix${System.currentTimeMillis()}$ext")
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun setupContent() {
        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePublishButtonState()
                updateCharCount(s?.length ?: 0)
            }
        })
        updateCharCount(0)
    }

    private fun updateCharCount(count: Int) {
        binding.tvCharacterCount.text = "$count"
    }

    private fun setupToolBar() {
        binding.btnCancel.setOnClickListener {
            if (binding.etContent.text.isNotBlank() || selectedTags.isNotEmpty() || imageAdapter.getImages().isNotEmpty() || videoAdapter.getVideos().isNotEmpty() || fileAdapter.getFiles().isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("退出编辑")
                    .setMessage("是否保存草稿？")
                    .setPositiveButton("保存") { _, _ ->
                        saveDraft()
                        clearAndNavigateHome()
                    }
                    .setNegativeButton("不保存") { _, _ ->
                        clearAndNavigateHome()
                    }
                    .setNeutralButton("取消", null)
                    .show()
            } else {
                clearAndNavigateHome()
            }
        }

        binding.btnSaveDraft.setOnClickListener {
            saveDraft()
            Toast.makeText(requireContext(), "草稿已保存", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDraft() {
        val content = binding.etContent.text.toString()
        val title = binding.etTitle.text.toString()
        draftRepository.saveDraft(content, title, selectedTags.toList(), imageAdapter.getImages())
    }

    private fun loadDraft() {
        val draft = draftRepository.loadDraft()
        if (draft != null) {
            binding.etContent.setText(draft.content)
            binding.etTitle.setText(draft.title)
            selectedTags.clear()
            selectedTags.addAll(draft.tags)
            imageAdapter.setImages(draft.imagePaths)
            updateTagDisplay()
            updatePublishButtonState()
            if (draft.title.isNotEmpty()) {
                hasTitle = true
                binding.etTitle.visibility = View.VISIBLE
            }
        }
    }

    private fun clearAndNavigateHome() {
        binding.etContent.text?.clear()
        binding.etTitle.text?.clear()
        selectedTags.clear()
        imageAdapter.setImages(emptyList())
        videoAdapter.clear()
        fileAdapter.clear()
        binding.layoutVideos.visibility = View.GONE
        binding.layoutFiles.visibility = View.GONE
        binding.layoutUrlPreviews.visibility = View.GONE
        binding.layoutUrlPreviews.removeAllViews()
        urlPreviews.clear()
        selectedLocation = ""
        binding.tvLocation.text = getString(R.string.add_location)
        isLongPost = false
        updateTagDisplay()
        draftRepository.clearDraft()
        (activity as? MainActivity)?.selectTab(R.id.nav_home)
    }

    private fun setupTagSelection() {
        binding.layoutAddTag.setOnClickListener {
            showTopicSearchDialog()
        }
        updateTagDisplay()
    }

    private fun showTopicSearchDialog() {
        val dialogBinding = com.wenshu.app.databinding.DialogTopicSearchBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        lateinit var adapter: TopicSearchAdapter
        adapter = TopicSearchAdapter { topic ->
            if (topic.isNew) {
                if (!selectedTags.contains(topic.name)) {
                    selectedTags.add(topic.name)
                }
            } else {
                if (selectedTags.contains(topic.name)) {
                    selectedTags.remove(topic.name)
                } else {
                    selectedTags.add(topic.name)
                }
            }
            updateTopicList(dialogBinding, dialogBinding.etSearchTopic.text.toString(), adapter)
            updateTagDisplay()
        }

        dialogBinding.recyclerTopics.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.recyclerTopics.adapter = adapter

        updateTopicList(dialogBinding, "", adapter)

        dialogBinding.etSearchTopic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateTopicList(dialogBinding, s?.toString() ?: "", adapter)
            }
        })

        dialogBinding.etSearchTopic.requestFocus()
        dialog.show()

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun updateTopicList(dialogBinding: com.wenshu.app.databinding.DialogTopicSearchBinding, query: String, adapter: TopicSearchAdapter) {
        val filteredTags = if (query.isBlank()) {
            defaultTags
        } else {
            defaultTags.filter { it.contains(query, ignoreCase = true) }
        }
        val items = filteredTags.map { tag ->
            TopicItem(tag, isNew = false, isSelected = selectedTags.contains(tag))
        }.toMutableList()

        if (query.isNotBlank() && !defaultTags.any { it.equals(query, ignoreCase = true) }) {
            items.add(0, TopicItem(query, isNew = true, isSelected = false))
        }

        adapter.submitList(items)
    }

    private fun updateTagDisplay() {
        binding.layoutSelectedTags.visibility = if (selectedTags.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutSelectedTags.removeAllViews()
        selectedTags.forEach { tag ->
            val tagView = layoutInflater.inflate(R.layout.item_tag_chip, binding.layoutSelectedTags, false)
            val tvTag = tagView.findViewById<TextView>(R.id.tv_tag)
            val btnRemove = tagView.findViewById<android.widget.ImageView>(R.id.btn_remove)
            tvTag.text = "#$tag"
            btnRemove.setOnClickListener {
                selectedTags.remove(tag)
                updateTagDisplay()
            }
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            }
            tagView.layoutParams = params
            binding.layoutSelectedTags.addView(tagView)
        }
    }

    private fun setupButtons() {
        binding.btnAddMedia.setOnClickListener { checkPermissionsAndShowPicker() }

        binding.btnAddTitle.setOnClickListener {
            hasTitle = !hasTitle
            binding.etTitle.visibility = if (hasTitle) View.VISIBLE else View.GONE
            if (hasTitle) {
                binding.etTitle.requestFocus()
            } else {
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etTitle.windowToken, 0)
            }
        }

        binding.btnRichEditor.setOnClickListener {
            val intent = Intent(requireContext(), RichEditorActivity::class.java)
            intent.putExtra("content", binding.etContent.text.toString())
            intent.putExtra("isLongPost", isLongPost)
            if (hasTitle) {
                intent.putExtra("title", binding.etTitle.text.toString())
            }
            richEditorLauncher.launch(intent)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.no_anim)
        }

        binding.layoutAddLocation.setOnClickListener {
            requestLocation()
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "位置功能开发中", Toast.LENGTH_SHORT).show()
            binding.tvLocation.text = "位置已选择"
            selectedLocation = "我的位置"
        } else {
            requestLocationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun addUrlPreview(preview: UrlPreview) {
        val exists = urlPreviews.any { it.url == preview.url }
        if (exists) return
        urlPreviews.add(preview)
        binding.layoutUrlPreviews.visibility = View.VISIBLE

        val itemBinding = ItemUrlPreviewBinding.inflate(layoutInflater, binding.layoutUrlPreviews, false)
        itemBinding.tvUrlTitle.text = preview.title.ifEmpty { preview.url }
        val host = try {
            java.net.URL(preview.url).host
        } catch (e: Exception) { preview.url }
        itemBinding.tvUrlSite.text = host
        if (preview.favicon != null) {
            Glide.with(requireContext())
                .load(ImageUtils.normalizeUrl(preview.favicon!!))
                .placeholder(R.drawable.ic_globe)
                .error(R.drawable.ic_globe)
                .into(itemBinding.imgFavicon)
        } else {
            itemBinding.imgFavicon.setImageResource(R.drawable.ic_globe)
        }
        itemBinding.btnRemoveUrl.setOnClickListener {
            urlPreviews.remove(preview)
            binding.layoutUrlPreviews.removeView(itemBinding.root)
            binding.layoutUrlPreviews.visibility = if (urlPreviews.isEmpty()) View.GONE else View.VISIBLE
        }
        binding.layoutUrlPreviews.addView(itemBinding.root)
    }

    private fun setupPublishButton() {
        binding.btnPublish.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            val title = binding.etTitle.text.toString().trim()

            val imagePaths = imageAdapter.getImages()
            val videoPaths = videoAdapter.getVideos()

            binding.btnPublish.isEnabled = false
            binding.btnPublish.text = "处理中..."

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val urls = extractUrls(content)
                    for (url in urls) {
                        if (urlPreviews.any { it.url == url }) continue
                        val result = withContext(Dispatchers.IO) {
                            viewModel.fetchUrlPreview(url)
                        }
                        result.onSuccess { preview ->
                            addUrlPreview(preview)
                        }
                    }

                    viewModel.publish(
                        content = content,
                        title = title,
                        imagePaths = imagePaths,
                        videoPaths = videoPaths,
                        filePaths = fileAdapter.getFiles(),
                        tags = selectedTags.toList(),
                        location = selectedLocation,
                        isLongPost = isLongPost,
                        urlPreviews = urlPreviews.toList()
                    )
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "发布失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnPublish.isEnabled = true
                    binding.btnPublish.text = getString(R.string.publish_button)
                }
            }
        }
        updatePublishButtonState()
    }

    private fun extractUrls(text: String): List<String> {
        val urlPattern = Pattern.compile("(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)")
        val matcher = urlPattern.matcher(text)
        val urls = mutableListOf<String>()
        while (matcher.find()) {
            matcher.group(1)?.let { urls.add(it) }
        }
        return urls
    }

    private fun updatePublishButtonState() {
        val hasContent = binding.etContent.text.toString().isNotBlank()
        binding.btnPublish.isEnabled = hasContent
        binding.btnPublish.alpha = if (hasContent) 1f else 0.5f
    }

    private fun observeData() {
        setupPublishButton()
        viewModel.publishResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), getString(R.string.publish_success), Toast.LENGTH_SHORT).show()
                draftRepository.clearDraft()
                clearAndNavigateHome()
                viewModel.resetPublishResult()
                (activity as? MainActivity)?.onPostPublished()
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnPublish.isEnabled = !isLoading
            binding.btnPublish.text = if (isLoading) "发布中..." else getString(R.string.publish_button)
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
