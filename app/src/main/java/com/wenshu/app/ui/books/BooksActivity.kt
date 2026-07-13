package com.wenshu.app.ui.books

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.Book
import com.wenshu.app.data.model.CreateBookRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class BooksActivity : AppCompatActivity() {
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recycler: RecyclerView
    private lateinit var progress: View
    private lateinit var tvEmpty: TextView
    private lateinit var tvTitle: TextView
    private var bookType = "book"
    private val books = mutableListOf<Book>()
    private lateinit var adapter: BookAdapter
    private var uploadedFileUrl: String? = null
    private var filePickDialogView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books)
        bookType = intent.getStringExtra("type") ?: "book"

        swipeRefresh = findViewById(R.id.swipe_refresh)
        recycler = findViewById(R.id.recycler_books)
        progress = findViewById(R.id.progress_loading)
        tvEmpty = findViewById(R.id.tv_empty)
        tvTitle = findViewById(R.id.toolbar_title)

        tvTitle.text = if (bookType == "novel") "文书小说" else "文书阅读"
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.fab_upload).setOnClickListener { showUploadDialog() }

        adapter = BookAdapter(books) { book -> openBook(book) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        swipeRefresh.setColorSchemeColors(getColor(R.color.seal))
        swipeRefresh.setOnRefreshListener { loadBooks() }
        loadBooks()
    }

    private fun loadBooks() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = safeApiCall { RetrofitClient.apiService.getBooks(if (bookType == "novel") "novel" else null) }
            runOnUiThread {
                progress.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                result.onSuccess { list ->
                    books.clear()
                    val myId = SharedPreferencesManager.getUser()?.id
                    books.addAll(list.filter { !it.isPrivate || it.authorId == myId })
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
                }.onFailure { Toast.makeText(this@BooksActivity, "加载失败: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun showUploadDialog() {
        uploadedFileUrl = null
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_book, null)
        filePickDialogView = dialogView
        val etTitle = dialogView.findViewById<EditText>(R.id.et_title)
        val etDesc = dialogView.findViewById<EditText>(R.id.et_desc)
        val etContent = dialogView.findViewById<EditText>(R.id.et_content)
        val btnSelectFile = dialogView.findViewById<TextView>(R.id.btn_select_file)
        val categoryGroup = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.chip_category)
        val switchPrivate = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_private)

        if (bookType == "novel") { categoryGroup.check(R.id.chip_novel); categoryGroup.visibility = View.GONE }

        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }
            startActivityForResult(Intent.createChooser(intent, "选择图书文件"), 100)
        }

        AlertDialog.Builder(this)
            .setTitle(if (bookType == "novel") "发布小说" else "发布图书")
            .setView(dialogView)
            .setPositiveButton("发布") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val content = etContent.text.toString().trim()
                if (title.isEmpty()) { Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val category = if (bookType == "novel") "novel" else if (categoryGroup.checkedChipId == R.id.chip_novel) "novel" else "book"
                uploadBook(title, desc, content, category, switchPrivate.isChecked)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val name = getFileName(uri)
                filePickDialogView?.findViewById<TextView>(R.id.tv_filename)?.text = "已选: $name"
                uploadFile(uri)
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) name = cursor.getString(idx)
        }
        return name
    }

    private fun uploadFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val tmpFile = File(cacheDir, "upload_${System.currentTimeMillis()}")
                contentResolver.openInputStream(uri)?.use { it.copyTo(FileOutputStream(tmpFile)) }
                val body = tmpFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tmpFile.name, body)
                val result = safeApiCall { RetrofitClient.apiService.uploadBookFile(part) }
                result.onSuccess { uploadedFileUrl = it.url }
                    .onFailure { runOnUiThread { Toast.makeText(this@BooksActivity, "文件上传失败", Toast.LENGTH_SHORT).show() } }
                tmpFile.delete()
            } catch (e: Exception) { runOnUiThread { Toast.makeText(this@BooksActivity, "文件错误", Toast.LENGTH_SHORT).show() } }
        }
    }

    private fun uploadBook(title: String, desc: String, content: String, category: String, isPrivate: Boolean) {
        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.createBook(CreateBookRequest(title, desc, category, content, uploadedFileUrl ?: "", "", isPrivate))
            }
            runOnUiThread {
                result.onSuccess { Toast.makeText(this@BooksActivity, "发布成功!", Toast.LENGTH_SHORT).show(); loadBooks() }
                    .onFailure { Toast.makeText(this@BooksActivity, "发布失败: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun openBook(book: Book) {
        lifecycleScope.launch { safeApiCall { RetrofitClient.apiService.markBookRead(book.id) } }
        startActivity(Intent(this, BookReadActivity::class.java).apply {
            putExtra("title", book.title); putExtra("content", book.content)
            putExtra("fileUrl", book.fileUrl); putExtra("authorName", book.authorName)
        })
    }

    class BookAdapter(private val items: List<Book>, private val onClick: (Book) -> Unit) : RecyclerView.Adapter<BookAdapter.BookVH>() {
        class BookVH(v: View) : RecyclerView.ViewHolder(v) {
            val imgIcon = v.findViewById<ImageView>(R.id.img_icon)
            val imgCover = v.findViewById<ImageView>(R.id.img_cover)
            val tvTitle = v.findViewById<TextView>(R.id.tv_title)
            val tvAuthor = v.findViewById<TextView>(R.id.tv_author)
            val tvDesc = v.findViewById<TextView>(R.id.tv_desc)
            val tvCategory = v.findViewById<TextView>(R.id.tv_category)
            val tvStats = v.findViewById<TextView>(R.id.tv_stats)
            val imgPrivate = v.findViewById<ImageView>(R.id.img_private)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int): BookVH {
            return BookVH(LayoutInflater.from(p.context).inflate(R.layout.item_book, p, false))
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(h: BookVH, pos: Int) {
            val b = items[pos]
            h.tvTitle.text = b.title
            h.tvAuthor.text = b.authorName ?: "匿名"
            h.tvDesc.text = b.description.ifEmpty { "暂无简介" }
            h.tvCategory.text = if (b.category == "novel") "小说" else "图书"
            h.tvStats.text = "${b.readCount}阅读"
            h.imgPrivate.visibility = if (b.isPrivate) View.VISIBLE else View.GONE
            if (b.coverUrl.isNotEmpty()) {
                h.imgCover.visibility = View.VISIBLE
                h.imgIcon.visibility = View.GONE
                Glide.with(h.imgCover).load(b.coverUrl).into(h.imgCover)
            } else {
                h.imgCover.visibility = View.GONE
                h.imgIcon.visibility = View.VISIBLE
                h.imgIcon.setImageResource(R.drawable.ic_book_placeholder)
            }
            h.itemView.setOnClickListener { onClick(b) }
        }
    }
}
