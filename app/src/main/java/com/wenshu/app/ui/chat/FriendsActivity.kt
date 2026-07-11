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
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.model.User
import com.wenshu.app.databinding.ActivityFriendsBinding
import com.wenshu.app.databinding.ItemFriendBinding
import com.wenshu.app.util.ImageUtils
import kotlinx.coroutines.launch
import java.util.UUID

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

        loadFriends()
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                val list = RetrofitClient.apiService.getFriends()
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.recyclerFriends.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.recyclerFriends.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            } catch (e: Exception) {
                Toast.makeText(this@FriendsActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startChatWithFriend(user: User) {
        val convId = "dm_${minOf(user.id, getCurrentUserId())}_${maxOf(user.id, getCurrentUserId())}"
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("conversationId", convId)
            putExtra("conversationTitle", user.username)
            putExtra("conversationType", "dm")
            putExtra("otherUserId", user.id)
            putExtra("otherUserAvatar", user.avatar)
        }
        startActivity(intent)
    }

    private fun getCurrentUserId(): String {
        return getSharedPreferences("wenshu", MODE_PRIVATE).getString("user_id", "") ?: ""
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
                val url = if (!user.avatar.isNullOrBlank()) user.avatar else ImageUtils.getAvatarUrl(user.username)
                Glide.with(this@FriendsActivity).load(url).circleCrop()
                    .placeholder(R.drawable.bg_avatar_placeholder).error(R.drawable.bg_avatar_placeholder).into(b.imgAvatar)
                b.root.setOnClickListener { onClick(user) }
            }
        }
    }
}
