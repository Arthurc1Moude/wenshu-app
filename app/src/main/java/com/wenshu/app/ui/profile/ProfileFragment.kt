package com.wenshu.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentProfileBinding
import com.wenshu.app.ui.adapters.PostGridAdapter
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.util.TimeUtils

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var gridAdapter: PostGridAdapter
    private var currentTab = "my"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupCoverPhoto()
        setupTabs()
        setupGrid()
        observeData()
    }

    private fun setupCoverPhoto() {
        Glide.with(this)
            .load("https://picsum.photos/seed/wenshu-cover/800/400")
            .centerCrop()
            .into(binding.imgCover)
    }

    private fun setupUserInfo() {
        binding.btnEditProfile.setOnClickListener { }
        binding.btnSettings.setOnClickListener { showSettingsSheet() }
        binding.btnShareProfile.setOnClickListener { }
    }

    private fun showSettingsSheet() {
        val options = arrayOf(getString(R.string.settings), getString(R.string.feedback), getString(R.string.about), getString(R.string.logout))
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setItems(options) { dialog, which ->
            when (which) {
                3 -> { }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setupTabs() {
        val tabs = listOf(
            "my" to getString(R.string.my_posts),
            "liked" to getString(R.string.my_likes),
            "collected" to getString(R.string.my_collects)
        )
        tabs.forEach { (key, label) ->
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_xl) * 2
                }
            }

            val tv = TextView(requireContext()).apply {
                text = label
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                tag = "tab_$key"
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            container.addView(tv)

            val indicator = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(20.dpToPx(), 3.dpToPx()).apply {
                    topMargin = 4.dpToPx()
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
                tag = "indicator_$key"
            }
            container.addView(indicator)

            container.setOnClickListener { selectTab(key) }
            binding.tabLayout.addView(container)
        }
        updateTabSelection()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun selectTab(key: String) {
        currentTab = key
        updateTabSelection()
        updateGrid()
    }

    private fun updateTabSelection() {
        val black = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val gray = ContextCompat.getColor(requireContext(), R.color.text_tertiary)
        val tabs = listOf("my", "liked", "collected")
        for (key in tabs) {
            val tabView = binding.tabLayout.findViewWithTag<TextView>("tab_$key")
            val indicator = binding.tabLayout.findViewWithTag<View>("indicator_$key")
            val isSelected = key == currentTab
            tabView?.setTextColor(if (isSelected) black else gray)
            tabView?.typeface = if (isSelected) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            tabView?.textSize = if (isSelected) 16f else 15f
            indicator?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setupGrid() {
        gridAdapter = PostGridAdapter(emptyList()) { post ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }
        binding.recyclerPosts.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = gridAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            Glide.with(this)
                .load(user.avatarUrl)
                .placeholder(R.drawable.bg_circle_placeholder)
                .into(binding.imgAvatar)
            binding.tvNickname.text = user.nickname
            binding.tvBio.text = user.bio
            binding.tvFollowingCount.text = TimeUtils.formatCount(user.followingCount.toLong())
            binding.tvFollowersCount.text = TimeUtils.formatCount(user.followersCount.toLong())
            binding.tvLikedCount.text = TimeUtils.formatCount(user.totalLikesCount.toLong())

            if (user.bio.isBlank()) {
                binding.tvBio.visibility = View.GONE
            }
        }
        viewModel.myPosts.observe(viewLifecycleOwner) { if (currentTab == "my") updateGrid() }
        viewModel.likedPosts.observe(viewLifecycleOwner) { if (currentTab == "liked") updateGrid() }
        viewModel.collectedPosts.observe(viewLifecycleOwner) { if (currentTab == "collected") updateGrid() }
        updateGrid()
    }

    private fun updateGrid() {
        val posts = viewModel.getCurrentTabPosts(currentTab)
        gridAdapter.updatePosts(posts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
