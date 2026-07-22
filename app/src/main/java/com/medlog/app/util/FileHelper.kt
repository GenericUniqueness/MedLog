package com.medlog.app.util

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object FileHelper {

    /**
     * Creates a file in context.filesDir/attachments/ with a UUID prefix to avoid
     * collisions. The directory is created if it does not exist.
     *
     * @return the newly created [File]
     */
    fun createAttachmentFile(context: Context, fileName: String): File {
        val dir = File(context.filesDir, "attachments")
        if (!dir.exists()) dir.mkdirs()
        val safeName = "${UUID.randomUUID()}_$fileName"
        return File(dir, safeName)
    }

    /**
     * Creates a file in context.filesDir/exports/. The directory is created if it
     * does not exist.
     *
     * @return the newly created [File]
     */
    fun createExportFile(context: Context, fileName: String): File {
        val dir = File(context.filesDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, fileName)
    }

    /**
     * Deletes the file at the given path.
     *
     * @return `true` if the file was deleted, `false` otherwise
     */
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) file.delete() else false
    }

    /**
     * Simple file-extension → MIME-type mapping.
     * Returns "application/octet-stream" for unknown extensions.
     */
    fun getMimeType(fileName: String): String {
        val lower = fileName.lowercase()
        val dot = lower.lastIndexOf('.')
        if (dot < 0) return "application/octet-stream"
        return when (lower.substring(dot + 1)) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "xml" -> "text/xml"
            "html", "htm" -> "text/html"
            "json" -> "application/json"
            "zip" -> "application/zip"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            "wav" -> "audio/wav"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            else -> "application/octet-stream"
        }
    }

    /**
     * Returns a content:// URI for [file] via FileProvider, suitable for sharing
     * with other apps via Intent.ACTION_SEND.
     */
    fun getShareUri(context: Context, file: File): android.net.Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}