package com.wenshu.app.ui.books

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.R

class BookReadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_read)
        val title = intent.getStringExtra("title") ?: "阅读"
        val author = intent.getStringExtra("authorName") ?: ""
        val content = intent.getStringExtra("content") ?: ""
        val fileUrl = intent.getStringExtra("fileUrl") ?: ""
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_title).text = title
        findViewById<TextView>(R.id.tv_author).text = author
        val tvContent = findViewById<TextView>(R.id.tv_content)
        val progress = findViewById<ProgressBar>(R.id.progress)
        if (fileUrl.isNotEmpty() && fileUrl.endsWith(".pdf", true)) {
            tvContent.visibility = View.GONE
            val webView = WebView(this).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(-1, -1)
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                val base = "https://wenshu-server.onrender.com"
                val fullUrl = if (fileUrl.startsWith("http")) fileUrl else base + fileUrl
                loadUrl("https://docs.google.com/gview?embedded=true&url=${java.net.URLEncoder.encode(fullUrl, "UTF-8")}")
            }
            (tvContent.parent as android.widget.FrameLayout).addView(webView)
        } else if (fileUrl.isNotEmpty() && fileUrl.endsWith(".txt", true)) {
            tvContent.text = "正在加载..."
            progress.visibility = View.VISIBLE
            Thread {
                try {
                    val base = "https://wenshu-server.onrender.com"
                    val fullUrl = if (fileUrl.startsWith("http")) fileUrl else base + fileUrl
                    val text = java.net.URL(fullUrl).readText()
                    runOnUiThread { tvContent.text = text; progress.visibility = View.GONE }
                } catch (e: Exception) {
                    runOnUiThread { tvContent.text = content.ifEmpty { "无法加载文件: ${e.message}" }; progress.visibility = View.GONE }
                }
            }.start()
        } else {
            tvContent.text = if (content.isNotEmpty()) content else "暂无内容"
            progress.visibility = View.GONE
        }
    }
}
