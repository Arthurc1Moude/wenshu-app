package com.wenshu.app.ui.file

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.model.FileAttachment
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.util.FileIconUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileViewerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var imgFileIcon: ImageView
    private lateinit var tvFileExt: TextView
    private lateinit var tvFilename: TextView
    private lateinit var tvFileInfo: TextView
    private lateinit var tvExpireInfo: TextView
    private lateinit var progressDownload: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var btnSave: Button
    private lateinit var btnOpenWith: Button

    private var fileAttachment: FileAttachment? = null
    private var downloadedFile: File? = null
    private var downloadJob: Job? = null
    private val repository = PostRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_viewer)

        initViews()
        loadFileInfo()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        imgFileIcon = findViewById(R.id.img_file_icon)
        tvFileExt = findViewById(R.id.tv_file_ext)
        tvFilename = findViewById(R.id.tv_filename)
        tvFileInfo = findViewById(R.id.tv_file_info)
        tvExpireInfo = findViewById(R.id.tv_expire_info)
        progressDownload = findViewById(R.id.progress_download)
        tvProgress = findViewById(R.id.tv_progress)
        btnSave = findViewById(R.id.btn_save)
        btnOpenWith = findViewById(R.id.btn_open_with)

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { startDownload() }
        btnOpenWith.setOnClickListener { openWithOtherApp() }
    }

    private fun loadFileInfo() {
        val fileId = intent.getStringExtra("fileId")
        val fileUrl = intent.getStringExtra("fileUrl")
        val filename = intent.getStringExtra("filename")
        val fileSize = intent.getLongExtra("fileSize", 0)
        val mimeType = intent.getStringExtra("mimeType") ?: "application/octet-stream"
        val expiresAt = intent.getLongExtra("expiresAt", -1)
        val isPermanent = intent.getBooleanExtra("isPermanent", false)

        if (fileId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = repository.getFileInfo(fileId)
                withContext(Dispatchers.Main) {
                    result.onSuccess { file ->
                        fileAttachment = file
                        updateUI(file)
                    }.onFailure {
                        Toast.makeText(this@FileViewerActivity, "文件信息加载失败", Toast.LENGTH_SHORT).show()
                        if (filename != null) {
                            val tempFile = FileAttachment(
                                id = fileId,
                                filename = filename,
                                size = fileSize,
                                mimeType = mimeType,
                                url = fileUrl ?: "",
                                expiresAt = if (expiresAt > 0) expiresAt else null,
                                isPermanent = isPermanent
                            )
                            updateUI(tempFile)
                        }
                    }
                }
            }
        } else if (filename != null) {
            val file = FileAttachment(
                id = "",
                filename = filename,
                size = fileSize,
                mimeType = mimeType,
                url = fileUrl ?: "",
                expiresAt = if (expiresAt > 0) expiresAt else null,
                isPermanent = isPermanent
            )
            updateUI(file)
        }
    }

    private fun updateUI(file: FileAttachment) {
        fileAttachment = file
        tvFilename.text = file.filename
        tvFileInfo.text = "${file.displaySize}"
        tvExpireInfo.text = file.expireText

        val iconRes = FileIconUtil.getIconRes(file.fileTypeCategory, file.mimeType, file.filename)
        imgFileIcon.setImageResource(iconRes)

        val ext = file.extension
        if (ext.isNotBlank()) {
            tvFileExt.text = ext
            tvFileExt.visibility = View.VISIBLE
        } else {
            tvFileExt.visibility = View.GONE
        }
    }

    private fun startDownload() {
        val file = fileAttachment ?: return
        val downloadUrl = file.url.ifBlank {
            "${RetrofitClient.getBaseUrl()}/api/files/${file.id}/download"
        }

        btnSave.isEnabled = false
        btnSave.text = "下载中..."
        progressDownload.visibility = View.VISIBLE
        tvProgress.visibility = View.VISIBLE
        progressDownload.progress = 0

        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.downloadFile(file.id)
                val body = response
                saveFile(body, file)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FileViewerActivity, "下载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetDownloadUI()
                }
            }
        }
    }

    private suspend fun saveFile(body: ResponseBody, file: FileAttachment) {
        try {
            val contentLength = body.contentLength()
            val inputStream: InputStream = body.byteStream()
            val fileName = file.filename

            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            } ?: filesDir

            val outputFile = File(downloadsDir, fileName)
            if (outputFile.exists()) {
                outputFile.delete()
            }

            var totalBytesRead: Long = 0
            var lastUpdate = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val outputStream = FileOutputStream(outputFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        totalBytesRead += read

                        val now = System.currentTimeMillis()
                        if (contentLength > 0 && now - lastUpdate > 100) {
                            lastUpdate = now
                            val progress = ((totalBytesRead * 100) / contentLength).toInt()
                            withContext(Dispatchers.Main) {
                                progressDownload.progress = progress
                                tvProgress.text = "${FileAttachment.formatSize(totalBytesRead)} / ${file.displaySize}"
                            }
                        }
                    }
                    output.flush()
                }
            }

            downloadedFile = outputFile

            withContext(Dispatchers.Main) {
                progressDownload.progress = 100
                tvProgress.text = "下载完成"
                btnSave.visibility = View.GONE
                btnOpenWith.visibility = View.VISIBLE
                Toast.makeText(this@FileViewerActivity, "下载完成", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FileViewerActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                resetDownloadUI()
            }
        }
    }

    private fun resetDownloadUI() {
        btnSave.isEnabled = true
        btnSave.text = "保存"
        progressDownload.visibility = View.GONE
        tvProgress.visibility = View.GONE
    }

    private fun openWithOtherApp() {
        val file = downloadedFile ?: return
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }

            val mimeType = contentResolver.getType(uri) ?: fileAttachment?.mimeType ?: "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "使用其他应用打开")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开文件: ${e.message}", Toast.LENGTH_SHORT).show()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = fileAttachment?.mimeType ?: "*/*"
                putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this@FileViewerActivity, "${packageName}.fileprovider", file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "分享文件"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}
