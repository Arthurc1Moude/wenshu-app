package com.wenshu.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.util.regex.Pattern

object LinkifyUtils {

    private val URL_PATTERN = Pattern.compile(
        "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val PHONE_PATTERN = Pattern.compile(
        "(?<!\\d)(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})(?!\\d)"
    )

    private const val URL_COLOR = Color.parseColor("#1E88E5")
    private const val PHONE_COLOR = Color.parseColor("#1E88E5")

    fun linkify(textView: TextView, onUrlClick: ((String) -> Unit)? = null) {
        val text = textView.text?.toString() ?: return
        if (text.isEmpty()) return

        val spannable = if (textView.text is SpannableStringBuilder) {
            textView.text as SpannableStringBuilder
        } else {
            SpannableStringBuilder(text)
        }

        addLinksToSpannable(spannable, onUrlClick)
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    fun addLinksToSpannable(spannable: SpannableStringBuilder, onUrlClick: ((String) -> Unit)? = null) {
        val text = spannable.toString()
        if (text.isEmpty()) return

        val urlSpans = mutableListOf<Triple<Int, Int, String>>()
        val phoneSpans = mutableListOf<Triple<Int, Int, String>>()

        val urlMatcher = URL_PATTERN.matcher(text)
        while (urlMatcher.find()) {
            val start = urlMatcher.start()
            val end = urlMatcher.end()
            val url = urlMatcher.group(1) ?: continue
            val hasOverlap = hasExistingSpan(spannable, start, end)
            if (!hasOverlap) {
                urlSpans.add(Triple(start, end, url))
            }
        }

        val phoneMatcher = PHONE_PATTERN.matcher(text)
        while (phoneMatcher.find()) {
            val start = phoneMatcher.start()
            val end = phoneMatcher.end()
            val phone = phoneMatcher.group(1) ?: continue
            var overlap = hasExistingSpan(spannable, start, end)
            if (!overlap) {
                for ((s, e, _) in urlSpans) {
                    if (start < e && end > s) { overlap = true; break }
                }
            }
            if (!overlap) {
                phoneSpans.add(Triple(start, end, phone))
            }
        }

        for ((start, end, url) in urlSpans) {
            spannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (onUrlClick != null) {
                            onUrlClick(url)
                        } else {
                            openUrl(widget.context, url)
                        }
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = URL_COLOR
                        ds.isUnderlineText = true
                    }
                },
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(URL_COLOR),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                UnderlineSpan(),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        for ((start, end, phone) in phoneSpans) {
            spannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        showPhoneDialog(widget.context, phone)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = PHONE_COLOR
                        ds.isUnderlineText = true
                    }
                },
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(PHONE_COLOR),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                UnderlineSpan(),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun hasExistingSpan(spannable: SpannableStringBuilder, start: Int, end: Int): Boolean {
        val clickSpans = spannable.getSpans(start, end, ClickableSpan::class.java)
        return clickSpans.isNotEmpty()
    }

    fun extractUrls(text: String): List<String> {
        val urls = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(text)
        while (matcher.find()) {
            matcher.group(1)?.let { urls.add(it) }
        }
        return urls.distinct()
    }

    private fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(context, Class.forName("com.wenshu.app.ui.web.WebViewActivity"))
            intent.putExtra("url", if (url.startsWith("http")) url else "https://$url")
            context.startActivity(intent)
        } catch (e: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(if (url.startsWith("http")) url else "https://$url"))
            context.startActivity(browserIntent)
        }
    }

    fun showPhoneDialog(context: Context, phone: String) {
        val options = arrayOf("呼叫", "查询号码", "复制号码")
        AlertDialog.Builder(context)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(callIntent)
                    }
                    1 -> {
                        val queryIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com/s?wd=$phone"))
                        context.startActivity(queryIntent)
                    }
                    2 -> {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("phone", phone)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "号码已复制", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
}
