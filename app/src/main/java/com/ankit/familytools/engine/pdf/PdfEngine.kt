package com.ankit.familytools.engine.pdf

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.util.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Comprehensive native PDF processing engine using Apache PDFBox-Android.
 */

object PdfEngine {

    /**
     * Merge multiple PDF files into one output file.
     */
    fun mergePdfs(inputFiles: List<File>, outputFile: File): Boolean {
        if (inputFiles.isEmpty()) return false
        val merger = PDFMergerUtility()
        merger.destinationFileName = outputFile.absolutePath
        inputFiles.forEach { file ->
            if (file.exists() && file.length() > 0) {
                merger.addSource(file)
            }
        }
        return try {
            merger.mergeDocuments(null)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Split PDF by extracting specified 0-based page indices into a new document.
     */
    fun extractPages(inputFile: File, pageIndices: List<Int>, outputFile: File): Boolean {
        var sourceDoc: PDDocument? = null
        var destDoc: PDDocument? = null
        return try {
            sourceDoc = PDDocument.load(inputFile)
            destDoc = PDDocument()
            val total = sourceDoc.numberOfPages
            pageIndices.filter { it in 0 until total }.forEach { index ->
                destDoc.addPage(sourceDoc.getPage(index))
            }
            if (destDoc.numberOfPages == 0) return false
            destDoc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            destDoc?.close()
            sourceDoc?.close()
        }
    }

    /**
     * Delete specified 0-based page indices from PDF.
     */
    fun deletePages(inputFile: File, pagesToDelete: Set<Int>, outputFile: File): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val total = doc.numberOfPages
            pagesToDelete.filter { it in 0 until total }
                .sortedDescending()
                .forEach { index ->
                    doc.removePage(index)
                }
            if (doc.numberOfPages == 0) return false
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Rotate selected or all pages by given degrees (90, 180, 270).
     */
    fun rotatePages(inputFile: File, pageRotations: Map<Int, Int>, outputFile: File): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val total = doc.numberOfPages
            pageRotations.forEach { (index, angle) ->
                if (index in 0 until total) {
                    val page = doc.getPage(index)
                    val currentRotation = page.rotation
                    page.rotation = (currentRotation + angle) % 360
                }
            }
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Reorders pages based on a list of 0-based indices.
     */
    fun reorderPages(inputFile: File, newOrder: List<Int>, outputFile: File): Boolean {
        var sourceDoc: PDDocument? = null
        var destDoc: PDDocument? = null
        return try {
            sourceDoc = PDDocument.load(inputFile)
            destDoc = PDDocument()
            val total = sourceDoc.numberOfPages
            newOrder.filter { it in 0 until total }.forEach { index ->
                destDoc.addPage(sourceDoc.getPage(index))
            }
            if (destDoc.numberOfPages == 0) return false
            destDoc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            destDoc?.close()
            sourceDoc?.close()
        }
    }

    /**
     * Converts a list of image files into a multi-page PDF document.
     */
    fun imagesToPdf(imageFiles: List<File>, outputFile: File): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument()
            imageFiles.forEach { imgFile ->
                if (imgFile.exists() && imgFile.length() > 0) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    if (bitmap != null) {
                        val a4Width = PDRectangle.A4.width
                        val a4Height = PDRectangle.A4.height

                        val imgW = bitmap.width.toFloat()
                        val imgH = bitmap.height.toFloat()
                        val scale = (a4Width / imgW).coerceAtMost(a4Height / imgH)

                        val drawW = imgW * scale
                        val drawH = imgH * scale
                        val x = (a4Width - drawW) / 2f
                        val y = (a4Height - drawH) / 2f

                        val page = PDPage(PDRectangle.A4)
                        doc.addPage(page)

                        val pdImage = JPEGFactory.createFromImage(doc, bitmap, 0.85f)
                        PDPageContentStream(doc, page).use { cs ->
                            cs.drawImage(pdImage, x, y, drawW, drawH)
                        }
                        bitmap.recycle()
                    }
                }
            }
            if (doc.numberOfPages == 0) return false
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Converts plain text into a paginated PDF document.
     */
    fun textToPdf(textContent: String, outputFile: File, title: String = "Document"): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument()
            val fontSize = 12f
            val margin = 50f
            val pageHeight = PDRectangle.A4.height
            val pageWidth = PDRectangle.A4.width
            val usableWidth = pageWidth - (2 * margin)
            val lineHeight = fontSize * 1.5f

            val lines = mutableListOf<String>()
            textContent.split("\n").forEach { paragraph ->
                val words = paragraph.split(" ")
                var currentLine = ""
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val testWidth = PDType1Font.HELVETICA.getStringWidth(testLine) / 1000f * fontSize
                    if (testWidth > usableWidth) {
                        if (currentLine.isNotEmpty()) lines.add(currentLine)
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) lines.add(currentLine)
            }

            var currentPage: PDPage? = null
            var contentStream: PDPageContentStream? = null
            var currentY = pageHeight - margin

            for (line in lines) {
                if (currentPage == null || currentY < margin + lineHeight) {
                    contentStream?.endText()
                    contentStream?.close()

                    currentPage = PDPage(PDRectangle.A4)
                    doc.addPage(currentPage)
                    currentY = pageHeight - margin

                    contentStream = PDPageContentStream(doc, currentPage)
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, fontSize)
                    contentStream.setLeading(lineHeight)
                    contentStream.newLineAtOffset(margin, currentY)
                }

                contentStream?.showText(line)
                contentStream?.newLine()
                currentY -= lineHeight
            }

            contentStream?.endText()
            contentStream?.close()

            if (doc.numberOfPages == 0) {
                doc.addPage(PDPage(PDRectangle.A4))
            }

            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Extracts selectable text from a PDF document.
     */
    fun extractText(inputFile: File): String {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            stripper.getText(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            "Error extracting text: ${e.message}"
        } finally {
            doc?.close()
        }
    }

    /**
     * Extracts metadata from PDF.
     */
    fun getMetadata(inputFile: File): Map<String, String> {
        val map = mutableMapOf<String, String>()
        var doc: PDDocument? = null
        try {
            doc = PDDocument.load(inputFile)
            map["Pages"] = doc.numberOfPages.toString()
            map["Encrypted"] = if (doc.isEncrypted) "Yes" else "No"
            val info = doc.documentInformation
            if (info != null) {
                info.title?.let { map["Title"] = it }
                info.author?.let { map["Author"] = it }
                info.subject?.let { map["Subject"] = it }
                info.creator?.let { map["Creator"] = it }
                info.creationDate?.let {
                    map["Created"] = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it.time)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            map["Error"] = e.message ?: "Could not read metadata"
        } finally {
            doc?.close()
        }
        return map
    }

    /**
     * Adds page numbers (e.g. Page X of Y) to the bottom center of each page.
     */
    fun addPageNumbers(inputFile: File, outputFile: File, formatStr: String = "Page %d of %d"): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val totalPages = doc.numberOfPages
            for (i in 0 until totalPages) {
                val page = doc.getPage(i)
                val text = String.format(Locale.getDefault(), formatStr, i + 1, totalPages)
                val fontSize = 10f
                val textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 1000f * fontSize
                val x = (page.mediaBox.width - textWidth) / 2f
                val y = 25f

                PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA, fontSize)
                    cs.setNonStrokingColor(80, 80, 80)
                    cs.newLineAtOffset(x, y)
                    cs.showText(text)
                    cs.endText()
                }
            }
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Adds diagonal semi-transparent watermark text across pages.
     */
    fun addWatermark(inputFile: File, outputFile: File, watermarkText: String, opacity: Float = 0.25f): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val total = doc.numberOfPages
            val fontSize = 42f

            val gState = PDExtendedGraphicsState().apply {
                nonStrokingAlphaConstant = opacity
            }

            for (i in 0 until total) {
                val page = doc.getPage(i)
                val mediaBox = page.cropBox ?: page.mediaBox
                val textWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(watermarkText) / 1000f * fontSize

                PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                    cs.setGraphicsStateParameters(gState)
                    cs.setNonStrokingColor(180, 0, 0)
                    cs.beginText()
                    cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize)
                    val x = (mediaBox.width - textWidth) / 2f
                    val y = mediaBox.height / 2f
                    cs.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(45.0), x, y))
                    cs.showText(watermarkText)
                    cs.endText()
                }
            }
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Encrypts and protects a PDF with user and owner passwords using standard 128-bit AES encryption.
     */
    fun protectPdf(inputFile: File, outputFile: File, userPass: String, ownerPass: String): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val ap = AccessPermission().apply {
                setCanPrint(true)
                setCanExtractContent(false)
                setCanModify(false)
            }
            val spp = StandardProtectionPolicy(ownerPass, userPass, ap).apply {
                encryptionKeyLength = 128
            }
            doc.protect(spp)
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Unlocks a password-protected PDF by providing the correct password and saving an unencrypted copy.
     */
    fun unlockPdf(inputFile: File, outputFile: File, password: String): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile, password)
            if (doc.isEncrypted) {
                doc.isAllSecurityToBeRemoved = true
            }
            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }

    /**
     * Compresses PDF by downsampling embedded images and re-encoding with JPEG compression.
     */
    fun compressPdf(inputFile: File, outputFile: File, level: Int = 2): Boolean {
        var doc: PDDocument? = null
        return try {
            doc = PDDocument.load(inputFile)
            val quality = when (level) {
                1 -> 0.70f
                2 -> 0.45f
                else -> 0.30f
            }
            val maxDim = when (level) {
                1 -> 1500
                2 -> 1000
                else -> 750
            }

            for (page in doc.pages) {
                val resources = page.resources ?: continue
                val xobjectNames = resources.xObjectNames.toList()
                for (name in xobjectNames) {
                    val xobject = resources.getXObject(name)
                    if (xobject is PDImageXObject) {
                        try {
                            val bmp = xobject.image ?: continue
                            val origW = bmp.width
                            val origH = bmp.height
                            if (origW > maxDim || origH > maxDim) {
                                val scale = minOf(maxDim.toFloat() / origW, maxDim.toFloat() / origH).coerceAtMost(1f)
                                val targetW = (origW * scale).toInt().coerceAtLeast(1)
                                val targetH = (origH * scale).toInt().coerceAtLeast(1)
                                val scaledBmp = Bitmap.createScaledBitmap(bmp, targetW, targetH, true)

                                val compressedImg = JPEGFactory.createFromImage(doc, scaledBmp, quality)
                                resources.put(name, compressedImg)
                                if (scaledBmp != bmp) scaledBmp.recycle()
                            }
                        } catch (ignored: Exception) {}
                    }
                }
            }

            doc.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            doc?.close()
        }
    }
}
