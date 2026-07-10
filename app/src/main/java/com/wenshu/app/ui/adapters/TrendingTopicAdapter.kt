package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wenshu.app.databinding.ItemTrendingTopicBinding
import com.wenshu.app.ui.discover.TrendingTopic

class TrendingTopicAdapter(
    private val onTopicClick: (TrendingTopic) -> Unit
) : ListAdapter<TrendingTopic, TrendingTopicAdapter.TopicViewHolder>(TopicDiffCallback()) {

    inner class TopicViewHolder(val binding: ItemTrendingTopicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTrendingTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = getItem(position)
        with(holder.binding) {
            tvRank.text = topic.rank.toString()
            tvTopicTitle.text = topic.title
            tvTopicDesc.visibility = View.GONE
            tvHotValue.text = topic.heat
            imgFire.visibility = if (topic.rank in 1..3) View.VISIBLE else View.GONE

            tvRank.setTextColor(root.context.getColor(
                when (topic.rank) {
                    1 -> android.graphics.Color.parseColor("#FF2442")
                    2 -> android.graphics.Color.parseColor("#FF6B35")
                    3 -> android.graphics.Color.parseColor("#FFAB00")
                    else -> android.graphics.Color.parseColor("#999999")
                }
            ))

            root.setOnClickListener { onTopicClick(topic) }
        }
    }

    class TopicDiffCallback : DiffUtil.ItemCallback<TrendingTopic>() {
        override fun areItemsTheSame(oldItem: TrendingTopic, newItem: TrendingTopic): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: TrendingTopic, newItem: TrendingTopic): Boolean {
            return oldItem == newItem
        }
    }
}
