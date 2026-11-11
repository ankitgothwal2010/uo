package com.ankit.familytools.engine.pdf

import android.graphics.Bitmap
import android.graphics.Color
import com.ankit.familytools.data.model.PageSignData
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.io.File

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Handles visible electronic signature placement and date/name stamps on PDF pages
 * with accurate coordinate mapping and page rotation transformation.
 */

object PdfSigner {

    /**
     * Calculates exact PDF point coordinates from normalized screen coordinates (0.0..1.0).
     * Accounts for PDF origin being at bottom-left vs screen origin at top-left,
     * as well as page rotation (0, 90, 180, 270).
     */
    data class PdfBounds(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val rotation: Float
    )

    fun mapToPdfCoordinates(
        pageWidth: Float,
        pageHeight: Float,
        pageRotation: Int,
        normX: Float,
        normY: Float,
        normWidth: Float,
        normHeight: Float
    ): PdfBounds {
        // Clamp normalized bounds
        val clampedNormX = normX.coerceIn(0f, 1f - normWidth.coerceAtMost(1f))
        val clampedNormY = normY.coerceIn(0f, 1f - normHeight.coerceAtMost(1f))
        val clampedNormW = normWidth.coerceIn(0.01f, 1f)
        val clampedNormH = normHeight.coerceIn(0.01f, 1f)

        return when ((pageRotation % 360 + 360) % 360) {
            90 -> {
                // Page is rotated 90 degrees clockwise
                val w = clampedNormH * pageWidth
                val h = clampedNormW * pageHeight
                val x = clampedNormY * pageWidth
                val y = clampedNormX * pageHeight
                PdfBounds(x, y, w, h, 90f)
            }
            180 -> {
                // Page is rotated 180 degrees
                val w = clampedNormW * pageWidth
                val h = clampedNormH * pageHeight
                val x = (1f - clampedNormX - clampedNormW) * pageWidth
                val y = clampedNormY * pageHeight
                PdfBounds(x, y, w, h, 180f)
            }
            270 -> {
                // Page is rotated 270 degrees
                val w = clampedNormH * pageWidth
                val h = clampedNormW * pageHeight
                val x = (1f - clampedNormY - clampedNormH) * pageWidth
                val y = (1f - clampedNormX - clampedNormW) * pageHeight
                PdfBounds(x, y, w, h, 270f)
            }
            else -> {
                // Standard 0 degrees: PDF origin is bottom-left, screen origin is top-left
                val w = clampedNormW * pageWidth
                val h = clampedNormH * pageHeight
                val x = clampedNormX * pageWidth
                val y = (1.0f - clampedNormY - clampedNormH) * pageHeight
                PdfBounds(x, y, w, h, 0f)
            }
        }
    }

    /**
     * Embeds one or more signatures and text stamps into the input PDF and writes to outputFile.
     */
    fun signPdf(
        inputFile: File,
        outputFile: File,
        signs: List<PageSignData>
    ): Boolean {
        var document: PDDocument? = null
        return try {
            document = PDDocument.load(inputFile)
            val pageCount = document.numberOfPages

            signs.forEach { signData ->
                if (signData.pageIndex in 0 until pageCount && signData.signatureBitmap != null) {
                    val page = document.getPage(signData.pageIndex)
                    val mediaBox = page.cropBox ?: page.mediaBox
                    val pageWidth = mediaBox.width
                    val pageHeight = mediaBox.height
                    val rotation = page.rotation

                    val bounds = mapToPdfCoordinates(
                        pageWidth = pageWidth,
                        pageHeight = pageHeight,
                        pageRotation = rotation,
                        normX = signData.normX,
                        normY = signData.normY,
                        normWidth = signData.normWidth,
                        normHeight = signData.normHeight
                    )

                    // Convert Bitmap to PDImageXObject (Lossless PNG preserves signature transparency)
                    val pdImage = LosslessFactory.createFromImage(document, signData.signatureBitmap)

                    PDPageContentStream(
                        document,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                    ).use { contentStream ->
                        // Draw signature image
                        contentStream.drawImage(
                            pdImage,
                            bounds.x,
                            bounds.y,
                            bounds.width,
                            bounds.height
                        )

                        // Draw optional Typed Name or Date below signature if requested
                        val textList = mutableListOf<String>()
                        if (!signData.typedName.isNullOrBlank()) {
                            textList.add("Signed by: ${signData.typedName}")
                        }
                        if (!signData.dateText.isNullOrBlank()) {
                            textList.add("Date: ${signData.dateText}")
                        }

                        if (textList.isNotEmpty()) {
                            val fontSize = (bounds.height * 0.12f).coerceIn(8f, 14f)
                            contentStream.beginText()
                            contentStream.setFont(PDType1Font.HELVETICA, fontSize)
                            contentStream.setNonStrokingColor(0, 0, 0)
                            
                            var textY = bounds.y - fontSize - 2f
                            for (line in textList) {
                                if (textY > 5f) {
                                    contentStream.newLineAtOffset(bounds.x, textY)
                                    contentStream.showText(line)
                                    textY -= (fontSize + 2f)
                                }
                            }
                            contentStream.endText()
                        }
                    }
                }
            }

            document.save(outputFile)
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            document?.close()
        }
    }
}
