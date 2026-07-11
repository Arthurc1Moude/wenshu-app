package com.wenshu.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentProfileBinding
import com.wenshu.app.ui.adapters.PostGridAdapter
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.ui.settings.SettingsActivity
import com.wenshu.app.ui.settings.SignInActivity
import com.wenshu.app.util.ImageUtils

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var gridAdapter: PostGridAdapter
    private var currentTab = "my"
    private var hasSignedInToday = false

    private val signInLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadUserProfile()
        }
    }

    private val editProfileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.loadUserProfile()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInfo()
        setupTabs()
        setupGrid()
        observeData()
    }

    private fun setupUserInfo() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.btnShareProfile.setOnClickListener {
            shareProfile()
        }
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }
        binding.btnSignIn.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            signInLauncher.launch(intent)
        }
        binding.btnFollow.setOnClickListener {
            Toast.makeText(requireContext(), "关注功能", Toast.LENGTH_SHORT).show()
        }
        binding.btnMessage.setOnClickListener {
            Toast.makeText(requireContext(), "消息功能开发中", Toast.LENGTH_SHORT).show()
        }

        binding.btnFollow.visibility = View.GONE
        binding.btnMessage.visibility = View.GONE
    }

    private fun shareProfile() {
        val user = viewModel.user.value ?: return
        val shareUrl = "https://wenshucom.vercel.app/${user.username}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "来看看 ${user.displayName} 在文书上的分享：$shareUrl")
        }
        startActivity(Intent.createChooser(shareIntent, "分享个人主页"))
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
                typeface = Typeface.SERIF
            }
            container.addView(tv)

            val indicator = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(20.dpToPx(), 2.dpToPx()).apply {
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
        when (key) {
            "liked" -> viewModel.loadLikedPosts()
            "collected" -> viewModel.loadSavedPosts()
        }
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
            tabView?.typeface = if (isSelected) Typeface.create("serif", Typeface.BOLD) else Typeface.SERIF
            tabView?.textSize = if (isSelected) 16f else 15f
            indicator?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun setupGrid() {
        gridAdapter = PostGridAdapter { post ->
            val intent = Intent(requireContext(), PostDetailActivity::class.java)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }
        binding.recyclerPosts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = gridAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Glide.with(this)
                    .load(ImageUtils.normalizeUrl(user.cover))
                    .placeholder(R.color.paper)
                    .error(R.color.paper)
                    .centerCrop()
                    .into(binding.imgCover)

                Glide.with(this)
                    .load(ImageUtils.normalizeUrl(user.avatar))
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .into(binding.imgAvatar)

                binding.tvNickname.text = user.displayName
                binding.tvBio.text = user.bio ?: ""
                binding.tvFollowingCount.text = user.followingCount.toString()
                binding.tvFollowersCount.text = user.followersCount.toString()
                binding.tvLikedCount.text = user.likesCount.toString()
                binding.tvWenshuCoin.text = user.wenshuCoin.toString()

                binding.tvVipBadge.visibility = if (user.isVip) View.VISIBLE else View.GONE
                binding.tvBio.visibility = if (user.bio.isNullOrBlank()) View.GONE else View.VISIBLE

                if (user.isSignedInToday) {
                    binding.btnSignIn.text = "今日已签"
                    binding.btnSignIn.isEnabled = false
                    binding.btnSignIn.alpha = 0.6f
                } else {
                    binding.btnSignIn.text = "每日签到"
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.alpha = 1f
                }

                if (!user.location.isNullOrBlank()) {
                    binding.layoutLocationProfile.visibility = View.VISIBLE
                    binding.tvLocation.text = user.location
                } else {
                    binding.layoutLocationProfile.visibility = View.GONE
                }

                hasSignedInToday = user.isSignedInToday
                binding.btnEditProfile.text = "编辑资料"
            }
        }
        viewModel.userPosts.observe(viewLifecycleOwner) { if (currentTab == "my") updateGrid() }
        viewModel.likedPosts.observe(viewLifecycleOwner) { if (currentTab == "liked") updateGrid() }
        viewModel.savedPosts.observe(viewLifecycleOwner) { if (currentTab == "collected") updateGrid() }

        viewModel.signInResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSignInResult()
            }
        }
        viewModel.followResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearFollowResult()
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        updateGrid()
    }

    private fun updateGrid() {
        val posts = when (currentTab) {
            "liked" -> viewModel.likedPosts.value
            "collected" -> viewModel.savedPosts.value
            else -> viewModel.userPosts.value
        }
        gridAdapter.submitList(posts ?: emptyList())
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
