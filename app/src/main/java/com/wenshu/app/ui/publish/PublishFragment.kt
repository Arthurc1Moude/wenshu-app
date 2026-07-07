package com.wenshu.app.ui.publish

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentPublishBinding
import com.wenshu.app.util.MockDataGenerator

class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PublishViewModel by viewModels()
    private val availableLocations = listOf("北京·朝阳区", "上海·静安区", "广州·天河区", "深圳·南山区", "杭州·西湖区", "成都·锦江区")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImagesRecycler()
        setupTagSelection()
        setupLocationSelection()
        setupPublishButton()
        setupCancelButton()
        observeData()
    }

    private fun setupImagesRecycler() {
        binding.recyclerImages.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = ImagePreviewAdapter(
                items = emptyList(),
                onAddClick = { addMockImage() },
                onRemoveClick = { uri -> viewModel.removeImage(uri) }
            )
        }
        updateImages(emptyList())
    }

    private fun addMockImage() {
        val newUri = Uri.parse("https://picsum.photos/seed/pub${System.currentTimeMillis()}/400/400")
        viewModel.addImage(newUri)
    }

    private fun updateImages(images: List<Uri>) {
        val items = images.toMutableList<Any>()
        if (images.size < 9) items.add("add_button")
        (binding.recyclerImages.adapter as? ImagePreviewAdapter)?.updateItems(items)
    }

    private fun setupTagSelection() {
        binding.layoutAddTag.setOnClickListener {
            showTagDialog()
        }
    }

    private fun showTagDialog() {
        val tags = MockDataGenerator.getHotSearches().take(8)

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("选择话题")

        val selectedTags = viewModel.selectedTags.value?.toMutableList() ?: mutableListOf()
        val checkedItems = tags.map { selectedTags.contains(it) }.toBooleanArray()
        builder.setMultiChoiceItems(tags.toTypedArray(), checkedItems) { _, which, isChecked ->
            val tag = tags[which]
            viewModel.toggleTag(tag)
        }
        builder.setPositiveButton("确定", null)
        builder.show()
    }

    private fun setupLocationSelection() {
        binding.layoutAddLocation.setOnClickListener {
            val options = availableLocations.toMutableList<String?>()
            options.add(0, null)
            val labels = options.map { it ?: "不显示位置" }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("选择位置")
                .setItems(labels) { _, which ->
                    viewModel.setLocation(options[which])
                }
                .show()
        }
    }

    private fun setupPublishButton() {
        binding.btnPublish.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.publish(title, content)
        }

        binding.etContent.doAfterTextChanged {
            updatePublishButtonState()
        }
    }

    private fun updatePublishButtonState() {
        val hasContent = binding.etContent.text.toString().isNotBlank()
        binding.btnPublish.isEnabled = hasContent
        binding.btnPublish.alpha = if (hasContent) 1f else 0.5f
    }

    private fun setupCancelButton() {
        binding.btnCancel.setOnClickListener {
            binding.etTitle.text?.clear()
            binding.etContent.text?.clear()
            viewModel.resetPublish()
            (activity as? MainActivity)?.selectTab(R.id.nav_home)
        }
    }

    private fun observeData() {
        viewModel.selectedImages.observe(viewLifecycleOwner) { images ->
            updateImages(images)
        }
        viewModel.selectedTags.observe(viewLifecycleOwner) { tags ->
            binding.tvAddTag.text = if (tags.isEmpty()) "#" else tags.joinToString(" #", prefix = "#")
        }
        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            binding.tvLocation.text = location ?: getString(R.string.add_location)
        }
        viewModel.publishSuccess.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                Toast.makeText(requireContext(), getString(R.string.publish_success), Toast.LENGTH_SHORT).show()
                binding.etTitle.text?.clear()
                binding.etContent.text?.clear()
                viewModel.resetPublish()
                (activity as? MainActivity)?.onPostPublished()
            }
        }
        updatePublishButtonState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ImagePreviewAdapter(
    private var items: List<Any>,
    private val onAddClick: () -> Unit,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_ADD = 1
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPreview: ImageView = itemView.findViewById(R.id.img_preview)
        val btnRemove: ImageView = itemView.findViewById(R.id.btn_remove)
    }

    inner class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String && items[position] == "add_button") TYPE_ADD else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_IMAGE) {
            val view = inflater.inflate(R.layout.item_publish_image, parent, false)
            ImageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_add_image, parent, false)
            AddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            val uri = items[position] as Uri
            com.bumptech.glide.Glide.with(holder.imgPreview.context)
                .load(uri)
                .centerCrop()
                .into(holder.imgPreview)
            holder.btnRemove.visibility = View.VISIBLE
            holder.btnRemove.setOnClickListener { onRemoveClick(uri) }
        } else if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener { onAddClick() }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }
}
