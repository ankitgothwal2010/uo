package com.ankit.familytools.engine.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Lightweight PDF Renderer using Android's native PdfRenderer to efficiently
 * generate page previews and thumbnails without loading heavy structures into RAM.
 */

object PdfRendererHelper {

    data class PageInfo(
        val pageIndex: Int,
        val width: Int,
        val height: Int
    )

    fun getPageCount(file: File): Int {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            renderer.pageCount
        } catch (e: Exception) {
            0
        } finally {
            renderer?.close()
            pfd?.close()
        }
    }

    fun getPageInfoList(file: File): List<PageInfo> {
        val list = mutableListOf<PageInfo>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                list.add(PageInfo(i, page.width, page.height))
                page.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            renderer?.close()
            pfd?.close()
        }
        return list
    }

    /**
     * Renders a specific PDF page to a Bitmap at a max bounded dimension to prevent memory pressure.
     */
    fun renderPageToBitmap(file: File, pageIndex: Int, maxDimension: Int = 1200): Bitmap? {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return null
            
            page = renderer.openPage(pageIndex)
            val w = page.width
            val h = page.height
            
            val scale = if (w > h) {
                if (w > maxDimension) maxDimension.toFloat() / w else 1.0f
            } else {
                if (h > maxDimension) maxDimension.toFloat() / h else 1.0f
            }
            
            val targetW = (w * scale).toInt().coerceAtLeast(1)
            val targetH = (h * scale).toInt().coerceAtLeast(1)
            
            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            // Draw white background because PDF pages are transparent by default in PdfRenderer
            bitmap.eraseColor(Color.WHITE)
            
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            page?.close()
            renderer?.close()
            pfd?.close()
        }
    }
}
