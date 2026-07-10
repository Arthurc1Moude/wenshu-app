package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wenshu.app.databinding.ItemTopicSearchBinding

data class TopicItem(
    val name: String,
    val isNew: Boolean = false,
    val isSelected: Boolean = false
)

class TopicSearchAdapter(
    private val onTopicClick: (TopicItem) -> Unit
) : ListAdapter<TopicItem, TopicSearchAdapter.TopicViewHolder>(TopicDiffCallback()) {

    inner class TopicViewHolder(val binding: ItemTopicSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = getItem(position)
        with(holder.binding) {
            tvTagName.text = "#${topic.name}"
            tvTagNew.visibility = if (topic.isNew) android.view.View.VISIBLE else android.view.View.GONE
            ivCheck.visibility = if (topic.isSelected) android.view.View.VISIBLE else android.view.View.GONE
            root.setOnClickListener { onTopicClick(topic) }
        }
    }

    class TopicDiffCallback : DiffUtil.ItemCallback<TopicItem>() {
        override fun areItemsTheSame(oldItem: TopicItem, newItem: TopicItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: TopicItem, newItem: TopicItem): Boolean {
            return oldItem == newItem
        }
    }
}
