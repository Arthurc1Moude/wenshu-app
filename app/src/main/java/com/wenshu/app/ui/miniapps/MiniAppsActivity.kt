package com.wenshu.app.ui.miniapps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.MiniApp
import com.wenshu.app.ui.web.WebViewActivity
import kotlinx.coroutines.launch

class MiniAppsActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_items)
        recycler = findViewById(R.id.recycler_items)
        progress = findViewById(R.id.progress_loading)
        swipe = findViewById(R.id.swipe_refresh)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_title).text = "文书小应用"
        findViewById<ImageView>(R.id.fab_publish).setOnClickListener { showPublishDialog() }
        adapter = MyAdapter { item ->
            startActivity(Intent(this, WebViewActivity::class.java).putExtra("title", item.name).putExtra("url", item.url))
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        swipe.setColorSchemeColors(getColor(R.color.seal))
        swipe.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val r = safeApiCall { RetrofitClient.apiService.getMiniApps() }
            runOnUiThread {
                progress.visibility = View.GONE; swipe.isRefreshing = false
                r.onSuccess { adapter.update(it) }
                    .onFailure { Toast.makeText(this@MiniAppsActivity, "加载失败", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun showPublishDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_publish_url, null)
        AlertDialog.Builder(this).setTitle("发布小应用").setView(v)
            .setPositiveButton("发布") { _, _ ->
                val name = v.findViewById<EditText>(R.id.et_name).text.toString().trim()
                val url = v.findViewById<EditText>(R.id.et_url).text.toString().trim()
                val desc = v.findViewById<EditText>(R.id.et_desc).text.toString().trim()
                if (name.isEmpty() || url.isEmpty()) { Toast.makeText(this, "请填写名称和URL", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                lifecycleScope.launch {
                    val r = safeApiCall { RetrofitClient.apiService.createMiniApp(mapOf("name" to name, "url" to url, "description" to desc)) }
                    runOnUiThread { r.onSuccess { load() }.onFailure { Toast.makeText(this@MiniAppsActivity, "发布失败", Toast.LENGTH_SHORT).show() } }
                }
            }.setNegativeButton("取消", null).show()
    }

    class MyAdapter(private val onClick: (MiniApp) -> Unit) : RecyclerView.Adapter<MyAdapter.AppVH>() {
        private val items = mutableListOf<MiniApp>()
        fun update(list: List<MiniApp>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
        class AppVH(v: View) : RecyclerView.ViewHolder(v) {
            val imgIcon = v.findViewById<ImageView>(R.id.img_icon)
            val name = v.findViewById<TextView>(R.id.tv_name)
            val desc = v.findViewById<TextView>(R.id.tv_desc)
            val meta = v.findViewById<TextView>(R.id.tv_meta)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int): AppVH {
            return AppVH(LayoutInflater.from(p.context).inflate(R.layout.item_app_game, p, false))
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(h: AppVH, pos: Int) {
            val item = items[pos]
            h.name.text = item.name
            h.desc.text = item.description
            h.meta.text = item.developerName
            if (item.icon.isNotEmpty()) {
                Glide.with(h.imgIcon).load(item.icon).into(h.imgIcon)
            } else {
                h.imgIcon.setImageResource(R.drawable.ic_app_placeholder)
            }
            h.itemView.setOnClickListener { onClick(item) }
        }
    }
}
