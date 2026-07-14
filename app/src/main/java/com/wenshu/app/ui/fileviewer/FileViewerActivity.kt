package com.wenshu.app.ui.fileviewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.model.FileAttachment
import com.wenshu.app.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileViewerActivity : AppCompatActivity() {

    private lateinit var imgFileIcon: ImageView
    private lateinit var tvFileName: TextView
    private lateinit var tvFileInfo: TextView
    private lateinit var progressDownload: ProgressBar
    private lateinit var tvDownloadStatus: TextView
    private lateinit var btnSave: android.widget.Button
    private lateinit var btnOpenWith: android.widget.Button
    private lateinit var btnBack: ImageView

    private var fileAttachment: FileAttachment? = null
    private var downloadedFile: File? = null
    private var downloadJob: Job? = null

    companion object {
        const val EXTRA_FILE = "extra_file"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_viewer)

        imgFileIcon = findViewById(R.id.img_file_icon)
        tvFileName = findViewById(R.id.tv_file_name)
        tvFileInfo = findViewById(R.id.tv_file_info)
        progressDownload = findViewById(R.id.progress_download)
        tvDownloadStatus = findViewById(R.id.tv_download_status)
        btnSave = findViewById(R.id.btn_save)
        btnOpenWith = findViewById(R.id.btn_open_with)
        btnBack = findViewById(R.id.btn_back)

        @Suppress("DEPRECATION")
        fileAttachment = intent.getSerializableExtra(EXTRA_FILE) as? FileAttachment

        fileAttachment?.let { file ->
            tvFileName.text = file.originalName
            val expiresText = if (file.isPermanent) {
                "永久保存"
            } else if (file.expiresAt != null) {
                val sdf = SimpleDateFormat("MM月dd日过期", Locale.CHINESE)
                sdf.format(Date(file.expiresAt))
            } else "14天后过期"
            tvFileInfo.text = "${file.sizeFormatted} · $expiresText"

            val iconRes = getFileIconResource(file.iconType, file.ext)
            imgFileIcon.setImageResource(iconRes)
        }

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            startDownload()
        }

        btnOpenWith.setOnClickListener {
            openWithOtherApp()
        }
    }

    private fun getFileIconResource(iconType: String, ext: String): Int {
        return when (iconType) {
            "image" -> R.drawable.ic_file_image
            "video" -> R.drawable.ic_file_video
            "audio" -> R.drawable.ic_file_audio
            "document" -> R.drawable.ic_file_document
            "spreadsheet" -> R.drawable.ic_file_spreadsheet
            "presentation" -> R.drawable.ic_file_presentation
            "archive" -> R.drawable.ic_file_archive
            "code" -> R.drawable.ic_file_code
            else -> when {
                ext.equals("pdf", true) -> R.drawable.ic_file_document
                ext.isEmpty() -> R.drawable.ic_file_unknown
                else -> R.drawable.ic_file_generic
            }
        }
    }

    private fun startDownload() {
        val file = fileAttachment ?: return
        btnSave.isEnabled = false
        btnSave.text = "下载中..."
        progressDownload.visibility = View.VISIBLE
        tvDownloadStatus.visibility = View.VISIBLE
        progressDownload.progress = 0

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = ImageUtils.normalizeUrl(file.url) ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FileViewerActivity, "下载失败", Toast.LENGTH_SHORT).show()
                        resetDownloadButton()
                    }
                    return@launch
                }
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FileViewerActivity, "下载失败", Toast.LENGTH_SHORT).show()
                        resetDownloadButton()
                    }
                    return@launch
                }

                val body = response.body
                val contentLength = body?.contentLength() ?: -1L
                val inputStream = body?.byteStream()

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val outputFile = File(downloadsDir, file.originalName)
                var targetFile = outputFile
                var counter = 1
                while (targetFile.exists()) {
                    val nameWithoutExt = file.originalName.substringBeforeLast(".")
                    val ext = file.originalName.substringAfterLast(".", "")
                    targetFile = if (ext.isNotEmpty()) {
                        File(downloadsDir, "${nameWithoutExt}_$counter.$ext")
                    } else {
                        File(downloadsDir, "${file.originalName}_$counter")
                    }
                    counter++
                }

                val outputStream = FileOutputStream(targetFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead: Long = 0

                inputStream?.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                withContext(Dispatchers.Main) {
                                    progressDownload.progress = progress
                                    tvDownloadStatus.text = "${formatSize(totalBytesRead)} / ${formatSize(contentLength)}"
                                }
                            }
                        }
                    }
                }

                downloadedFile = targetFile

                withContext(Dispatchers.Main) {
                    progressDownload.progress = 100
                    tvDownloadStatus.text = "下载完成"
                    btnSave.visibility = View.GONE
                    btnOpenWith.visibility = View.VISIBLE
                    Toast.makeText(this@FileViewerActivity, "已保存到下载目录", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FileViewerActivity, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetDownloadButton()
                }
            }
        }
    }

    private fun resetDownloadButton() {
        btnSave.isEnabled = true
        btnSave.text = "保存"
        progressDownload.visibility = View.GONE
        tvDownloadStatus.visibility = View.GONE
    }

    private fun openWithOtherApp() {
        val file = downloadedFile ?: return
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension) ?: "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(intent, "使用其他应用打开"))
        } catch (e: Exception) {
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(intent, "使用其他应用打开"))
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
    }
}
