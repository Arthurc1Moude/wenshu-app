package com.wenshu.app.util

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import com.wenshu.app.ui.web.WebViewActivity

object MarkdownUtils {

    private const val LINK_COLOR = Color.parseColor("#1E88E5")

    fun render(markdown: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val lines = markdown.split("\n")

        var inList = false

        for ((idx, line) in lines.withIndex()) {
            if (idx > 0) {
                builder.append("\n")
            }

            when {
                line.startsWith("### ") -> {
                    appendHeading(builder, line.removePrefix("### "), 1.15f, Typeface.BOLD)
                }
                line.startsWith("## ") -> {
                    appendHeading(builder, line.removePrefix("## "), 1.25f, Typeface.BOLD)
                }
                line.startsWith("# ") -> {
                    appendHeading(builder, line.removePrefix("# "), 1.4f, Typeface.BOLD)
                }
                line.startsWith("> ") -> {
                    appendQuote(builder, line.removePrefix("> "))
                }
                line.startsWith("- ") || line.matches(Regex("^\\d+\\.\\s.*")) -> {
                    appendListItem(builder, line, line.startsWith("- "))
                }
                line.trim() == "---" -> {
                    appendDivider(builder)
                }
                line.isBlank() -> {
                }
                else -> {
                    appendFormattedText(builder, line)
                }
            }
        }

        return builder
    }

    private fun appendHeading(builder: SpannableStringBuilder, text: String, sizeMult: Float, style: Int) {
        val start = builder.length
        appendFormattedText(builder, text)
        val end = builder.length
        builder.setSpan(RelativeSizeSpan(sizeMult), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun appendQuote(builder: SpannableStringBuilder, text: String) {
        val start = builder.length
        appendFormattedText(builder, text)
        val end = builder.length
        builder.setSpan(QuoteSpan(0xFF333333.toInt()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(LeadingMarginSpan.Standard(40, 20), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun appendListItem(builder: SpannableStringBuilder, line: String, isBullet: Boolean) {
        val text = if (isBullet) {
            "•  ${line.removePrefix("- ")}"
        } else {
            val match = Regex("^(\\d+)\\.\\s(.*)").find(line)
            if (match != null) {
                "${match.groupValues[1]}.  ${match.groupValues[2]}"
            } else line
        }
        val start = builder.length
        appendFormattedText(builder, text)
        val end = builder.length
        builder.setSpan(LeadingMarginSpan.Standard(30, 10), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun appendDivider(builder: SpannableStringBuilder) {
        val start = builder.length
        builder.append("──────────")
        val end = builder.length
        builder.setSpan(ForegroundColorSpan(0xFFCCCCCC.toInt()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun appendFormattedText(builder: SpannableStringBuilder, text: String) {
        val linkPattern = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
        val boldPattern = Regex("\\*\\*([^*]+)\\*\\*")
        val italicPattern = Regex("\\*([^*]+)\\*")
        val codePattern = Regex("`([^`]+)`")

        var lastPos = 0
        var remaining = text

        val allPatterns = mutableListOf<Pair<IntRange, () -> Unit>>()

        fun findAll(regex: Regex, handler: (MatchResult) -> Unit) {
            regex.findAll(remaining).forEach { match ->
                allPatterns.add(match.range to { handler(match) })
            }
        }

        val processed = mutableSetOf<Int>()
        var pos = 0
        while (pos < remaining.length) {
            var found: MatchResult? = null
            var foundType = 0
            var foundStart = remaining.length

            val linkMatch = linkPattern.find(remaining.substring(pos))
            if (linkMatch != null) {
                val absStart = pos + linkMatch.range.first
                if (absStart < foundStart) {
                    foundStart = absStart
                    found = linkMatch
                    foundType = 1
                }
            }
            val boldMatch = boldPattern.find(remaining.substring(pos))
            if (boldMatch != null) {
                val absStart = pos + boldMatch.range.first
                if (absStart < foundStart) {
                    foundStart = absStart
                    found = boldMatch
                    foundType = 2
                }
            }
            val italicMatch = italicPattern.find(remaining.substring(pos))
            if (italicMatch != null) {
                val absStart = pos + italicMatch.range.first
                if (absStart < foundStart) {
                    foundStart = absStart
                    found = italicMatch
                    foundType = 3
                }
            }
            val codeMatch = codePattern.find(remaining.substring(pos))
            if (codeMatch != null) {
                val absStart = pos + codeMatch.range.first
                if (absStart < foundStart) {
                    foundStart = absStart
                    found = codeMatch
                    foundType = 4
                }
            }

            if (found == null) {
                builder.append(remaining.substring(pos))
                break
            }

            val absRange = (pos + found.range.first)..(pos + found.range.last)
            if (absRange.first > pos) {
                builder.append(remaining.substring(pos, absRange.first))
            }

            val spanStart = builder.length

            when (foundType) {
                1 -> {
                    val linkText = found.groupValues[1]
                    val linkUrl = found.groupValues[2]
                    builder.append(linkText)
                    val spanEnd = builder.length
                    builder.setSpan(object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            try {
                                val context = widget.context
                                val intent = android.content.Intent(context, WebViewActivity::class.java)
                                intent.putExtra("url", linkUrl)
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        override fun updateDrawState(ds: android.text.TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = LINK_COLOR
                            ds.isUnderlineText = true
                        }
                    }, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(LINK_COLOR), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(UnderlineSpan(), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                2 -> {
                    val inner = found.groupValues[1]
                    builder.append(inner)
                    val spanEnd = builder.length
                    builder.setSpan(StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                3 -> {
                    val inner = found.groupValues[1]
                    builder.append(inner)
                    val spanEnd = builder.length
                    builder.setSpan(StyleSpan(Typeface.ITALIC), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                4 -> {
                    val inner = found.groupValues[1]
                    builder.append(inner)
                    val spanEnd = builder.length
                    builder.setSpan(TypefaceSpan("monospace"), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    builder.setSpan(ForegroundColorSpan(0xFF666666.toInt()), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            pos = absRange.last + 1
        }
    }

    fun renderTo(textView: TextView, content: String, isLongPost: Boolean, onUrlClick: ((String) -> Unit)? = null) {
        if (isLongPost) {
            val spannable = render(content)
            LinkifyUtils.addLinksToSpannable(spannable, onUrlClick)
            textView.text = spannable
            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.highlightColor = Color.TRANSPARENT
        } else {
            textView.text = content
        }
    }
}
