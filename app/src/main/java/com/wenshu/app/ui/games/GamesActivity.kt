package com.wenshu.app.ui.games

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
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.Game
import com.wenshu.app.ui.web.WebViewActivity
import kotlinx.coroutines.launch

class GamesActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var swipe: SwipeRefreshLayout
    private val items = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_items)
        recycler = findViewById(R.id.recycler_items); progress = findViewById(R.id.progress_loading); swipe = findViewById(R.id.swipe_refresh)
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_title).text = "文书游戏"
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_publish).setOnClickListener { showPublishDialog() }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = Adapter(items) { g -> playGame(g) }
        swipe.setColorSchemeColors(getColor(R.color.seal))
        swipe.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val r = safeApiCall { RetrofitClient.apiService.getGames() }
            runOnUiThread {
                progress.visibility = View.GONE; swipe.isRefreshing = false
                r.onSuccess { items.clear(); items.addAll(it); recycler.adapter?.notifyDataSetChanged() }
                    .onFailure { Toast.makeText(this@GamesActivity, "加载失败", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun showPublishDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_publish_url, null)
        val etName = v.findViewById<EditText>(R.id.et_name)
        val etUrl = v.findViewById<EditText>(R.id.et_url)
        val etDesc = v.findViewById<EditText>(R.id.et_desc)
        v.findViewById<TextView>(R.id.tv_dialog_title).text = "发布网页游戏"
        AlertDialog.Builder(this).setTitle("发布游戏").setView(v)
            .setPositiveButton("发布") { _, _ ->
                val name = etName.text.toString().trim()
                val url = etUrl.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                if (name.isEmpty() || url.isEmpty()) { Toast.makeText(this, "请填写名称和URL", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                publish(name, url, desc)
            }.setNegativeButton("取消", null).show()
    }

    private fun publish(name: String, url: String, desc: String) {
        lifecycleScope.launch {
            val r = safeApiCall { RetrofitClient.apiService.createGame(mapOf("name" to name, "url" to url, "description" to desc)) }
            runOnUiThread { r.onSuccess { load() }.onFailure { Toast.makeText(this@GamesActivity, "发布失败", Toast.LENGTH_SHORT).show() } }
        }
    }

    private fun playGame(g: Game) {
        lifecycleScope.launch { safeApiCall { RetrofitClient.apiService.markGamePlay(g.id) } }
        startActivity(Intent(this, WebViewActivity::class.java).putExtra("title", g.name).putExtra("url", g.url))
    }

    class Adapter(private val list: List<Game>, private val onClick: (Game) -> Unit) : RecyclerView.Adapter<Adapter.GameVH>() {
        class GameVH(v: View) : RecyclerView.ViewHolder(v) {
            val icon = v.findViewById<TextView>(R.id.tv_icon)
            val name = v.findViewById<TextView>(R.id.tv_name)
            val desc = v.findViewById<TextView>(R.id.tv_desc)
            val meta = v.findViewById<TextView>(R.id.tv_meta)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int): GameVH {
            return GameVH(LayoutInflater.from(p.context).inflate(R.layout.item_app_game, p, false))
        }
        override fun getItemCount() = list.size
        override fun onBindViewHolder(h: GameVH, pos: Int) {
            val item = list[pos]
            h.icon.text = item.icon.ifEmpty { "🎮" }
            h.name.text = item.name
            h.desc.text = item.description
            h.meta.text = "${item.developerName} · ${item.plays}次游玩"
            h.itemView.setOnClickListener { onClick(item) }
        }
    }
}
