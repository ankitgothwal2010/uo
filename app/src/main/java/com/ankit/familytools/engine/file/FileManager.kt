package com.ankit.familytools.engine.file

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

object FileManager {
    private const val BUFFER_SIZE = 64 * 1024 // 64 KB buffer for efficient streaming without RAM bloat

    fun getFileName(context: Context, uri: Uri): String {
        var name: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (name.isNullOrBlank()) {
            name = uri.lastPathSegment?.substringAfterLast('/')
        }
        return name ?: "document_${System.currentTimeMillis()}"
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return 0L
    }

    /**
     * Copies a Content URI into a secure temporary file in app internal cache using streaming.
     */
    fun copyToTempFile(context: Context, uri: Uri, prefix: String = "input_"): File {
        val originalName = getFileName(context, uri)
        val ext = originalName.substringAfterLast('.', "")
        val suffix = if (ext.isNotEmpty()) ".$ext" else ""
        val tempFile = File(context.cacheDir, "${prefix}${System.currentTimeMillis()}$suffix")
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
        } ?: throw IOException("Could not open input stream for URI: $uri")
        
        return tempFile
    }

    /**
     * Creates a destination file for processed output inside the app's output cache directory.
     */
    fun createOutputFile(context: Context, baseName: String, extension: String): File {
        val outputDir = File(context.filesDir, "outputs").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanBase = baseName.substringBeforeLast('.').replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val ext = if (extension.startsWith(".")) extension else ".$extension"
        return File(outputDir, "${cleanBase}_$timeStamp$ext")
    }

    /**
     * Copies a generated output file to a user-selected SAF destination Uri.
     */
    fun saveToSafUri(context: Context, sourceFile: File, destinationUri: Uri) {
        context.contentResolver.openOutputStream(destinationUri)?.use { out ->
            FileInputStream(sourceFile).use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    out.write(buffer, 0, bytesRead)
                }
                out.flush()
            }
        } ?: throw IOException("Could not open output stream for destination URI: $destinationUri")
    }

    /**
     * Gets a sharable Uri for a file using FileProvider.
     */
    fun getShareableUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Formats bytes into clean human-readable size (e.g. 2.4 MB, 450 KB).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return DecimalFormat("#,##0.#").format(value) + " " + units[digitGroups]
    }

    /**
     * Calculates percentage saved between original and new size.
     */
    fun calculatePercentageSaved(originalBytes: Long, newBytes: Long): Int {
        if (originalBytes <= 0 || newBytes >= originalBytes) return 0
        val diff = originalBytes - newBytes
        return ((diff.toDouble() / originalBytes.toDouble()) * 100).toInt().coerceIn(0, 99)
    }

    /**
     * Verifies that the internal device storage has enough space for processing.
     */
    fun hasSufficientSpace(context: Context, requiredBytes: Long): Boolean {
        return try {
            val stat = StatFs(context.cacheDir.path)
            val available = stat.availableBlocksLong * stat.blockSizeLong
            available > (requiredBytes + 50 * 1024 * 1024) // 50MB safety buffer
        } catch (e: Exception) {
            true // If unknown, allow attempting operation
        }
    }

    /**
     * Cleans up temporary cache files older than 24 hours to prevent storage clutter on low-memory devices.
     */
    fun cleanOldCache(context: Context) {
        try {
            val threshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < threshold) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
