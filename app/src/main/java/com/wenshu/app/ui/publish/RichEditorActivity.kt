package com.wenshu.app.ui.publish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.R

class RichEditorActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private var initialTitle: String = ""
    private var initialContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rich_editor)

        initialTitle = intent.getStringExtra("title") ?: ""
        initialContent = intent.getStringExtra("content") ?: ""

        etTitle = findViewById(R.id.et_title)
        etContent = findViewById(R.id.et_content)

        etTitle.setText(initialTitle)
        etContent.setText(initialContent)

        findViewById<ImageView>(R.id.btn_cancel).setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finishWithSlide()
        }

        findViewById<TextView>(R.id.btn_done).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString()
            if (content.isBlank()) {
                Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent().apply {
                putExtra("title", title)
                putExtra("content", content)
                putExtra("isLongPost", true)
            }
            setResult(Activity.RESULT_OK, intent)
            finishWithSlide()
        }

        setupFormattingButtons()
    }

    private fun finishWithSlide() {
        finish()
        overridePendingTransition(R.anim.no_anim, R.anim.slide_out_right)
    }

    @Deprecated("Use OnBackPressedCallback")
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finishWithSlide()
    }

    private fun setupFormattingButtons() {
        findViewById<ImageView>(R.id.btn_bold).setOnClickListener {
            insertFormatting("**", "**")
        }

        findViewById<ImageView>(R.id.btn_italic).setOnClickListener {
            insertFormatting("*", "*")
        }

        findViewById<ImageView>(R.id.btn_heading).setOnClickListener {
            insertAtLineStart("# ")
        }

        findViewById<ImageView>(R.id.btn_list).setOnClickListener {
            insertAtLineStart("- ")
        }

        findViewById<ImageView>(R.id.btn_quote).setOnClickListener {
            insertAtLineStart("> ")
        }

        findViewById<ImageView>(R.id.btn_link).setOnClickListener {
            insertLink()
        }

        findViewById<ImageView>(R.id.btn_divider).setOnClickListener {
            insertText("\n\n---\n\n")
        }
    }

    private fun insertFormatting(prefix: String, suffix: String) {
        val start = etContent.selectionStart
        val end = etContent.selectionEnd
        val text = etContent.text.toString()

        if (start == end) {
            etContent.text.insert(start, "$prefix$suffix")
            etContent.setSelection(start + prefix.length)
        } else {
            val selected = text.substring(start, end)
            etContent.text.replace(start, end, "$prefix$selected$suffix")
            etContent.setSelection(end + prefix.length + suffix.length)
        }
    }

    private fun insertAtLineStart(prefix: String) {
        val start = etContent.selectionStart
        val text = etContent.text.toString()

        var lineStart = start
        while (lineStart > 0 && text[lineStart - 1] != '\n') {
            lineStart--
        }

        etContent.text.insert(lineStart, prefix)
        etContent.setSelection(start + prefix.length)
    }

    private fun insertLink() {
        val start = etContent.selectionStart
        val end = etContent.selectionEnd
        val text = etContent.text.toString()

        if (start != end) {
            val selected = text.substring(start, end)
            etContent.text.replace(start, end, "[$selected](url)")
            etContent.setSelection(end + 3)
        } else {
            etContent.text.insert(start, "[链接文字](url)")
            etContent.setSelection(start + 7)
        }
    }

    private fun insertText(text: String) {
        val start = etContent.selectionStart
        etContent.text.insert(start, text)
        etContent.setSelection(start + text.length)
    }
}
