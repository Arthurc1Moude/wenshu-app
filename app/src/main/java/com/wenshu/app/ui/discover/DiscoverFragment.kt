package com.wenshu.app.ui.discover

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        Log.d("DiscoverFragment", "onCreateView")
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DiscoverFragment", "onViewCreated")
        try {
            setupTrending()
            setupSearch()
            setupHotSearches()
            setupSwipeRefresh()
            observeData()
        } catch (e: Exception) {
            Log.e("DiscoverFragment", "Error in onViewCreated", e)
        }
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
            try {
                startActivity(Intent(requireContext(), SearchActivity::class.java))
            } catch (e: Exception) {
                Log.e("DiscoverFragment", "Error starting SearchActivity", e)
            }
        }
        binding.etSearch.setOnClickListener {
            try {
                startActivity(Intent(requireContext(), SearchActivity::class.java))
            } catch (e: Exception) {
                Log.e("DiscoverFragment", "Error starting SearchActivity", e)
            }
        }
    }

    private fun setupHotSearches() {
        binding.hotSearchLayout.removeAllViews()
        hotSearches.forEach { search ->
            val tv = TextView(requireContext()).apply {
                text = search
                textSize = 14f
                typeface = Typeface.SERIF
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setBackgroundResource(R.drawable.bg_tag)
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.spacing_md),
                    resources.getDimensionPixelSize(R.dimen.spacing_sm),
                    resources.getDimensionPixelSize(R.dimen.spacing_md),
                    resources.getDimensionPixelSize(R.dimen.spacing_sm)
                )
                val params = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
                params.bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_sm)
                layoutParams = params
                setOnClickListener {
                    try {
                        val intent = Intent(requireContext(), SearchActivity::class.java)
                        intent.putExtra("query", search)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("DiscoverFragment", "Error searching for: $search", e)
                    }
                }
            }
            binding.hotSearchLayout.addView(tv)
        }
    }

    private fun setupTrending() {
        trendingAdapter = TrendingTopicAdapter(
            onTopicClick = { topic ->
                try {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra("query", topic.title)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("DiscoverFragment", "Error clicking topic: ${topic.title}", e)
                }
            }
        )
        binding.recyclerTrending.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trendingAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.trendingTags.observe(viewLifecycleOwner) { topics ->
            Log.d("DiscoverFragment", "Trending topics updated: ${topics?.size ?: 0}")
            trendingAdapter.submitList(topics ?: emptyList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
