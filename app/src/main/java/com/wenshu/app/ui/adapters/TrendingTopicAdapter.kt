package com.wenshu.app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wenshu.app.databinding.ItemTrendingTopicBinding

data class TrendingTopic(
    val rank: Int,
    val title: String,
    val description: String,
    val hotValue: String,
    val isHot: Boolean
)

class TrendingTopicAdapter(
    private var topics: List<TrendingTopic> = emptyList(),
    private val onTopicClick: (TrendingTopic) -> Unit
) : RecyclerView.Adapter<TrendingTopicAdapter.TopicViewHolder>() {

    inner class TopicViewHolder(val binding: ItemTrendingTopicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTrendingTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        with(holder.binding) {
            tvRank.text = topic.rank.toString()
            tvTopicTitle.text = topic.title
            tvTopicDesc.text = topic.description
            tvHotValue.text = topic.hotValue
            imgFire.visibility = if (topic.isHot) android.view.View.VISIBLE else android.view.View.GONE

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

    override fun getItemCount() = topics.size

    fun updateTopics(newTopics: List<TrendingTopic>) {
        topics = newTopics
        notifyDataSetChanged()
    }
}
