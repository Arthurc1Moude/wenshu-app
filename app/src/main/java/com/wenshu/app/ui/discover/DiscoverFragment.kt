package com.wenshu.app.ui.discover

import android.os.Bundle
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

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var trendingAdapter: TrendingTopicAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTrending()
        observeData()
    }

    private fun setupTrending() {
        trendingAdapter = TrendingTopicAdapter(
            topics = emptyList(),
            onTopicClick = { }
        )
        binding.recyclerTrending.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trendingAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.hotSearches.observe(viewLifecycleOwner) { searches ->
            binding.hotSearchLayout.removeAllViews()
            searches.forEach { search ->
                val tv = TextView(requireContext()).apply {
                    text = search
                    textSize = 14f
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
                }
                binding.hotSearchLayout.addView(tv)
            }
        }
        viewModel.trendingTopics.observe(viewLifecycleOwner) { topics ->
            trendingAdapter.updateTopics(topics)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
