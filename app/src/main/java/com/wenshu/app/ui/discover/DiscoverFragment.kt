package com.wenshu.app.ui.discover

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentDiscoverBinding
import com.wenshu.app.ui.adapters.TrendingTopicAdapter
import com.wenshu.app.ui.search.SearchActivity

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var trendingAdapter: TrendingTopicAdapter

    private val hotSearches = listOf("夏日生活", "日常打卡", "读书分享", "美食探店", "摄影日记", "穿搭分享")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTrending()
        setupSearch()
        setupHotSearches()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.seal))
        binding.swipeRefresh.setOnRefreshListener {
            loadTopics()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun loadTopics() {
        viewModel.loadTrendingTags()
    }

    private fun setupSearch() {
        binding.searchBar.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
        binding.etSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    private fun setupHotSearches() {
        val ctx = context ?: return
        binding.hotSearchLayout.removeAllViews()

        var currentRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val margin8 = (8 * resources.displayMetrics.density).toInt()
        val padH = (12 * resources.displayMetrics.density).toInt()
        val padV = (8 * resources.displayMetrics.density).toInt()

        hotSearches.forEachIndexed { index, search ->
            val tv = TextView(ctx).apply {
                text = search
                textSize = 14f
                typeface = Typeface.SERIF
                setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                setBackgroundResource(R.drawable.bg_tag)
                setPadding(padH, padV, padH, padV)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = margin8
                    bottomMargin = margin8
                }
                setOnClickListener {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra("query", search)
                    startActivity(intent)
                }
            }
            currentRow.addView(tv)

            if ((index + 1) % 3 == 0 && index < hotSearches.size - 1) {
                binding.hotSearchLayout.addView(currentRow)
                currentRow = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }
        if (currentRow.childCount > 0) {
            binding.hotSearchLayout.addView(currentRow)
        }
    }

    private fun setupTrending() {
        val ctx = context ?: return
        trendingAdapter = TrendingTopicAdapter(
            onTopicClick = { topic ->
                val intent = Intent(requireContext(), SearchActivity::class.java)
                intent.putExtra("query", topic.title)
                startActivity(intent)
            }
        )
        binding.recyclerTrending.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = trendingAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.trendingTags.observe(viewLifecycleOwner) { topics ->
            if (::trendingAdapter.isInitialized) {
                trendingAdapter.submitList(topics ?: emptyList())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
