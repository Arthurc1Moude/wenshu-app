package com.wenshu.app.util

import com.wenshu.app.R
import com.wenshu.app.data.model.FileType

object FileIconUtil {

    fun getIconRes(fileType: FileType, mimeType: String = "", filename: String = ""): Int {
        val ext = if (filename.contains('.')) {
            filename.substringAfterLast('.', "").lowercase()
        } else ""

        return when (fileType) {
            FileType.IMAGE -> R.drawable.ic_image
            FileType.VIDEO -> R.drawable.ic_play_small
            FileType.AUDIO -> R.drawable.ic_play_small
            FileType.PDF -> R.drawable.ic_file_pdf
            FileType.DOC -> when (ext) {
                "doc", "docx" -> R.drawable.ic_file_doc
                else -> R.drawable.ic_file_generic
            }
            FileType.EXCEL -> when (ext) {
                "xls", "xlsx" -> R.drawable.ic_file_xls
                else -> R.drawable.ic_file_generic
            }
            FileType.XLS -> R.drawable.ic_file_xls
            FileType.PPT -> when (ext) {
                "ppt", "pptx" -> R.drawable.ic_file_ppt
                else -> R.drawable.ic_file_generic
            }
            FileType.ARCHIVE -> R.drawable.ic_file_archive
            FileType.TEXT -> when (ext) {
                "txt" -> R.drawable.ic_file_txt
                "md" -> R.drawable.ic_file_md
                "json", "xml", "yaml", "yml" -> R.drawable.ic_file_code
                else -> R.drawable.ic_file_txt
            }
            FileType.MARKDOWN -> R.drawable.ic_file_md
            FileType.CODE -> R.drawable.ic_file_code
            FileType.UNKNOWN -> R.drawable.ic_file_unknown
        }
    }

    fun getFileTypeColor(fileType: FileType): Int {
        return when (fileType) {
            FileType.PDF -> 0xFFE53935.toInt()
            FileType.DOC -> 0xFF1E88E5.toInt()
            FileType.EXCEL -> 0xFF43A047.toInt()
            FileType.PPT -> 0xFFFB8C00.toInt()
            FileType.ARCHIVE -> 0xFF757575.toInt()
            FileType.IMAGE -> 0xFF8E24AA.toInt()
            FileType.VIDEO -> 0xFFE53935.toInt()
            FileType.AUDIO -> 0xFF00ACC1.toInt()
            FileType.TEXT -> 0xFF607D8B.toInt()
            FileType.UNKNOWN -> 0xFF9E9E9E.toInt()
        }
    }

    fun getFileExtensionLabel(mimeType: String, filename: String): String {
        val ext = if (filename.contains('.')) {
            filename.substringAfterLast('.', "").uppercase()
        } else ""
        return ext.ifBlank {
            when {
                mimeType.startsWith("image/") -> "IMG"
                mimeType.startsWith("video/") -> "VID"
                mimeType.startsWith("audio/") -> "AUD"
                mimeType == "application/pdf" -> "PDF"
                else -> "FILE"
            }
        }
    }
}
