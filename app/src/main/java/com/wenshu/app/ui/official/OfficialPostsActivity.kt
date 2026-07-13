package com.wenshu.app.ui.official

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.ui.adapters.PostCardAdapter
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.ui.profile.UserProfileActivity
import kotlinx.coroutines.launch

class OfficialPostsActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var adapter: PostCardAdapter
    private var posts: List<Post> = emptyList()
    private val repository = PostRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)
        recycler = findViewById(R.id.recycler_books)
        progress = findViewById(R.id.progress_loading)
        swipe = findViewById(R.id.swipe_refresh)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.toolbar_title).text = "文书天地"
        findViewById<ImageView>(R.id.fab_upload).visibility = View.GONE

        adapter = PostCardAdapter(
            onPostClick = { post ->
                startActivity(Intent(this, PostDetailActivity::class.java).putExtra("post_id", post.id))
            },
            onLikeClick = { post ->
                toggleLike(post)
            },
            onCoinClick = { post ->
                tipPost(post)
            },
            onUserClick = { post ->
                val uid = post.authorId ?: post.author?.id ?: return@PostCardAdapter
                startActivity(Intent(this, UserProfileActivity::class.java).putExtra("user_id", uid))
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        swipe.setColorSchemeColors(getColor(R.color.seal))
        swipe.setOnRefreshListener { load() }
        load()
    }

    private fun toggleLike(post: Post) {
        lifecycleScope.launch {
            val result = repository.toggleLike(post.id)
            result.onSuccess { resp ->
                updatePostInList(post.id) { it.copy(isLiked = resp.isLiked, likeCount = resp.likeCount) }
            }.onFailure {
                Toast.makeText(this@OfficialPostsActivity, it.message ?: "操作失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tipPost(post: Post) {
        val amount = SharedPreferencesManager.getDefaultTipAmount()
        lifecycleScope.launch {
            val result = repository.tipPost(post.id, amount)
            result.onSuccess { resp ->
                updatePostInList(post.id) { it.copy(isTipped = resp.isTipped, coinCount = resp.coinCount) }
                Toast.makeText(this@OfficialPostsActivity, "投入${resp.amount}文书币", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@OfficialPostsActivity, it.message ?: "投币失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePostInList(postId: String, transform: (Post) -> Post) {
        posts = posts.map { if (it.id == postId) transform(it) else it }
        adapter.submitList(posts)
    }

    private fun load() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val r = safeApiCall { RetrofitClient.apiService.getOfficialPosts() }
            runOnUiThread {
                progress.visibility = View.GONE
                swipe.isRefreshing = false
                r.onSuccess {
                    posts = it
                    adapter.submitList(it)
                }.onFailure { Toast.makeText(this@OfficialPostsActivity, "加载失败", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
