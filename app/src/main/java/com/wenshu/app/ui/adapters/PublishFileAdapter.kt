package com.wenshu.app.ui.adapters

import android.webkit.MimeTypeMap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wenshu.app.R
import com.wenshu.app.data.model.FileAttachment
import com.wenshu.app.data.model.FileType
import com.wenshu.app.databinding.ItemPublishFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PendingFile(
    val path: String,
    val filename: String,
    val size: Long,
    val mimeType: String
) {
    val displaySize: String get() = formatSize(size)
    val expiresText: String
        get() {
            val sdf = SimpleDateFormat("MM月dd日", Locale.CHINESE)
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, 14)
            return "${sdf.format(cal.time)}过期"
        }
    val extension: String
        get() = File(filename).extension.ifEmpty {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
        }.uppercase(Locale.ROOT)

    val fileType: FileType
        get() = when {
            mimeType.startsWith("image/") -> FileType.IMAGE
            mimeType.startsWith("video/") -> FileType.VIDEO
            mimeType.startsWith("audio/") -> FileType.AUDIO
            mimeType == "application/pdf" -> FileType.PDF
            mimeType.contains("word") || extension in listOf("DOC", "DOCX") -> FileType.DOC
            mimeType.contains("excel") || mimeType.contains("spreadsheet") || extension in listOf("XLS", "XLSX") -> FileType.XLS
            mimeType.contains("powerpoint") || mimeType.contains("presentation") || extension in listOf("PPT", "PPTX") -> FileType.PPT
            mimeType.contains("zip") || mimeType.contains("rar") || mimeType.contains("7z") || mimeType.contains("tar") || mimeType.contains("gz") -> FileType.ARCHIVE
            extension in listOf("TXT", "MD", "RTF", "LOG") -> FileType.TEXT
            extension in listOf("JSON", "XML", "HTML", "CSS", "JS", "PY", "JAVA", "KT", "C", "CPP", "SWIFT") -> FileType.CODE
            else -> FileType.UNKNOWN
        }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${String.format(Locale.US, "%.1f", bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${String.format(Locale.US, "%.1f", bytes / (1024.0 * 1024))} MB"
            else -> "${String.format(Locale.US, "%.2f", bytes / (1024.0 * 1024 * 1024))} GB"
        }
    }
}

class PublishFileAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<PublishFileAdapter.FileViewHolder>() {

    private val files = mutableListOf<PendingFile>()

    inner class FileViewHolder(val binding: ItemPublishFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemPublishFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        with(holder.binding) {
            tvFileName.text = file.filename
            tvFileInfo.text = "${file.displaySize} · ${file.expiresText}"
            tvFileExt.text = file.extension

            val iconRes = when (file.fileType) {
                FileType.PDF -> R.drawable.ic_file_pdf
                FileType.DOC -> R.drawable.ic_file_doc
                FileType.XLS -> R.drawable.ic_file_xls
                FileType.PPT -> R.drawable.ic_file_ppt
                FileType.ARCHIVE -> R.drawable.ic_file_archive
                FileType.TEXT -> R.drawable.ic_file_txt
                FileType.MARKDOWN -> R.drawable.ic_file_md
                FileType.CODE -> R.drawable.ic_file_code
                FileType.VIDEO, FileType.AUDIO, FileType.IMAGE -> R.drawable.ic_file_generic
                else -> R.drawable.ic_file_unknown
            }
            imgFileIcon.setImageResource(iconRes)

            btnRemoveFile.setOnClickListener {
                onRemoveClick(holder.bindingAdapterPosition)
            }
        }
    }

    override fun getItemCount(): Int = files.size

    fun getFiles(): List<PendingFile> = files.toList()

    fun addFile(path: String, filename: String, size: Long, mimeType: String) {
        files.add(PendingFile(path, filename, size, mimeType))
        notifyItemInserted(files.size - 1)
    }

    fun removeFile(position: Int) {
        if (position in files.indices) {
            files.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount - position)
        }
    }

    fun setFiles(newFiles: List<PendingFile>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }

    fun clear() {
        val count = files.size
        files.clear()
        notifyItemRangeRemoved(0, count)
    }
}
