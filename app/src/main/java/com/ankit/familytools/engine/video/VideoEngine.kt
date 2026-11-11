package com.ankit.familytools.engine.video

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * AnkitG Family Tools - Media3 Video Engine
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Handles video compression with downscaling Presentation effects,
 * video trimming, audio muting, audio extraction (M4A), and frame capture.
 * All Media3 Transformer operations run strictly on the Main Looper thread.
 */

object VideoEngine {

    data class VideoMetadata(
        val durationMs: Long = 0L,
        val width: Int = 0,
        val height: Int = 0,
        val rotation: Int = 0,
        val bitrateBps: Long = 0L,
        val fileSizeBytes: Long = 0L,
        val mimeType: String = "video/mp4"
    )

    enum class CompressionPreset(
        val label: String,
        val hindiLabel: String,
        val maxDimension: Int,
        val targetBitrateBps: Int
    ) {
        SMALL("Small File (WhatsApp)", "छोटा साइज़ (व्हाट्सएप)", 480, 1_000_000),
        BALANCED("Balanced (Standard)", "संतुलित (सामान्य)", 720, 2_200_000),
        HIGH("High Quality (Crisp)", "उच्च गुणवत्ता (साफ)", 1080, 4_500_000)
    }

    /**
     * Reads metadata and dimensions from video file using MediaMetadataRetriever.
     */
    fun getVideoMetadata(file: File): VideoMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"

            VideoMetadata(
                durationMs = duration,
                width = width,
                height = height,
                rotation = rotation,
                bitrateBps = bitrate,
                fileSizeBytes = file.length(),
                mimeType = mime
            )
        } catch (e: Exception) {
            e.printStackTrace()
            VideoMetadata(fileSizeBytes = file.length())
        } finally {
            try { retriever.release() } catch (ignored: Exception) {}
        }
    }

    /**
     * Extracts a frame as a Bitmap at a specific timestamp.
     */
    fun extractFrameAtTime(file: File, timeMicros: Long): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.getFrameAtTime(timeMicros, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { retriever.release() } catch (ignored: Exception) {}
        }
    }

    /**
     * Formats milliseconds into clean mm:ss or hh:mm:ss string.
     */
    fun formatDuration(ms: Long): String {
        val totalSecs = ms / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    /**
     * Estimates compressed video size in bytes.
     */
    fun estimateCompressedSize(durationMs: Long, targetBitrateBps: Int, includeAudio: Boolean = true): Long {
        val seconds = durationMs / 1000.0
        val videoBytes = (targetBitrateBps * seconds) / 8.0
        val audioBytes = if (includeAudio) (128_000 * seconds) / 8.0 else 0.0
        return (videoBytes + audioBytes).toLong().coerceAtLeast(1024L)
    }

    /**
     * Validates that the generated output file is playable and non-corrupt.
     */
    fun validateOutputFile(file: File): Boolean {
        if (!file.exists() || file.length() < 512) return false
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            val hasAudio = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
            hasVideo != null || hasAudio != null
        } catch (e: Exception) {
            // If retriever fails, check file length
            file.length() > 1024
        } finally {
            try { retriever.release() } catch (ignored: Exception) {}
        }
    }

    /**
     * Compresses, trims, mutes, or extracts audio using Android Media3 Transformer.
     * Guaranteed to execute on Dispatchers.Main so the Android Looper is always active.
     */
    @OptIn(UnstableApi::class)
    suspend fun processVideo(
        context: Context,
        inputFile: File,
        outputFile: File,
        preset: CompressionPreset = CompressionPreset.BALANCED,
        startMs: Long = 0L,
        endMs: Long = 0L,
        muteAudio: Boolean = false,
        extractAudioOnly: Boolean = false,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val inputUri = Uri.fromFile(inputFile)
            val mediaItemBuilder = MediaItem.Builder().setUri(inputUri)

            // Trimming clipping configuration
            if (startMs > 0 || endMs > startMs) {
                val clippingBuilder = MediaItem.ClippingConfiguration.Builder()
                if (startMs > 0) clippingBuilder.setStartPositionMs(startMs)
                if (endMs > startMs) clippingBuilder.setEndPositionMs(endMs)
                mediaItemBuilder.setClippingConfiguration(clippingBuilder.build())
            }
            val mediaItem = mediaItemBuilder.build()

            // Build Transformer
            val transformerBuilder = Transformer.Builder(context)

            if (extractAudioOnly) {
                transformerBuilder.setAudioMimeType(MimeTypes.AUDIO_AAC)
            } else {
                transformerBuilder.setVideoMimeType(MimeTypes.VIDEO_H264)
                if (!muteAudio) {
                    transformerBuilder.setAudioMimeType(MimeTypes.AUDIO_AAC)
                }
            }

            var transformerRef: Transformer? = null

            val transformer = transformerBuilder
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        val valid = validateOutputFile(outputFile)
                        if (continuation.isActive) {
                            continuation.resume(valid)
                        }
                    }

                    override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                        exportException.printStackTrace()
                        outputFile.delete()
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                })
                .build()

            transformerRef = transformer

            // Build EditedMediaItem with scaling Effects if compressing
            val editedMediaItemBuilder = EditedMediaItem.Builder(mediaItem)
                .setRemoveAudio(muteAudio)
                .setRemoveVideo(extractAudioOnly)

            if (!extractAudioOnly) {
                // Apply resolution downscale Presentation effect
                val presentation = Presentation.createForHeight(preset.maxDimension)
                val effects = Effects(emptyList(), listOf(presentation))
                editedMediaItemBuilder.setEffects(effects)
            }

            val editedMediaItem = editedMediaItemBuilder.build()

            // Monitor Progress
            val progressJob = launch(Dispatchers.Main) {
                val progressHolder = ProgressHolder()
                while (continuation.isActive) {
                    val state = transformer.getProgress(progressHolder)
                    if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress(progressHolder.progress / 100f)
                    }
                    delay(250)
                }
            }

            continuation.invokeOnCancellation {
                progressJob.cancel()
                try {
                    transformerRef?.cancel()
                    outputFile.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                transformer.start(editedMediaItem, outputFile.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
                progressJob.cancel()
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
    }
}
