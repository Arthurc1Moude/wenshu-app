package com.wenshu.app.ui.secret

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.CreateSecretPostRequest
import com.wenshu.app.data.model.SecretPost
import com.wenshu.app.data.model.SecretVisibilityRequest
import com.wenshu.app.util.TimeUtils
import kotlinx.coroutines.launch

class SecretSpaceActivity : AppCompatActivity() {
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var tvEmpty: TextView
    private var menuVisible = false
    private val posts = mutableListOf<SecretPost>()
    private lateinit var adapter: SecretPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_space)

        swipeRefresh = findViewById(R.id.swipe_refresh)
        recycler = findViewById(R.id.recycler_posts)
        progress = findViewById(R.id.progress_loading)
        tvEmpty = findViewById(R.id.tv_empty)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_add).setOnClickListener {
            if (menuVisible) closeMenu() else openMenu()
        }
        findViewById<View>(R.id.menu_overlay).setOnClickListener { closeMenu() }
        findViewById<TextView>(R.id.menu_publish).setOnClickListener { closeMenu(); showPublishDialog() }
        findViewById<TextView>(R.id.menu_manage).setOnClickListener { closeMenu(); showManageDialog() }
        findViewById<TextView>(R.id.menu_visits).setOnClickListener { closeMenu(); showVisitsDialog() }
        findViewById<TextView>(R.id.menu_settings).setOnClickListener { closeMenu(); showSettingsDialog() }

        adapter = SecretPostAdapter(posts)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        swipeRefresh.setColorSchemeColors(getColor(R.color.seal))
        swipeRefresh.setOnRefreshListener { loadPosts() }
        loadPosts()
    }

    private fun openMenu() {
        menuVisible = true
        findViewById<View>(R.id.menu_overlay).visibility = View.VISIBLE
        findViewById<View>(R.id.popup_menu).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.btn_add).animate().rotation(45f).setDuration(300).start()
    }

    private fun closeMenu() {
        menuVisible = false
        findViewById<View>(R.id.menu_overlay).visibility = View.GONE
        findViewById<View>(R.id.popup_menu).visibility = View.GONE
        findViewById<ImageView>(R.id.btn_add).animate().rotation(0f).setDuration(300).start()
    }

    private fun loadPosts() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = safeApiCall { RetrofitClient.apiService.getSecretPosts(mine = true) }
            runOnUiThread {
                progress.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                result.onSuccess { list ->
                    posts.clear()
                    posts.addAll(list)
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
                }.onFailure {
                    Toast.makeText(this@SecretSpaceActivity, "加载失败: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPublishDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_publish_secret, null)
        val etContent = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_content)
        val rgVisibility = dialogView.findViewById<RadioGroup>(R.id.rg_visibility)

        AlertDialog.Builder(this)
            .setTitle("发布秘帖")
            .setView(dialogView)
            .setPositiveButton("发布") { _, _ ->
                val content = etContent.text.toString().trim()
                if (content.isEmpty()) {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val visibility = when (rgVisibility.checkedRadioButtonId) {
                    R.id.rb_private -> "private"
                    R.id.rb_mutual -> "mutual"
                    R.id.rb_specified -> "specified"
                    else -> "private"
                }
                publishPost(content, visibility)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun publishPost(content: String, visibility: String) {
        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.createSecretPost(CreateSecretPostRequest(content = content, visibility = visibility))
            }
            runOnUiThread {
                result.onSuccess {
                    Toast.makeText(this@SecretSpaceActivity, "发布成功", Toast.LENGTH_SHORT).show()
                    loadPosts()
                }.onFailure {
                    Toast.makeText(this@SecretSpaceActivity, "发布失败: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showManageDialog() {
        if (posts.isEmpty()) {
            Toast.makeText(this, "暂无秘帖可管理", Toast.LENGTH_SHORT).show()
            return
        }
        val titles = posts.map { p ->
            val vis = when (p.visibility) {
                "private" -> "[私密]"
                "mutual" -> "[互关]"
                "specified" -> "[指定]"
                else -> ""
            }
            "$vis ${p.content.take(20)}${if (p.content.length > 20) "..." else ""}"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("管理秘帖")
            .setItems(titles) { _, which ->
                val post = posts[which]
                AlertDialog.Builder(this)
                    .setTitle("操作")
                    .setItems(arrayOf("删除此秘帖", "修改可见范围")) { _, action ->
                        when (action) {
                            0 -> deletePost(post)
                            1 -> showChangeVisibilityDialog(post)
                        }
                    }
                    .show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun deletePost(post: SecretPost) {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除这条秘帖吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    val result = safeApiCall { RetrofitClient.apiService.deleteSecretPost(post.id) }
                    runOnUiThread {
                        result.onSuccess {
                            Toast.makeText(this@SecretSpaceActivity, "已删除", Toast.LENGTH_SHORT).show()
                            loadPosts()
                        }.onFailure {
                            Toast.makeText(this@SecretSpaceActivity, "删除失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showChangeVisibilityDialog(post: SecretPost) {
        val options = arrayOf("仅自己可见", "仅互关好友可见", "仅指定的人可见")
        val currentIdx = when (post.visibility) {
            "private" -> 0
            "mutual" -> 1
            "specified" -> 2
            else -> 0
        }
        AlertDialog.Builder(this)
            .setTitle("修改可见范围")
            .setSingleChoiceItems(options, currentIdx) { dialog, which ->
                val visibility = when (which) {
                    0 -> "private"
                    1 -> "mutual"
                    2 -> "specified"
                    else -> "private"
                }
                lifecycleScope.launch {
                    val result = safeApiCall {
                        RetrofitClient.apiService.updateSecretVisibility(post.id, SecretVisibilityRequest(visibility))
                    }
                    runOnUiThread {
                        result.onSuccess {
                            Toast.makeText(this@SecretSpaceActivity, "已更新", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            loadPosts()
                        }.onFailure {
                            Toast.makeText(this@SecretSpaceActivity, "更新失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showVisitsDialog() {
        lifecycleScope.launch {
            val result = safeApiCall { RetrofitClient.apiService.getSecretVisits() }
            runOnUiThread {
                result.onSuccess { visits ->
                    if (visits.isEmpty()) {
                        Toast.makeText(this@SecretSpaceActivity, "暂无访问记录", Toast.LENGTH_SHORT).show()
                        return@onSuccess
                    }
                    val me = SharedPreferencesManager.getUser()
                    val myVisits = visits.filter { it.spaceOwnerId == me?.id }
                    if (myVisits.isEmpty()) {
                        Toast.makeText(this@SecretSpaceActivity, "暂无访问记录", Toast.LENGTH_SHORT).show()
                        return@onSuccess
                    }
                    val titles = myVisits.takeLast(50).reversed().map { v ->
                        "${v.visitorName ?: "匿名用户"} · ${TimeUtils.formatRelativeTime(v.createdAt)}"
                    }.toTypedArray()
                    AlertDialog.Builder(this@SecretSpaceActivity)
                        .setTitle("最近访客")
                        .setItems(titles, null)
                        .setPositiveButton("关闭", null)
                        .show()
                }.onFailure {
                    Toast.makeText(this@SecretSpaceActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSettingsDialog() {
        val options = arrayOf("仅我自己可见（默认）", "仅指定的人可见", "仅互关好友可见")
        AlertDialog.Builder(this)
            .setTitle("空间默认可见范围")
            .setItems(options) { _, which ->
                val desc = when (which) {
                    0 -> "已设置为：仅自己可见"
                    1 -> "已设置为：仅指定的人可见（发布秘帖时可选择具体用户）"
                    2 -> "已设置为：仅互关好友可见"
                    else -> ""
                }
                Toast.makeText(this, desc, Toast.LENGTH_LONG).show()
            }
            .show()
    }

    override fun onBackPressed() {
        if (menuVisible) {
            closeMenu()
        } else {
            super.onBackPressed()
        }
    }

    class SecretPostAdapter(private val items: List<SecretPost>) : RecyclerView.Adapter<SecretPostAdapter.PostVH>() {
        class PostVH(v: View) : RecyclerView.ViewHolder(v) {
            val tvVisibility = v.findViewById<TextView>(R.id.tv_visibility)
            val tvTime = v.findViewById<TextView>(R.id.tv_time)
            val tvContent = v.findViewById<TextView>(R.id.tv_content)
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int): PostVH {
            return PostVH(LayoutInflater.from(p.context).inflate(R.layout.item_secret_post, p, false))
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(h: PostVH, pos: Int) {
            val item = items[pos]
            h.tvContent.text = item.content
            h.tvTime.text = TimeUtils.formatRelativeTime(item.createdAt)
            h.tvVisibility.text = when (item.visibility) {
                "private" -> "私密"
                "mutual" -> "互关可见"
                "specified" -> "指定可见"
                else -> "私密"
            }
        }
    }
}
