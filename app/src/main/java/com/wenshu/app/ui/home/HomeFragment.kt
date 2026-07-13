package com.wenshu.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentHomeBinding
import com.wenshu.app.ui.adapters.PostCardAdapter
import com.wenshu.app.ui.books.BooksActivity
import com.wenshu.app.ui.games.GamesActivity
import com.wenshu.app.ui.miniapps.MiniAppsActivity
import com.wenshu.app.ui.official.OfficialPostsActivity
import com.wenshu.app.ui.search.SearchActivity
import com.wenshu.app.ui.secret.SecretSpaceActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostCardAdapter
    private var featuresVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("HomeFragment", "onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "onViewCreated")
        setupRecyclerView()
        setupTabs()
        setupSwipeRefresh()
        setupTopBarGesture()
        setupRetryButton()
        setupFeaturePanel()
        observeData()
        Log.d("HomeFragment", "setup complete")
    }

    private fun setupRecyclerView() {
        Log.d("HomeFragment", "setupRecyclerView")
        postAdapter = PostCardAdapter(
            onPostClick = { post -> (activity as? MainActivity)?.navigateToPostDetail(post.id) },
            onLikeClick = { post -> viewModel.toggleLike(post.id) },
            onCoinClick = { post ->
                val amount = com.wenshu.app.data.SharedPreferencesManager.getDefaultTipAmount()
                viewModel.tipPost(post.id, amount)
            },
            onUserClick = { (activity as? MainActivity)?.selectTab(R.id.nav_profile) }
        )
        binding.recyclerPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
            itemAnimator = null
        }
    }

    private fun setupTabs() {
        val tabs = listOf(0 to "最新", 1 to "热门")
        binding.chipGroup.removeAllViews()
        tabs.forEach { (index, label) ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = index == 0
                setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.chip_background_selector)
                textSize = 15f
                typeface = android.graphics.Typeface.SERIF
                setEnsureMinTouchTargetSize(false)
                setOnClickListener { viewModel.setTab(index) }
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
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.seal))
        binding.swipeRefresh.setOnRefreshListener {
            if (featuresVisible) {
                binding.swipeRefresh.isRefreshing = false
            } else {
                viewModel.refresh()
            }
        }
        binding.btnSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    private fun setupTopBarGesture() {
        val topBar = binding.topBar

        topBar.setOnClickListener {
            if (!featuresVisible) {
                toggleFeaturePanel()
            }
        }

        topBar.onSwipeDown = {
            if (!featuresVisible) {
                showFeaturePanel()
            }
        }
    }

    private fun setupFeaturePanel() {
        binding.pullHint.setOnClickListener { }
        binding.btnCloseFeatures.setOnClickListener { hideFeaturePanel() }
        binding.btnBooks.setOnClickListener { startActivity(Intent(requireContext(), BooksActivity::class.java).putExtra("type", "book")) }
        binding.btnNovels.setOnClickListener { startActivity(Intent(requireContext(), BooksActivity::class.java).putExtra("type", "novel")) }
        binding.btnMiniapps.setOnClickListener { startActivity(Intent(requireContext(), MiniAppsActivity::class.java)) }
        binding.btnWorld.setOnClickListener { startActivity(Intent(requireContext(), OfficialPostsActivity::class.java)) }
        binding.btnGames.setOnClickListener { startActivity(Intent(requireContext(), GamesActivity::class.java)) }
        binding.btnSpace.setOnClickListener { startActivity(Intent(requireContext(), SecretSpaceActivity::class.java)) }
    }

    private fun showFeaturePanel() {
        if (featuresVisible) return
        featuresVisible = true
        val panel = binding.featurePanel
        panel.visibility = View.VISIBLE
        panel.post {
            val height = panel.height.toFloat()
            if (height > 0) {
                panel.translationY = -height
                panel.animate()
                    .translationY(0f)
                    .setDuration(280)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }
        binding.swipeRefresh.isRefreshing = false
    }

    private fun hideFeaturePanel() {
        if (!featuresVisible) return
        val panel = binding.featurePanel
        val height = if (panel.height > 0) panel.height.toFloat() else resources.displayMetrics.heightPixels.toFloat()
        panel.animate()
            .translationY(-height)
            .setDuration(220)
            .setInterpolator(android.view.animation.AccelerateInterpolator())
            .withEndAction {
                panel.visibility = View.GONE
                panel.translationY = 0f
                featuresVisible = false
            }
            .start()
    }

    private fun toggleFeaturePanel() {
        if (featuresVisible) hideFeaturePanel() else showFeaturePanel()
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun observeData() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            Log.d("HomeFragment", "Posts updated: ${posts?.size ?: 0} posts")
            postAdapter.submitList(posts)
            updateEmptyState(posts.isNullOrEmpty())
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("HomeFragment", "Loading: $isLoading")
            if (!featuresVisible) {
                binding.swipeRefresh.isRefreshing = isLoading
            }
            val shouldShowLoading = isLoading && postAdapter.itemCount == 0
            binding.progressLoading.visibility = if (shouldShowLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                val isEmpty = postAdapter.itemCount == 0
                binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.e("HomeFragment", "Error: $error")
            error?.let {
                if (postAdapter.itemCount == 0) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.progressLoading.visibility = View.GONE
                } else {
                    try {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error showing snackbar", e)
                    }
                }
                viewModel.clearError()
            }
        }
        viewModel.tipResult.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                try {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                viewModel.clearTipResult()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val isLoading = viewModel.isLoading.value ?: false
        if (isEmpty && !isLoading) {
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshPosts() {
        viewModel.refresh()
    }
}
