package com.wenshu.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.wenshu.app.data.repository.ChatRepository
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.data.repository.UserRepository
import com.wenshu.app.databinding.ActivityMainBinding
import com.wenshu.app.ui.addpost.AddPostActivity
import com.wenshu.app.ui.chat.ChatActivity
import com.wenshu.app.ui.discover.DiscoverFragment
import com.wenshu.app.ui.home.HomeFragment
import com.wenshu.app.ui.notifications.NotificationsFragment
import com.wenshu.app.ui.profile.ProfileFragment
import com.wenshu.app.util.ImageUtils
import com.wenshu.app.util.PreferenceManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: PreferenceManager
    private val chatRepo = ChatRepository.getInstance()
    private val postRepo = PostRepository.getInstance()
    private val userRepo = UserRepository.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var unreadBadge: BadgeDrawable? = null
    private var lastNotifCount = 0

    private val pollRunnable = object : Runnable {
        override fun run() {
            pollUnread()
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = PreferenceManager(this)

        if (!pref.isLoggedIn()) {
            startActivity(Intent(this, com.wenshu.app.ui.auth.LoginActivity::class.java))
            finish()
            return
        }

        val userId = pref.getUserId() ?: ""
        chatRepo.setCurrentUserId(userId)
        lifecycleScope.launch {
            postRepo.refreshCurrentUser()
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        setupBottomNav()
        setupFab()
        setupNotificationPopup()

        handler.postDelayed(pollRunnable, 1000)
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_discover -> DiscoverFragment()
                R.id.nav_add -> return@setOnItemSelectedListener false
                R.id.nav_notifications -> NotificationsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
        binding.bottomNav.menu.findItem(R.id.nav_add).isEnabled = false
    }

    private fun setupFab() {
        binding.fabAddPost.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }
    }

    private fun setupNotificationPopup() {
        binding.notificationPopup.setOnClickListener {
            hideNotificationPopup()
            binding.bottomNav.selectedItemId = R.id.nav_notifications
        }
    }

    fun showNotificationPopup(text: String) {
        runOnUiThread {
            binding.notifText.text = text
            binding.notificationPopup.visibility = View.VISIBLE
            val displayMetrics = resources.displayMetrics
            val statusBarHeight = getStatusBarHeight()
            binding.notificationPopup.setPadding(
                binding.notificationPopup.paddingLeft,
                statusBarHeight + resources.getDimensionPixelSize(R.dimen.spacing_sm),
                binding.notificationPopup.paddingRight,
                resources.getDimensionPixelSize(R.dimen.spacing_sm)
            )
            val slideDown = ObjectAnimator.ofFloat(binding.notificationPopup, "translationY",
                -binding.notificationPopup.height.toFloat() - statusBarHeight, 0f)
            slideDown.duration = 400
            slideDown.interpolator = AccelerateDecelerateInterpolator()
            slideDown.start()

            val blinkAnim = ObjectAnimator.ofFloat(binding.notifDot, "alpha", 1f, 0.2f, 1f, 0.2f, 1f, 0.2f, 1f)
            blinkAnim.duration = 1200
            blinkAnim.start()

            handler.removeCallbacks(hidePopupRunnable)
            handler.postDelayed(hidePopupRunnable, 4500)
        }
    }

    private val hidePopupRunnable = Runnable { hideNotificationPopup() }

    private fun hideNotificationPopup() {
        if (binding.notificationPopup.visibility != View.VISIBLE) return
        val slideUp = ObjectAnimator.ofFloat(binding.notificationPopup, "translationY",
            0f, -binding.notificationPopup.height.toFloat() - getStatusBarHeight())
        slideUp.duration = 350
        slideUp.interpolator = AccelerateDecelerateInterpolator()
        slideUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.notificationPopup.visibility = View.GONE
                binding.notificationPopup.translationY = 0f
            }
        })
        slideUp.start()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }

    private fun pollUnread() {
        lifecycleScope.launch {
            try {
                val result = chatRepo.getUnreadConversations()
                result.onSuccess { unread ->
                    runOnUiThread {
                        if (unread > 0) {
                            showBadge(unread)
                            if (unread > lastNotifCount && lastNotifCount >= 0) {
                                showNotificationPopup("你有 $unread 条未读消息")
                            }
                        } else {
                            hideBadge()
                        }
                        lastNotifCount = unread
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun showBadge(count: Int) {
        val menuItemView = binding.bottomNav.findViewById<View>(R.id.nav_notifications)
        if (unreadBadge == null) {
            unreadBadge = BadgeDrawable.create(this)
            unreadBadge!!.backgroundColor = getColor(R.color.liked)
            unreadBadge!!.badgeTextColor = getColor(R.color.on_primary)
        }
        unreadBadge!!.number = count
        unreadBadge!!.isVisible = true
        BadgeUtils.attachBadgeDrawable(unreadBadge!!, binding.bottomNav, R.id.nav_notifications)
    }

    private fun hideBadge() {
        unreadBadge?.isVisible = false
        lastNotifCount = 0
    }

    fun clearUnreadBadge() {
        hideBadge()
    }

    fun loadAvatarWithGlide(view: ImageView, avatarUrl: String?, username: String) {
        val url = if (!avatarUrl.isNullOrBlank()) avatarUrl else ImageUtils.getAvatarUrl(username)
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar)
            .circleCrop()
            .into(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(pollRunnable)
        handler.removeCallbacks(hidePopupRunnable)
    }

    override fun onResume() {
        super.onResume()
        pollUnread()
    }
}
