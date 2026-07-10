package com.wenshu.app.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivitySearchBinding
import com.wenshu.app.ui.adapters.PostGridAdapter
import com.wenshu.app.ui.postdetail.PostDetailActivity
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var postAdapter: PostGridAdapter
    private val repository = PostRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialQuery = intent.getStringExtra("query")
        if (initialQuery != null) {
            binding.etSearch.setText(initialQuery)
            performSearch()
        }

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        postAdapter = PostGridAdapter { post ->
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }
        binding.rvResults.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = postAdapter
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString().trim()
        if (query.isEmpty()) return

        binding.tvEmpty.visibility = View.GONE
        binding.rvResults.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = repository.search(query)
            result.onSuccess { searchResponse ->
                val posts = searchResponse.posts
                postAdapter.submitList(posts)
                if (posts.isEmpty()) {
                    binding.tvEmpty.text = "未找到相关内容"
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvResults.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvResults.visibility = View.VISIBLE
                }
            }.onFailure { error ->
                Toast.makeText(this@SearchActivity, error.message ?: "搜索失败", Toast.LENGTH_SHORT).show()
                binding.tvEmpty.text = "搜索失败，请重试"
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }
}
