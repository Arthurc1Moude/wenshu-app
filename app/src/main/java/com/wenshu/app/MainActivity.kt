package com.wenshu.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.wenshu.app.databinding.ActivityMainBinding
import com.wenshu.app.ui.discover.DiscoverFragment
import com.wenshu.app.ui.home.HomeFragment
import com.wenshu.app.ui.notifications.NotificationsFragment
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.ui.profile.ProfileFragment
import com.wenshu.app.ui.publish.PublishFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null

    private val homeFragment by lazy { HomeFragment() }
    private val discoverFragment by lazy { DiscoverFragment() }
    private val publishFragment by lazy { PublishFragment() }
    private val notificationsFragment by lazy { NotificationsFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    private var isPublishing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            switchFragment(homeFragment)
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (isPublishing) {
                isPublishing = false
                return@setOnItemSelectedListener true
            }
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment)
                    true
                }
                R.id.nav_discover -> {
                    switchFragment(discoverFragment)
                    true
                }
                R.id.nav_publish -> {
                    isPublishing = true
                    switchFragment(publishFragment)
                    true
                }
                R.id.nav_notifications -> {
                    switchFragment(notificationsFragment)
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(profileFragment)
                    true
                }
                else -> false
            }
        }

        binding.bottomNav.setOnItemReselectedListener { }
    }

    private fun switchFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        currentFragment?.let { transaction.hide(it) }

        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.fragment_container, fragment)
        }

        currentFragment = fragment
        transaction.commitAllowingStateLoss()
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
        switchFragment(homeFragment)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
