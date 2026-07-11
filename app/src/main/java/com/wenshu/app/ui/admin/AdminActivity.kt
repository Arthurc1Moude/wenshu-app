package com.wenshu.app.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.AdminBanRequest
import com.wenshu.app.data.model.AdminRewardRequest
import com.wenshu.app.data.model.User
import com.wenshu.app.databinding.ActivityAdminBinding
import com.wenshu.app.databinding.ItemAdminUserBinding
import com.wenshu.app.util.ImageUtils
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var adapter: AdminUsersAdapter
    private var allUsers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = AdminUsersAdapter(
            onBan = { user -> showBanDialog(user) },
            onUnban = { user -> performUnban(user) },
            onReward = { user -> showRewardDialog(user) }
        )
        binding.recyclerUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerUsers.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { filterUsers(s?.toString() ?: "") }
        })

        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val result = safeApiCall { RetrofitClient.apiService.adminListUsers() }
            result.onSuccess { users ->
                allUsers = users.filter { !it.isAdmin }
                filterUsers(binding.etSearch.text?.toString() ?: "")
                binding.tvEmpty.visibility = if (allUsers.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerUsers.visibility = if (allUsers.isEmpty()) View.GONE else View.VISIBLE
            }.onFailure { e ->
                Toast.makeText(this@AdminActivity, "加载失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun filterUsers(query: String) {
        val filtered = if (query.isBlank()) allUsers else allUsers.filter {
            it.username.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun showBanDialog(user: User) {
        val options = arrayOf("警告（不封禁）", "封禁1天", "封禁7天", "封禁30天", "永久封禁")
        AlertDialog.Builder(this)
            .setTitle("封禁 ${user.username}")
            .setItems(options) { _, which ->
                val durationMs = when (which) {
                    1 -> 86400000L
                    2 -> 7 * 86400000L
                    3 -> 30 * 86400000L
                    else -> null
                }
                val reasonInput = EditText(this).apply {
                    hint = "封禁原因（可选）"
                    setPadding(48, 32, 48, 32)
                }
                if (which == 0) {
                    Toast.makeText(this, "未执行封禁", Toast.LENGTH_SHORT).show()
                    return@setItems
                }
                AlertDialog.Builder(this)
                    .setTitle("封禁原因")
                    .setView(reasonInput)
                    .setPositiveButton("确认封禁") { _, _ ->
                        performBan(user, durationMs, reasonInput.text?.toString()?.ifBlank { "违反社区规则" })
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performBan(user: User, durationMs: Long?, reason: String?) {
        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.adminBanUser(user.id, AdminBanRequest(durationMs, reason))
            }
            result.onSuccess {
                Toast.makeText(this@AdminActivity, "已封禁 ${user.username}", Toast.LENGTH_SHORT).show()
                loadUsers()
            }.onFailure { e ->
                Toast.makeText(this@AdminActivity, "操作失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performUnban(user: User) {
        lifecycleScope.launch {
            val result = safeApiCall { RetrofitClient.apiService.adminUnbanUser(user.id) }
            result.onSuccess {
                Toast.makeText(this@AdminActivity, "已解封 ${user.username}", Toast.LENGTH_SHORT).show()
                loadUsers()
            }.onFailure { e ->
                Toast.makeText(this@AdminActivity, "操作失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showRewardDialog(user: User) {
        val options = arrayOf("奖励100文书币", "奖励500文书币", "奖励1000文书币", "奖励7天VIP", "奖励30天VIP")
        AlertDialog.Builder(this)
            .setTitle("发奖励给 ${user.username}")
            .setItems(options) { _, which ->
                var coins = 0
                var vipDays = 0
                when (which) {
                    0 -> coins = 100
                    1 -> coins = 500
                    2 -> coins = 1000
                    3 -> vipDays = 7
                    4 -> vipDays = 30
                }
                performReward(user, coins, vipDays)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun performReward(user: User, coins: Int, vipDays: Int) {
        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.adminRewardUser(user.id, AdminRewardRequest(coins, vipDays, "管理员发放"))
            }
            result.onSuccess {
                Toast.makeText(this@AdminActivity, "奖励已发放", Toast.LENGTH_SHORT).show()
                loadUsers()
            }.onFailure { e ->
                Toast.makeText(this@AdminActivity, "发放失败: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class AdminUsersAdapter(
        private val onBan: (User) -> Unit,
        private val onUnban: (User) -> Unit,
        private val onReward: (User) -> Unit
    ) : RecyclerView.Adapter<AdminUsersAdapter.VH>() {
        private var items: List<User> = emptyList()
        fun submitList(list: List<User>) { items = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount() = items.size

        inner class VH(private val b: ItemAdminUserBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(user: User) {
                b.tvUsername.text = user.username
                val info = buildString {
                    append("文书币: ${user.wenshuCoin}")
                    if (user.isVip) append(" | VIP")
                    append(" | 粉丝: ${user.followersCount}")
                }
                b.tvInfo.text = info
                b.tvBadge.visibility = if (user.isAdmin) View.VISIBLE else View.GONE
                b.tvBanned.visibility = if (user.isBanned) View.VISIBLE else View.GONE
                b.btnBan.visibility = if (user.isBanned) View.GONE else View.VISIBLE
                b.btnUnban.visibility = if (user.isBanned) View.VISIBLE else View.GONE

                Glide.with(b.imgAvatar.context)
                    .load(ImageUtils.normalizeUrl(user.avatar))
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .circleCrop()
                    .into(b.imgAvatar)

                b.btnBan.setOnClickListener { onBan(user) }
                b.btnUnban.setOnClickListener { onUnban(user) }
                b.btnReward.setOnClickListener { onReward(user) }
            }
        }
    }
}
