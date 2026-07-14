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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.data.repository.DraftRepository
import com.wenshu.app.databinding.DialogMediaPickerBinding
import com.wenshu.app.databinding.DialogTopicSearchBinding
import com.wenshu.app.databinding.FragmentPublishBinding
import com.wenshu.app.ui.adapters.PublishImageAdapter
import com.wenshu.app.ui.adapters.TopicItem
import com.wenshu.app.ui.adapters.TopicSearchAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PublishViewModel by viewModels()
    private lateinit var imageAdapter: PublishImageAdapter
    private lateinit var draftRepository: DraftRepository

    private val defaultTags = listOf("夏日生活", "日常打卡", "读书分享", "美食探店", "摄影日记", "穿搭分享", "旅行日记", "心情随笔", "生活记录", "每日一思")
    private val selectedTags = mutableListOf<String>()
    private val pendingFiles = mutableListOf<PendingFile>()
    private val videoPaths = mutableListOf<String>()
    private var hasTitle = false
    private var isLongTextMode = false
    private var selectedLocation: String? = null
    private var currentPhotoPath: String? = null

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleMediaPick(uri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    imageAdapter.addImage(path)
                }
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

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            showMediaPickerDialog()
        } else {
            Toast.makeText(requireContext(), "需要相关权限才能选择媒体", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            requestLocation()
        } else {
            Toast.makeText(requireContext(), "需要位置权限才能添加位置", Toast.LENGTH_SHORT).show()
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
        setupContent()
        setupTagSelection()
        setupButtons()
        setupToolBar()
        setupLocation()
        observeData()
        loadDraft()
    }

    private fun setupImages() {
        imageAdapter = PublishImageAdapter(
            onAddClick = { checkPermissionsAndShowMediaPicker() },
            onRemoveClick = { position -> imageAdapter.removeImage(position) }
        )
        binding.recyclerImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imageAdapter
            itemAnimator = null
        }
    }

    private fun checkPermissionsAndShowMediaPicker() {
        val requiredPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            requiredPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            showMediaPickerDialog()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun showMediaPickerDialog() {
        val dialogBinding = DialogMediaPickerBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnPickGallery.setOnClickListener {
            pickFromGallery()
            dialog.dismiss()
        }
        dialogBinding.btnPickCamera.setOnClickListener {
            checkCameraPermissionAndTake()
            dialog.dismiss()
        }
        dialogBinding.btnPickFile.setOnClickListener {
            pickFile()
            dialog.dismiss()
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun checkCameraPermissionAndTake() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            takePicture()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun takePicture() {
        try {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            takePictureLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "相机启动失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        pickMediaLauncher.launch(Intent.createChooser(intent, "选择图片或视频"))
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        pickFileLauncher.launch(Intent.createChooser(intent, "选择文件"))
    }

    private fun handleMediaPick(uri: Uri) {
        try {
            val mimeType = requireContext().contentResolver.getType(uri) ?: ""
            when {
                mimeType.startsWith("video/") -> {
                    val tempFile = File(requireContext().cacheDir, "video_${System.currentTimeMillis()}.mp4")
                    copyUriToFile(uri, tempFile)
                    videoPaths.add(tempFile.absolutePath)
                    imageAdapter.addImage(tempFile.absolutePath)
                }
                mimeType.startsWith("image/") -> {
                    val tempFile = File(requireContext().cacheDir, "media_${System.currentTimeMillis()}.jpg")
                    copyUriToFile(uri, tempFile)
                    imageAdapter.addImage(tempFile.absolutePath)
                }
                else -> {
                    handleFilePick(uri)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "选择失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFilePick(uri: Uri) {
        try {
            val mimeType = requireContext().contentResolver.getType(uri) ?: "*/*"
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            var fileName = "unknown_file"
            var fileSize = 0L
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeIndex = c.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    if (nameIndex >= 0) fileName = c.getString(nameIndex) ?: "unknown_file"
                    if (sizeIndex >= 0) fileSize = c.getLong(sizeIndex)
                }
            }

            val ext = fileName.substringAfterLast(".", "")
            val tempFile = File(requireContext().cacheDir, "file_${System.currentTimeMillis()}.$ext")
            copyUriToFile(uri, tempFile)

            val pendingFile = PendingFile(
                uri = uri,
                name = fileName,
                size = fileSize,
                ext = ext,
                localPath = tempFile.absolutePath
            )
            pendingFiles.add(pendingFile)
            updateFilesDisplay()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "文件选择失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyUriToFile(uri: Uri, destFile: File) {
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun updateFilesDisplay() {
        binding.layoutSelectedFiles.visibility = if (pendingFiles.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutSelectedFiles.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        for ((index, file) in pendingFiles.withIndex()) {
            val view = inflater.inflate(R.layout.item_file_attachment, binding.layoutSelectedFiles, false)
            val imgIcon = view.findViewById<ImageView>(R.id.img_file_icon)
            val tvName = view.findViewById<TextView>(R.id.tv_file_name)
            val tvInfo = view.findViewById<TextView>(R.id.tv_file_info)
            val btnRemove = view.findViewById<ImageView>(R.id.btn_remove_file)

            tvName.text = file.name
            tvInfo.text = formatFileSize(file.size)

            val iconRes = getFileIconByExt(file.ext)
            imgIcon.setImageResource(iconRes)

            btnRemove.visibility = View.VISIBLE
            btnRemove.setOnClickListener {
                pendingFiles.removeAt(index)
                updateFilesDisplay()
            }

            binding.layoutSelectedFiles.addView(view)
        }
    }

    private fun getFileIconByExt(ext: String): Int {
        return when (ext.lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> R.drawable.ic_file_image
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm" -> R.drawable.ic_file_video
            "mp3", "wav", "flac", "aac", "ogg", "m4a" -> R.drawable.ic_file_audio
            "doc", "docx", "pdf", "txt", "rtf", "odt" -> R.drawable.ic_file_document
            "xls", "xlsx", "csv", "ods" -> R.drawable.ic_file_spreadsheet
            "ppt", "pptx", "odp" -> R.drawable.ic_file_presentation
            "zip", "rar", "7z", "tar", "gz", "bz2" -> R.drawable.ic_file_archive
            "js", "ts", "py", "java", "cpp", "c", "html", "css", "json", "xml", "kt", "swift" -> R.drawable.ic_file_code
            else -> R.drawable.ic_file_generic
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
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
            if (binding.etContent.text.isNotBlank() || selectedTags.isNotEmpty() ||
                imageAdapter.getImages().isNotEmpty() || pendingFiles.isNotEmpty()) {
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
        pendingFiles.clear()
        videoPaths.clear()
        selectedLocation = null
        isLongTextMode = false
        hasTitle = false
        imageAdapter.setImages(emptyList())
        updateTagDisplay()
        updateFilesDisplay()
        binding.tvLocation.text = getString(R.string.add_location)
        binding.etTitle.visibility = View.GONE
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
        val dialogBinding = DialogTopicSearchBinding.inflate(LayoutInflater.from(requireContext()))
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

    private fun updateTopicList(dialogBinding: DialogTopicSearchBinding, query: String, adapter: TopicSearchAdapter) {
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
            val btnRemove = tagView.findViewById<ImageView>(R.id.btn_remove)
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

    private fun setupLocation() {
        binding.layoutAddLocation.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            requestLocation()
        } else {
            requestLocationPermissionLauncher.launch(permissions)
        }
    }

    private fun requestLocation() {
        val locations = listOf("不添加位置", "当前位置", "北京市", "上海市", "广州市", "深圳市", "杭州市", "成都市")
        AlertDialog.Builder(requireContext())
            .setTitle("选择位置")
            .setItems(locations.toTypedArray()) { _, which ->
                if (which == 1) {
                    selectedLocation = "我的位置"
                    binding.tvLocation.text = "📍 我的位置"
                    binding.tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                } else if (which > 1) {
                    selectedLocation = locations[which]
                    binding.tvLocation.text = "📍 ${locations[which]}"
                    binding.tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                } else {
                    selectedLocation = null
                    binding.tvLocation.text = getString(R.string.add_location)
                    binding.tvLocation.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                }
            }
            .show()
    }

    private fun setupButtons() {
        binding.btnAddMedia.setOnClickListener { checkPermissionsAndShowMediaPicker() }
        binding.btnAttachFile.setOnClickListener { pickFile() }
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
        binding.btnLongText.setOnClickListener {
            isLongTextMode = !isLongTextMode
            if (isLongTextMode) {
                binding.btnLongText.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
                Toast.makeText(requireContext(), "长文模式已开启", Toast.LENGTH_SHORT).show()
            } else {
                binding.btnLongText.clearColorFilter()
                Toast.makeText(requireContext(), "已切换到普通模式", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPublishButton() {
        binding.btnPublish.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val title = binding.etTitle.text.toString().trim()
            val imagePaths = imageAdapter.getImages().filter { path ->
                !videoPaths.contains(path)
            }
            val tags = selectedTags.toList()

            binding.btnPublish.isEnabled = false
            binding.btnPublish.text = "发布中..."

            viewModel.publish(
                content = content,
                title = title,
                imagePaths = imagePaths,
                videoPaths = videoPaths,
                tags = tags,
                files = pendingFiles.toList(),
                isLongText = isLongTextMode,
                location = selectedLocation
            )
        }
        updatePublishButtonState()
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
        viewModel.uploadProgress.observe(viewLifecycleOwner) { (completed, total) ->
            if (total > 0) {
                binding.btnPublish.text = "上传中 $completed/$total"
            }
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
