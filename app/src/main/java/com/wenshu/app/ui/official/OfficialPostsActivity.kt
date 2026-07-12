package com.wenshu.app.ui.official

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.Post
import com.wenshu.app.ui.adapters.PostCardAdapter
import com.wenshu.app.ui.postdetail.PostDetailActivity
import com.wenshu.app.ui.profile.UserProfileActivity
import kotlinx.coroutines.launch

class OfficialPostsActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var adapter: PostCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)
        recycler = findViewById(R.id.recycler_books)
        progress = findViewById(R.id.progress_loading)
        swipe = findViewById(R.id.swipe_refresh)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.toolbar_title).text = "文书天地"
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_upload).visibility = View.GONE

        adapter = PostCardAdapter(
            onPostClick = { post ->
                startActivity(Intent(this, PostDetailActivity::class.java).putExtra("postId", post.id))
            },
            onLikeClick = { post ->
                lifecycleScope.launch {
                    safeApiCall { RetrofitClient.apiService.toggleLike(post.id) }
                }
            },
            onUserClick = { post ->
                startActivity(Intent(this, UserProfileActivity::class.java).putExtra("userId", post.authorId))
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        swipe.setColorSchemeColors(getColor(R.color.seal))
        swipe.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val r = safeApiCall { RetrofitClient.apiService.getOfficialPosts() }
            runOnUiThread {
                progress.visibility = View.GONE
                swipe.isRefreshing = false
                r.onSuccess { adapter.submitList(it) }
                    .onFailure { Toast.makeText(this@OfficialPostsActivity, "加载失败", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
