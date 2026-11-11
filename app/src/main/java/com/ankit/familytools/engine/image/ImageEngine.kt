package com.ankit.familytools.engine.image

import android.graphics.*
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Comprehensive native Image Processing Engine for family photo tasks.
 */

object ImageEngine {

    /**
     * Decodes bitmap with EXIF orientation correction.
     */
    fun decodeWithExif(file: File, maxDimension: Int = 2048): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

        return try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return rawBitmap
            }
            val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            if (rotated != rawBitmap) rawBitmap.recycle()
            rotated
        } catch (e: Exception) {
            rawBitmap
        }
    }

    /**
     * Crops image using normalized coordinates (0.0..1.0).
     */
    fun cropImage(
        inputFile: File,
        outputFile: File,
        normLeft: Float,
        normTop: Float,
        normRight: Float,
        normBottom: Float,
        quality: Int = 90
    ): Boolean {
        val bitmap = decodeWithExif(inputFile, maxDimension = 4096) ?: return false
        return try {
            val x = (normLeft.coerceIn(0f, 1f) * bitmap.width).toInt()
            val y = (normTop.coerceIn(0f, 1f) * bitmap.height).toInt()
            val w = ((normRight - normLeft).coerceIn(0.01f, 1f) * bitmap.width).toInt().coerceAtMost(bitmap.width - x)
            val h = ((normBottom - normTop).coerceIn(0.01f, 1f) * bitmap.height).toInt().coerceAtMost(bitmap.height - y)

            val cropped = Bitmap.createBitmap(bitmap, x, y, w, h)
            saveBitmap(cropped, outputFile, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Rotates (90, 180, 270) and flips image horizontally/vertically.
     */
    fun rotateAndFlip(
        inputFile: File,
        outputFile: File,
        rotationDegrees: Float,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false,
        quality: Int = 90
    ): Boolean {
        val bitmap = decodeWithExif(inputFile, maxDimension = 4096) ?: return false
        return try {
            val matrix = Matrix()
            if (rotationDegrees != 0f) matrix.postRotate(rotationDegrees)
            val sx = if (flipHorizontal) -1f else 1f
            val sy = if (flipVertical) -1f else 1f
            if (flipHorizontal || flipVertical) matrix.postScale(sx, sy)

            val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            saveBitmap(transformed, outputFile, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Resizes image to target width and height in pixels or percentage.
     */
    fun resizeImage(
        inputFile: File,
        outputFile: File,
        targetWidth: Int,
        targetHeight: Int,
        quality: Int = 90
    ): Boolean {
        val bitmap = decodeWithExif(inputFile, maxDimension = 4096) ?: return false
        return try {
            val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth.coerceAtLeast(1), targetHeight.coerceAtLeast(1), true)
            saveBitmap(scaled, outputFile, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Compresses image with selectable quality (1-100) and format.
     */
    fun compressImage(
        inputFile: File,
        outputFile: File,
        quality: Int,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Boolean {
        val bitmap = decodeWithExif(inputFile, maxDimension = 4096) ?: return false
        return try {
            saveBitmap(bitmap, outputFile, quality, format)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Removes all EXIF metadata (GPS, camera info, date/time) by re-encoding pixel data into a fresh container.
     */
    fun removeExif(inputFile: File, outputFile: File, quality: Int = 95): Boolean {
        val bitmap = decodeWithExif(inputFile, maxDimension = 4096) ?: return false
        return try {
            // Saving raw pixels to new file completely strips old EXIF tags
            saveBitmap(bitmap, outputFile, quality)
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Adjusts brightness, contrast, and saturation using Android ColorMatrix.
     * brightness: -100f..100f (0 = normal)
     * contrast: 0.5f..2.0f (1 = normal)
     * saturation: 0.0f..2.0f (1 = normal)
     */
    fun adjustColors(
        inputFile: File,
        outputFile: File,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        quality: Int = 90
    ): Boolean {
        val bitmap = decodeWithExif(inputFile, maxDimension = 2048) ?: return false
        return try {
            val adjusted = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
            val canvas = Canvas(adjusted)
            val paint = Paint()

            val cm = ColorMatrix()
            // 1. Saturation
            cm.setSaturation(saturation.coerceIn(0f, 2f))

            // 2. Contrast & Brightness
            val scale = contrast.coerceIn(0.1f, 3f)
            val translate = brightness.coerceIn(-100f, 100f)
            val contrastMatrix = ColorMatrix(floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(contrastMatrix)

            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            saveBitmap(adjusted, outputFile, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Stamps a signature onto an image at normalized coordinates (0.0..1.0).
     */
    fun addSignatureStamp(
        inputFile: File,
        outputFile: File,
        signatureBitmap: Bitmap,
        normX: Float,
        normY: Float,
        normWidth: Float,
        normHeight: Float,
        quality: Int = 92
    ): Boolean {
        val baseBitmap = decodeWithExif(inputFile, maxDimension = 4096) ?: return false
        return try {
            val result = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawBitmap(baseBitmap, 0f, 0f, null)

            val destX = normX * baseBitmap.width
            val destY = normY * baseBitmap.height
            val destW = normWidth * baseBitmap.width
            val destH = normHeight * baseBitmap.height

            val destRect = RectF(destX, destY, destX + destW, destY + destH)
            canvas.drawBitmap(signatureBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))

            saveBitmap(result, outputFile, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            baseBitmap.recycle()
        }
    }

    /**
     * Creates a photo collage grid from 2 to 6 images.
     */
    fun createCollage(
        imageFiles: List<File>,
        outputFile: File,
        columns: Int = 2,
        borderPx: Int = 12,
        borderColor: Int = Color.WHITE,
        quality: Int = 90
    ): Boolean {
        if (imageFiles.isEmpty()) return false
        val cellDimension = 800
        val rows = (imageFiles.size + columns - 1) / columns
        val totalWidth = (columns * cellDimension) + ((columns + 1) * borderPx)
        val totalHeight = (rows * cellDimension) + ((rows + 1) * borderPx)

        val collageBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collageBitmap)
        canvas.drawColor(borderColor)

        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        imageFiles.forEachIndexed { index, file ->
            val col = index % columns
            val row = index / columns
            val left = borderPx + col * (cellDimension + borderPx)
            val top = borderPx + row * (cellDimension + borderPx)

            val itemBitmap = decodeWithExif(file, maxDimension = cellDimension)
            if (itemBitmap != null) {
                // Center-crop into cell
                val srcRect = calculateCenterCropRect(itemBitmap.width, itemBitmap.height)
                val destRect = Rect(left, top, left + cellDimension, top + cellDimension)
                canvas.drawBitmap(itemBitmap, srcRect, destRect, paint)
                itemBitmap.recycle()
            }
        }

        return saveBitmap(collageBitmap, outputFile, quality)
    }

    private fun calculateCenterCropRect(w: Int, h: Int): Rect {
        val minDim = minOf(w, h)
        val left = (w - minDim) / 2
        val top = (h - minDim) / 2
        return Rect(left, top, left + minDim, top + minDim)
    }

    fun saveBitmap(
        bitmap: Bitmap,
        outputFile: File,
        quality: Int,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): Boolean {
        return try {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(format, quality.coerceIn(1, 100), out)
                out.flush()
            }
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Estimates output size based on quality factor and resolution.
     */
    fun estimateCompressedSize(originalBytes: Long, quality: Int): Long {
        val factor = (quality.toFloat() / 100f) * 0.7f + 0.15f
        return (originalBytes * factor).toLong().coerceAtLeast(1024L)
    }
}
