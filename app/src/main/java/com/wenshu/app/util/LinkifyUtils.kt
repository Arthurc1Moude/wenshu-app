package com.wenshu.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.wenshu.app.R
import com.wenshu.app.ui.webview.WebViewActivity
import java.util.regex.Pattern

object LinkifyUtils {

    private val URL_PATTERN = Pattern.compile(
        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val PHONE_PATTERN = Pattern.compile(
        "(?<!\\d)(1[3-9]\\d{9}|\\d{3,4}-?\\d{7,8})(?!\\d)"
    )

    private val LINK_COLOR = Color.parseColor("#0066CC")

    fun setupClickableLinks(textView: TextView, context: Context, originalText: String) {
        val spannable = SpannableString(originalText)
        var hasLinks = false

        val urlMatcher = URL_PATTERN.matcher(originalText)
        while (urlMatcher.find()) {
            val url = urlMatcher.group()
            val start = urlMatcher.start()
            val end = urlMatcher.end()
            hasLinks = true

            val clickSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    openUrl(context, url)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = LINK_COLOR
                    ds.isUnderlineText = true
                }
            }
            spannable.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        val phoneMatcher = PHONE_PATTERN.matcher(originalText)
        while (phoneMatcher.find()) {
            val phone = phoneMatcher.group()
            val cleanPhone = phone.replace("-", "")
            val start = phoneMatcher.start()
            val end = phoneMatcher.end()
            hasLinks = true

            val existingSpans = spannable.getSpans(start, end, ClickableSpan::class.java)
            if (existingSpans.isNotEmpty()) continue

            val clickSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    showPhoneDialog(context, cleanPhone)
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = LINK_COLOR
                    ds.isUnderlineText = true
                }
            }
            spannable.setSpan(clickSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannable
        if (hasLinks) {
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.highlightColor = Color.TRANSPARENT
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_URL, url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                android.widget.Toast.makeText(context, "无法打开链接", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showPhoneDialog(context: Context, phoneNumber: String) {
        val options = arrayOf("呼叫 $phoneNumber", "查询号码", "复制号码")
        AlertDialog.Builder(context)
            .setTitle(phoneNumber)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> makePhoneCall(context, phoneNumber)
                    1 -> queryPhoneNumber(context, phoneNumber)
                    2 -> copyToClipboard(context, phoneNumber)
                }
            }
            .show()
    }

    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                android.widget.Toast.makeText(context, "无法发起呼叫", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun queryPhoneNumber(context: Context, phoneNumber: String) {
        val searchUrl = "https://www.baidu.com/s?wd=$phoneNumber"
        openUrl(context, searchUrl)
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("phone_number", text)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "已复制: $text", android.widget.Toast.LENGTH_SHORT).show()
    }
}
