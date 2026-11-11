package com.ankit.familytools.data.model

import android.graphics.Bitmap

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

data class ProcessingHistoryItem(
    val id: String = System.currentTimeMillis().toString(),
    val toolName: String,
    val inputFileName: String,
    val outputFileName: String,
    val outputFilePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L,
    val mimeType: String = "*/*"
)

data class SignatureItem(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val imagePath: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class PageSignData(
    val pageIndex: Int,
    val signatureBitmap: Bitmap? = null,
    // Coordinates normalized 0.0f..1.0f relative to the page width and height
    val normX: Float = 0.5f,
    val normY: Float = 0.5f,
    val normWidth: Float = 0.35f,
    val normHeight: Float = 0.15f,
    val rotationDegrees: Float = 0f,
    val dateText: String? = null,
    val typedName: String? = null
)
