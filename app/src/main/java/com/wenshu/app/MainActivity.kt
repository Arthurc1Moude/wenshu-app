package com.wenshu.app

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.badge.BadgeDrawable
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivityMainBinding
import com.wenshu.app.ui.auth.LoginActivity
import com.wenshu.app.ui.home.HomeFragment
import com.wenshu.app.ui.search.SearchActivity
import com.wenshu.app.ui.notifications.NotificationsFragment
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.ui.profile.ProfileFragment
import com.wenshu.app.ui.publish.PublishFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        var appContext: android.content.Context? = null
            private set
    }

    private lateinit var binding: ActivityMainBinding

    private var homeFragment: HomeFragment? = null
    private var publishFragment: PublishFragment? = null
    private var notificationsFragment: NotificationsFragment? = null
    private var profileFragment: ProfileFragment? = null

    private var currentFragment: Fragment? = null
    private var unreadBadge: BadgeDrawable? = null
    private val repository = PostRepository.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private var notifAutoHide: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        Log.d("MainActivity", "onCreate started")

        if (!SharedPreferencesManager.isLoggedIn()) {
            Log.d("MainActivity", "User not logged in, navigating to login")
            navigateToLogin()
            return
        }

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("MainActivity", "Content view set successfully")

            restoreFragments(savedInstanceState)
            setupBottomNavigation()
            setupNotificationPopup()

            if (savedInstanceState == null) {
                binding.bottomNav.selectedItemId = R.id.nav_home
                Log.d("MainActivity", "Initial navigation to HomeFragment")
            } else {
                val tag = savedInstanceState.getString("current_fragment_tag", "home")
                val navId = tagToNavId(tag)
                binding.bottomNav.selectedItemId = navId
            }

            observeUnreadCount()

            lifecycleScope.launch {
                try {
                    repository.loadNotifications()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to load notifications", e)
                }
            }

            Log.d("MainActivity", "onCreate completed successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "FATAL ERROR in onCreate", e)
            throw e
        }
    }

    private fun setupNotificationPopup() {
        binding.notificationPopup.setOnClickListener {
            selectTab(R.id.nav_notifications)
            hideNotificationPopup()
        }
        (binding.notificationPopup.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            params.topMargin = getStatusBarHeight()
            binding.notificationPopup.layoutParams = params
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }

    fun showNotificationPopup(text: String) {
        runOnUiThread {
            binding.notifText.text = text
            binding.notificationPopup.visibility = View.VISIBLE
            val targetHeight = binding.notificationPopup.height
            val slideDown = ObjectAnimator.ofFloat(binding.notificationPopup, "translationY",
                -(targetHeight + getStatusBarHeight()).toFloat(), 0f)
            slideDown.duration = 400
            slideDown.interpolator = AccelerateDecelerateInterpolator()
            slideDown.start()

            val dot = binding.notifDot
            fun flash(i: Int) {
                if (i < 4) {
                    dot.animate().alpha(if (dot.alpha == 1f) 0.2f else 1f).setDuration(250)
                        .withEndAction { flash(i + 1) }.start()
                }
            }
            dot.alpha = 1f
            flash(0)

            notifAutoHide?.let { handler.removeCallbacks(it) }
            val hide = Runnable { hideNotificationPopup() }
            notifAutoHide = hide
            handler.postDelayed(hide, 4000)
        }
    }

    private fun hideNotificationPopup() {
        if (binding.notificationPopup.visibility != View.VISIBLE) return
        val targetHeight = binding.notificationPopup.height
        val slideUp = ObjectAnimator.ofFloat(binding.notificationPopup, "translationY",
            0f, -(targetHeight + getStatusBarHeight()).toFloat())
        slideUp.duration = 300
        slideUp.interpolator = AccelerateDecelerateInterpolator()
        slideUp.start()
        handler.postDelayed({ binding.notificationPopup.visibility = View.GONE }, 320)
    }

    fun showBadge(count: Int) {
        if (count > 0) {
            unreadBadge?.number = count
            unreadBadge?.isVisible = true
        } else {
            unreadBadge?.isVisible = false
        }
    }

    fun hideBadge() {
        unreadBadge?.isVisible = false
    }

    fun clearUnreadBadge() {
        hideBadge()
    }

    private fun restoreFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            homeFragment = supportFragmentManager.findFragmentByTag("home") as? HomeFragment
            publishFragment = supportFragmentManager.findFragmentByTag("publish") as? PublishFragment
            notificationsFragment = supportFragmentManager.findFragmentByTag("notifications") as? NotificationsFragment
            profileFragment = supportFragmentManager.findFragmentByTag("profile") as? ProfileFragment
            var currentTag = savedInstanceState.getString("current_fragment_tag", "home")
            if (currentTag == "discover") currentTag = "home"
            currentFragment = supportFragmentManager.findFragmentByTag(currentTag)
            if (currentFragment == null) currentFragment = homeFragment
            Log.d("MainActivity", "Restored fragments from saved state, current: $currentTag")
        }
    }

    private fun tagToNavId(tag: String): Int {
        return when (tag) {
            "home" -> R.id.nav_home
            "publish" -> R.id.nav_publish
            "notifications" -> R.id.nav_notifications
            "profile" -> R.id.nav_profile
            else -> R.id.nav_home
        }
    }

    private fun navIdToTag(navId: Int): String {
        return when (navId) {
            R.id.nav_home -> "home"
            R.id.nav_publish -> "publish"
            R.id.nav_notifications -> "notifications"
            R.id.nav_profile -> "profile"
            else -> "home"
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val tag = when (currentFragment) {
            is HomeFragment -> "home"
            is PublishFragment -> "publish"
            is NotificationsFragment -> "notifications"
            is ProfileFragment -> "profile"
            else -> "home"
        }
        outState.putString("current_fragment_tag", tag)
    }

    private fun setupBottomNavigation() {
        Log.d("MainActivity", "Setting up bottom navigation")
        binding.bottomNav.setOnItemSelectedListener { item ->
            Log.d("MainActivity", "Bottom nav item selected: ${item.itemId}")

            if (item.itemId == R.id.nav_discover) {
                val homeFrag = if (homeFragment == null) {
                    homeFragment = HomeFragment()
                    homeFragment!!
                } else {
                    homeFragment!!
                }
                switchFragment(homeFrag, "home")
                binding.bottomNav.menu.findItem(R.id.nav_home).isChecked = true
                binding.root.postDelayed({
                    runOnUiThread {
                        homeFragment?.openSearch()
                    }
                }, 100)
                return@setOnItemSelectedListener false
            }

            val fragment = when (item.itemId) {
                R.id.nav_home -> {
                    if (homeFragment == null) homeFragment = HomeFragment()
                    homeFragment!!
                }
                R.id.nav_publish -> {
                    if (publishFragment == null) publishFragment = PublishFragment()
                    publishFragment!!
                }
                R.id.nav_notifications -> {
                    if (notificationsFragment == null) notificationsFragment = NotificationsFragment()
                    notificationsFragment!!
                }
                R.id.nav_profile -> {
                    if (profileFragment == null) profileFragment = ProfileFragment()
                    profileFragment!!
                }
                else -> {
                    Log.e("MainActivity", "Unknown nav item: ${item.itemId}")
                    return@setOnItemSelectedListener false
                }
            }
            switchFragment(fragment, navIdToTag(item.itemId))
            true
        }

        binding.bottomNav.setOnItemReselectedListener {
            Log.d("MainActivity", "Bottom nav item reselected: ${it.itemId}")
        }

        unreadBadge = binding.bottomNav.getOrCreateBadge(R.id.nav_notifications)
        unreadBadge?.backgroundColor = ContextCompat.getColor(this, R.color.seal)
        unreadBadge?.badgeTextColor = ContextCompat.getColor(this, android.R.color.white)
        unreadBadge?.isVisible = false
    }

    private fun observeUnreadCount() {
        repository.unreadCount.observe(this) { count ->
            Log.d("MainActivity", "Unread count updated: $count")
            if (count != null && count > 0) {
                unreadBadge?.number = count
                unreadBadge?.isVisible = true
            } else {
                unreadBadge?.isVisible = false
            }
        }
    }

    private fun switchFragment(fragment: Fragment, tag: String) {
        try {
            Log.d("MainActivity", "switchFragment: $tag")

            val transaction = supportFragmentManager.beginTransaction()

            currentFragment?.let {
                if (it !== fragment) {
                    transaction.hide(it)
                    Log.d("MainActivity", "Hiding current fragment: ${it.javaClass.simpleName}")
                }
            }

            val existingFragment = supportFragmentManager.findFragmentByTag(tag)
            if (existingFragment != null) {
                if (existingFragment !== fragment) {
                    Log.w("MainActivity", "Fragment exists but is different instance! Using existing.")
                    transaction.show(existingFragment)
                    currentFragment = existingFragment
                    updateFragmentReference(tag, existingFragment)
                } else {
                    transaction.show(fragment)
                    currentFragment = fragment
                }
                Log.d("MainActivity", "Showing existing fragment")
            } else {
                transaction.add(R.id.fragment_container, fragment, tag)
                currentFragment = fragment
                Log.d("MainActivity", "Adding new fragment: ${fragment.javaClass.simpleName}")
            }

            transaction.commitAllowingStateLoss()
            Log.d("MainActivity", "Fragment transaction committed for $tag")

            if (tag == "home") {
                (currentFragment as? HomeFragment)?.refreshPosts()
            } else if (tag == "notifications") {
                hideNotificationPopup()
                lifecycleScope.launch {
                    try {
                        repository.loadNotifications()
                        repository.markNotificationsRead()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to refresh notifications", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error switching fragment", e)
        }
    }

    private fun updateFragmentReference(tag: String, fragment: Fragment) {
        when (tag) {
            "home" -> homeFragment = fragment as? HomeFragment
            "publish" -> publishFragment = fragment as? PublishFragment
            "notifications" -> notificationsFragment = fragment as? NotificationsFragment
            "profile" -> profileFragment = fragment as? ProfileFragment
        }
    }

    fun navigateToPostDetail(postId: String) {
        val intent = Intent(this, PostDetailActivity::class.java)
        intent.putExtra("post_id", postId)
        startActivity(intent)
    }

    fun selectTab(tabId: Int) {
        binding.bottomNav.selectedItemId = tabId
    }

    fun onPostPublished() {
        binding.bottomNav.selectedItemId = R.id.nav_home
    }

    fun logout() {
        SharedPreferencesManager.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        notifAutoHide?.let { handler.removeCallbacks(it) }
    }
}
