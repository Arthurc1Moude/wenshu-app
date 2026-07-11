package com.wenshu.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivitySettingsBinding
import com.wenshu.app.ui.admin.AdminActivity
import com.wenshu.app.ui.auth.LoginActivity
import com.wenshu.app.ui.profile.EditProfileActivity
import com.wenshu.app.util.TimeUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val repository = PostRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updateUserInfo()
    }

    private fun updateUserInfo() {
        val user = SharedPreferencesManager.getUser()
        if (user != null && user.isVip) {
            val expiresText = user.vipExpiresAt?.let {
                "会员至 ${TimeUtils.formatDate(it)}"
            } ?: "会员已开通"
            binding.tvVipStatus.text = expiresText
        } else {
            binding.tvVipStatus.text = "立即开通"
        }

        if (user != null && user.isAdmin) {
            binding.itemAdmin.visibility = View.VISIBLE
            binding.dividerAdmin.visibility = View.VISIBLE
        } else {
            binding.itemAdmin.visibility = View.GONE
            binding.dividerAdmin.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.itemVip.setOnClickListener {
            startActivity(Intent(this, VipActivity::class.java))
        }

        binding.itemRedeem.setOnClickListener {
            startActivity(Intent(this, RedeemActivity::class.java))
        }

        binding.itemNotificationSettings.setOnClickListener {
            showNotificationSettings()
        }

        binding.itemPrivacy.setOnClickListener {
            showPrivacySettings()
        }

        binding.itemAccountSecurity.setOnClickListener {
            startActivity(Intent(this, AccountSecurityActivity::class.java))
        }

        binding.itemShareApp.setOnClickListener {
            shareApp()
        }

        binding.itemAdmin.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        binding.itemAbout.setOnClickListener {
            showAbout()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirm()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "文书 - 文化生活社区\n来记录生活，分享思考。\nhttps://wenshucom.vercel.app")
        }
        startActivity(Intent.createChooser(shareIntent, "分享文书"))
    }

    private fun showNotificationSettings() {
        val options = arrayOf("全部通知", "仅关注的人", "关闭")
        AlertDialog.Builder(this)
            .setTitle("消息通知")
            .setItems(options) { _, which ->
                val msg = when (which) {
                    0 -> "已开启全部通知"
                    1 -> "仅接收关注的人通知"
                    else -> "已关闭通知"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showPrivacySettings() {
        val options = arrayOf("黑名单管理", "清除缓存")
        AlertDialog.Builder(this)
            .setTitle("隐私与数据")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showBlacklist()
                    1 -> clearCache()
                }
            }
            .show()
    }

    private fun showBlacklist() {
        AlertDialog.Builder(this)
            .setTitle("黑名单")
            .setMessage("暂无拉黑用户")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun clearCache() {
        AlertDialog.Builder(this)
            .setTitle("清除缓存")
            .setMessage("确定要清除本地缓存吗？这不会删除您的账号数据。")
            .setPositiveButton("清除") { _, _ ->
                try {
                    cacheDir.deleteRecursively()
                    Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "清除失败", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showAbout() {
        val message = buildString {
            append("文书 - 文化生活社区\n")
            append("版本: 1.0.0\n\n")
            append("一款以文学、文化为主题的社交应用。\n")
            append("记录生活，分享思考。\n\n")
            append("官网: https://wenshucom.vercel.app")
        }
        AlertDialog.Builder(this)
            .setTitle("关于文书")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .setNeutralButton("访问官网") { _, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wenshucom.vercel.app"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "无法打开浏览器", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }

    private fun showLogoutConfirm() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("退出") { _, _ ->
                performLogout()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performLogout() {
        SharedPreferencesManager.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
