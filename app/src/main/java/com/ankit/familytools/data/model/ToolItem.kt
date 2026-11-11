package com.ankit.familytools.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

enum class ToolCategory(val title: String, val hindiTitle: String) {
    ALL("All Tools", "सभी टूल्स"),
    PDF("PDF Tools", "पीडीएफ टूल्स"),
    IMAGE("Image Tools", "फोटो टूल्स"),
    VIDEO("Video Tools", "वीडियो टूल्स"),
    FAVORITES("Favorites", "पसंदीदा")
}

data class ToolItem(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val description: String,
    val hindiDescription: String,
    val category: ToolCategory,
    val iconName: String,
    val route: String,
    val isPrimaryShortcut: Boolean = false
) {
    fun getVectorIcon(): ImageVector {
        return when (iconName) {
            "Draw" -> Icons.Default.Draw
            "VideoCall" -> Icons.Default.VideoCall
            "PictureAsPdf" -> Icons.Default.PictureAsPdf
            "Crop" -> Icons.Default.Crop
            "AddPhotoAlternate" -> Icons.Default.AddPhotoAlternate
            "DocumentScanner" -> Icons.Default.DocumentScanner
            "CallMerge" -> Icons.Default.CallMerge
            "CallSplit" -> Icons.Default.CallSplit
            "RotateRight" -> Icons.Default.RotateRight
            "Compress" -> Icons.Default.Compress
            "TextFields" -> Icons.Default.TextFields
            "Lock" -> Icons.Default.Lock
            "LockOpen" -> Icons.Default.LockOpen
            "WaterDrop" -> Icons.Default.WaterDrop
            "FormatListNumbered" -> Icons.Default.FormatListNumbered
            "PhotoLibrary" -> Icons.Default.PhotoLibrary
            "Tune" -> Icons.Default.Tune
            "ContentCut" -> Icons.Default.ContentCut
            "VolumeOff" -> Icons.Default.VolumeOff
            "Audiotrack" -> Icons.Default.Audiotrack
            "Transform" -> Icons.Default.Transform
            else -> Icons.Default.Build
        }
    }
}
