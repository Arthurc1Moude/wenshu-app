package com.wenshu.app.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.model.CreateGroupRequest
import com.wenshu.app.data.model.User
import com.wenshu.app.databinding.ActivityCreateGroupBinding
import com.wenshu.app.databinding.ItemFriendSelectBinding
import com.wenshu.app.util.ImageUtils
import kotlinx.coroutines.launch

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding
    private var friends: List<User> = emptyList()
    private val selectedIds = mutableSetOf<String>()
    private lateinit var adapter: FriendSelectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnCreate.setOnClickListener { createGroup() }

        adapter = FriendSelectAdapter()
        binding.recyclerFriends.layoutManager = LinearLayoutManager(this)
        binding.recyclerFriends.adapter = adapter

        loadFriends()
    }

    private fun loadFriends() {
        lifecycleScope.launch {
            try {
                val list = RetrofitClient.apiService.getFriends()
                friends = list
                if (list.isEmpty()) {
                    binding.tvNoFriends.visibility = View.VISIBLE
                    binding.recyclerFriends.visibility = View.GONE
                } else {
                    binding.tvNoFriends.visibility = View.GONE
                    binding.recyclerFriends.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateGroupActivity, "加载好友失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createGroup() {
        val name = binding.etGroupName.text.toString().trim()
        if (name.isBlank()) {
            Toast.makeText(this, "请输入群聊名称", Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnCreate.isEnabled = false
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.createGroup(
                    CreateGroupRequest(name, selectedIds.toList())
                )
                Toast.makeText(this@CreateGroupActivity, "群聊「${resp.name}」已创建！群号：${resp.groupNumber}", Toast.LENGTH_LONG).show()
                val intent = Intent(this@CreateGroupActivity, ChatActivity::class.java).apply {
                    putExtra("conversationId", resp.id)
                    putExtra("conversationTitle", resp.name)
                    putExtra("conversationType", "group")
                }
                startActivity(intent)
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                binding.btnCreate.isEnabled = true
                Toast.makeText(this@CreateGroupActivity, "创建失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class FriendSelectAdapter : RecyclerView.Adapter<FriendSelectAdapter.VH>() {
        private var items: List<User> = emptyList()
        fun submitList(list: List<User>) { items = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemFriendSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val user = items[position]
            holder.bind(user)
        }

        override fun getItemCount() = items.size

        inner class VH(private val b: ItemFriendSelectBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(user: User) {
                b.tvUsername.text = user.username
                b.tvBio.text = user.bio ?: "互关好友"
                val url = if (!user.avatar.isNullOrBlank()) user.avatar else ImageUtils.getAvatarUrl(user.username)
                Glide.with(this@CreateGroupActivity).load(url).circleCrop()
                    .placeholder(R.drawable.bg_avatar_placeholder).error(R.drawable.bg_avatar_placeholder).into(b.imgAvatar)
                b.cbSelect.setOnCheckedChangeListener(null)
                b.cbSelect.isChecked = selectedIds.contains(user.id)
                val toggle: (View) -> Unit = {
                    if (selectedIds.contains(user.id)) selectedIds.remove(user.id) else selectedIds.add(user.id)
                    b.cbSelect.isChecked = selectedIds.contains(user.id)
                }
                b.root.setOnClickListener(toggle)
                b.cbSelect.setOnClickListener(toggle)
            }
        }
    }
}
