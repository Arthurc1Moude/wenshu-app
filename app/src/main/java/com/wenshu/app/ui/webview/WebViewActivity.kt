package com.wenshu.app.ui.webview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.R

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvTitle: TextView
    private lateinit var btnClose: ImageView
    private lateinit var btnMore: ImageView

    private var currentUrl: String = ""
    private var pageTitle: String = ""

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.web_view)
        tvTitle = findViewById(R.id.tv_title)
        btnClose = findViewById(R.id.btn_close)
        btnMore = findViewById(R.id.btn_more)

        currentUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        pageTitle = intent.getStringExtra(EXTRA_TITLE) ?: currentUrl

        tvTitle.text = pageTitle

        setupWebView()
        setupButtons()

        if (currentUrl.isNotEmpty()) {
            webView.loadUrl(currentUrl)
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = userAgentString + " WenshuApp/1.0"
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    currentUrl = url
                    view?.loadUrl(url)
                    return false
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val title = view?.title
                if (!title.isNullOrEmpty()) {
                    pageTitle = title
                    tvTitle.text = title
                }
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrEmpty()) {
                    pageTitle = title
                    tvTitle.text = title
                }
            }
        }
    }

    private fun setupButtons() {
        btnClose.setOnClickListener { finish() }
        btnMore.setOnClickListener { showMoreMenu(it) }
    }

    private fun showMoreMenu(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.dialog_web_more, null)
        val popupWindow = PopupWindow(
            popupView,
            resources.getDimensionPixelSize(R.dimen.popup_menu_width),
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 8f
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.bg_bottom_sheet))

        popupView.findViewById<View>(R.id.btn_refresh).setOnClickListener {
            webView.reload()
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_copy_link).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("url", currentUrl))
            Toast.makeText(this, "链接已复制", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_open_browser).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "无法打开浏览器", Toast.LENGTH_SHORT).show()
            }
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_share).setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$pageTitle\n$currentUrl")
            }
            startActivity(Intent.createChooser(shareIntent, "分享链接"))
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(btnMore, 0, 0)
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
