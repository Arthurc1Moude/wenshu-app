package com.wenshu.app.ui.profile

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.model.User
import com.wenshu.app.databinding.ActivityUserProfileBinding
import com.wenshu.app.ui.adapters.PostGridAdapter
import com.wenshu.app.ui.chat.ChatActivity
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.util.ImageUtils

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var gridAdapter: PostGridAdapter
    private var currentTab = "posts"
    private var userId: String? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("user_id") ?: intent.getStringExtra("userId")
        if (userId == null) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupToolbar()
        setupTabs()
        setupGrid()
        setupButtons()
        observeData()

        viewModel.loadUserProfile(userId!!)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnShareProfile.setOnClickListener { shareProfile() }
    }

    private fun setupButtons() {
        binding.btnFollow.setOnClickListener {
            handleFollowClick()
        }
        binding.btnMessage.setOnClickListener {
            val user = currentUser ?: return@setOnClickListener
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("otherUserId", user.id)
            intent.putExtra("conversationTitle", user.username)
            intent.putExtra("conversationType", "private")
            startActivity(intent)
        }
    }

    private fun handleFollowClick() {
        val user = currentUser ?: return
        if (user.isFollowing) {
            AlertDialog.Builder(this)
                .setTitle("取消关注")
                .setMessage("确定不再关注 ${user.displayName} 吗？")
                .setPositiveButton("确定") { _, _ ->
                    viewModel.toggleFollow()
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            viewModel.toggleFollow()
        }
    }

    private fun shareProfile() {
        val user = currentUser ?: return
        val shareUrl = "https://wenshucom.vercel.app/${user.username}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "来看看 ${user.displayName} 在文书上的分享：$shareUrl")
        }
        startActivity(Intent.createChooser(shareIntent, "分享个人主页"))
    }

    private fun setupTabs() {
        val tabs = listOf(
            "posts" to "作品"
        )
        tabs.forEach { (key, label) ->
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_xl) * 2
                }
            }

            val tv = TextView(this).apply {
                text = label
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@UserProfileActivity, R.color.text_primary))
                tag = "tab_$key"
                typeface = Typeface.SERIF
            }
            container.addView(tv)

            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(20.dpToPx(), 2.dpToPx()).apply {
                    topMargin = 4.dpToPx()
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setBackgroundColor(ContextCompat.getColor(this@UserProfileActivity, R.color.primary))
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
        val black = ContextCompat.getColor(this, R.color.text_primary)
        val gray = ContextCompat.getColor(this, R.color.text_tertiary)
        val tabs = listOf("posts")
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
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }
        binding.recyclerPosts.apply {
            layoutManager = GridLayoutManager(this@UserProfileActivity, 2)
            adapter = gridAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                currentUser = user
                bindUser(user)
            }
        }
        viewModel.userPosts.observe(this) {
            if (currentTab == "posts") updateGrid()
        }
        viewModel.followResult.observe(this) { result ->
            result?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearFollowResult()
                viewModel.loadUserProfile(userId!!)
            }
        }
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun bindUser(user: User) {
        val myId = SharedPreferencesManager.getUser()?.id
        val isMe = myId == user.id

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

        binding.tvVipBadge.visibility = if (user.isVip) View.VISIBLE else View.GONE
        binding.tvBio.visibility = if (user.bio.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvMutualBadge.visibility = if (user.isMutual) View.VISIBLE else View.GONE

        if (!user.location.isNullOrBlank()) {
            binding.layoutLocationProfile.visibility = View.VISIBLE
            binding.tvLocation.text = user.location
        } else {
            binding.layoutLocationProfile.visibility = View.GONE
        }

        if (isMe) {
            binding.btnFollow.visibility = View.GONE
            binding.btnMessage.visibility = View.GONE
        } else {
            updateFollowButton(user)
            binding.btnMessage.visibility = if (user.isMutual) View.VISIBLE else View.GONE
        }
    }

    private fun updateFollowButton(user: User) {
        binding.btnFollow.visibility = View.VISIBLE
        val ctx = this
        when {
            user.isMutual -> {
                binding.btnFollow.text = "互相关注"
                binding.btnFollow.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                binding.btnFollow.strokeColor = ContextCompat.getColorStateList(ctx, R.color.outline)
                binding.btnFollow.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.surface)
            }
            user.isFollowing -> {
                binding.btnFollow.text = "已关注"
                binding.btnFollow.setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                binding.btnFollow.strokeColor = ContextCompat.getColorStateList(ctx, R.color.outline)
                binding.btnFollow.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.surface)
            }
            else -> {
                binding.btnFollow.text = "关注"
                binding.btnFollow.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
                binding.btnFollow.strokeColor = ContextCompat.getColorStateList(ctx, R.color.ink)
                binding.btnFollow.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.ink)
            }
        }
    }

    private fun updateGrid() {
        gridAdapter.submitList(viewModel.userPosts.value ?: emptyList())
    }

    override fun onResume() {
        super.onResume()
        userId?.let { viewModel.loadUserProfile(it) }
    }
}
