package com.wenshu.app.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.User
import com.wenshu.app.databinding.ActivityFriendsBinding
import com.wenshu.app.databinding.ItemFriendBinding
import com.wenshu.app.util.ImageUtils
import kotlinx.coroutines.launch

class FriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsBinding
    private lateinit var adapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        adapter = FriendsAdapter { user -> startChatWithFriend(user) }
        binding.recyclerFriends.layoutManager = LinearLayoutManager(this)
        binding.recyclerFriends.adapter = adapter

        val swipeRefresh = SwipeRefreshLayout(this).apply {
            setColorSchemeColors(getColor(R.color.seal))
            setOnRefreshListener { loadFriends { isRefreshing = false } }
        }
        val parent = binding.recyclerFriends.parent as? ViewGroup
        if (parent != null) {
            val idx = parent.indexOfChild(binding.recyclerFriends)
            parent.removeView(binding.recyclerFriends)
            swipeRefresh.addView(binding.recyclerFriends)
            parent.addView(swipeRefresh, idx)
        }

        loadFriends()
    }

    private fun loadFriends(onComplete: (() -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                val result = safeApiCall { RetrofitClient.apiService.getFriends() }
                result.onSuccess { list ->
                    if (list.isEmpty()) {
                        binding.tvEmpty.text = "暂无互关好友\n互相关注后即可聊天"
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.recyclerFriends.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.recyclerFriends.visibility = View.VISIBLE
                        adapter.submitList(list)
                    }
                }.onFailure { e ->
                    Toast.makeText(this@FriendsActivity, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "网络错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            onComplete?.invoke()
        }
    }

    private fun startChatWithFriend(user: User) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("otherUserId", user.id)
            putExtra("conversationTitle", user.username)
            putExtra("conversationType", "private")
        }
        startActivity(intent)
    }

    inner class FriendsAdapter(private val onClick: (User) -> Unit) : RecyclerView.Adapter<FriendsAdapter.VH>() {
        private var items: List<User> = emptyList()
        fun submitList(list: List<User>) { items = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount() = items.size

        inner class VH(private val b: ItemFriendBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(user: User) {
                b.tvUsername.text = user.username
                b.tvBio.text = user.bio ?: "互关好友"
                b.tvVipBadge.visibility = if (user.isVip) View.VISIBLE else View.GONE
                val url = ImageUtils.normalizeUrl(user.avatar)
                Glide.with(this@FriendsActivity).load(url).circleCrop()
                    .placeholder(R.drawable.bg_avatar_placeholder).error(R.drawable.bg_avatar_placeholder).into(b.imgAvatar)
                b.root.setOnClickListener { onClick(user) }
            }
        }
    }
}
