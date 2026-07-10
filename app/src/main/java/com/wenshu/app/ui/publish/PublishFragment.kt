package com.wenshu.app.ui.publish

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.data.repository.DraftRepository
import com.wenshu.app.databinding.DialogTopicSearchBinding
import com.wenshu.app.databinding.FragmentPublishBinding
import com.wenshu.app.ui.adapters.PublishImageAdapter
import com.wenshu.app.ui.adapters.TopicItem
import com.wenshu.app.ui.adapters.TopicSearchAdapter
import java.io.File

class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PublishViewModel by viewModels()
    private lateinit var imageAdapter: PublishImageAdapter
    private lateinit var draftRepository: DraftRepository

    private val defaultTags = listOf("夏日生活", "日常打卡", "读书分享", "美食探店", "摄影日记", "穿搭分享", "旅行日记", "心情随笔", "生活记录", "每日一思")
    private val selectedTags = mutableListOf<String>()
    private var hasTitle = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImagePick(uri)
            }
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
        observeData()
        loadDraft()
    }

    private fun setupImages() {
        imageAdapter = PublishImageAdapter(
            onAddClick = { pickFromGallery() },
            onRemoveClick = { position -> imageAdapter.removeImage(position) }
        )
        binding.recyclerImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imageAdapter
            itemAnimator = null
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        pickImageLauncher.launch(intent)
    }

    private fun handleImagePick(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File(requireContext().cacheDir, "publish_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            imageAdapter.addImage(tempFile.absolutePath)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "图片选择失败", Toast.LENGTH_SHORT).show()
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
            if (binding.etContent.text.isNotBlank() || selectedTags.isNotEmpty() || imageAdapter.getImages().isNotEmpty()) {
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
            val tvTag = tagView.findViewById<android.widget.TextView>(R.id.tv_tag)
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
        binding.btnAddImage.setOnClickListener { pickFromGallery() }
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
    }

    private fun setupPublishButton() {
        binding.btnPublish.setOnClickListener {
            val content = binding.etContent.text.toString().trim()
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val title = binding.etTitle.text.toString().trim()
            val imagePaths = imageAdapter.getImages()
            viewModel.publish(content, title, imagePaths, selectedTags.toList())
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
                binding.etContent.text?.clear()
                binding.etTitle.text?.clear()
                selectedTags.clear()
                imageAdapter.setImages(emptyList())
                updateTagDisplay()
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
