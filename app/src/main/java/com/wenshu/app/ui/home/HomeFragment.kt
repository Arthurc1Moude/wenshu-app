package com.wenshu.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.FragmentHomeBinding
import com.wenshu.app.ui.adapters.PostCardAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostCardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupCategories()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupRecyclerView() {
        postAdapter = PostCardAdapter(
            posts = emptyList(),
            onPostClick = { post -> (activity as? MainActivity)?.navigateToPostDetail(post.id) },
            onLikeClick = { post -> viewModel.toggleLike(post) },
            onUserClick = { post -> (activity as? MainActivity)?.selectTab(R.id.nav_profile) }
        )
        binding.recyclerPosts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = postAdapter
            itemAnimator = null
        }
    }

    private fun setupCategories() {
        val cats = PostRepository.getCategories()
        binding.chipGroup.removeAllViews()
        cats.forEach { (key, label, _) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = key == "recommend"
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_background_selector)
                textSize = 15f
                setEnsureMinTouchTargetSize(false)
                setOnClickListener { viewModel.loadPosts(key) }
            }
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            )
            params.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_sm)
            binding.chipGroup.addView(chip, params)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary))
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }
        binding.btnSearch.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_discover)
        }
    }

    private fun observeData() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.updatePosts(posts)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
