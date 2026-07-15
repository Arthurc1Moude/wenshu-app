package com.wenshu.app.ui.web

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wenshu.app.R

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvTitle: TextView
    private var currentUrl: String = ""
    private var pageTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val title = intent.getStringExtra("title") ?: ""
        currentUrl = intent.getStringExtra("url") ?: ""

        tvTitle = findViewById(R.id.tv_title)
        webView = findViewById(R.id.webview)

        findViewById<ImageView>(R.id.btn_close).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_more).setOnClickListener { showOptionsMenu() }

        tvTitle.text = if (title.isNotBlank()) title else currentUrl

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { currentUrl = it }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                title?.let {
                    pageTitle = it
                    tvTitle.text = it
                }
            }
        }

        if (currentUrl.isNotEmpty()) {
            if (!currentUrl.startsWith("http")) {
                currentUrl = "https://$currentUrl"
            }
            webView.loadUrl(currentUrl)
        }
    }

    private fun showOptionsMenu() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_webview_options, null)
        dialog.setContentView(view)

        view.findViewById<LinearLayout>(R.id.option_refresh).setOnClickListener {
            webView.reload()
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.option_copy_link).setOnClickListener {
            copyLink()
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.option_open_browser).setOnClickListener {
            openInBrowser()
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.option_share).setOnClickListener {
            shareLink()
            dialog.dismiss()
        }

        view.findViewById<LinearLayout>(R.id.option_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun copyLink() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("url", currentUrl)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "链接已复制", Toast.LENGTH_SHORT).show()
    }

    private fun openInBrowser() {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
            startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开浏览器", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareLink() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, currentUrl)
                putExtra(Intent.EXTRA_SUBJECT, pageTitle)
            }
            startActivity(Intent.createChooser(shareIntent, "分享链接"))
        } catch (e: Exception) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
