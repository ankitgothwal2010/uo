package com.ankit.familytools

import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.engine.video.VideoEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * AnkitG Family Tools - Video & File Manager Unit Tests
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */
class VideoAndFileProcessingTest {

    @Test
    fun testDurationFormatting() {
        assertEquals("00:05", VideoEngine.formatDuration(5000L))
        assertEquals("02:30", VideoEngine.formatDuration(150_000L))
        assertEquals("1:00:00", VideoEngine.formatDuration(3600_000L))
        assertEquals("1:15:30", VideoEngine.formatDuration(4530_000L))
    }

    @Test
    fun testCompressedVideoSizeEstimation() {
        val durationMs = 60_000L // 1 minute
        val smallEst = VideoEngine.estimateCompressedSize(
            durationMs,
            VideoEngine.CompressionPreset.SMALL.targetBitrateBps
        )
        val balancedEst = VideoEngine.estimateCompressedSize(
            durationMs,
            VideoEngine.CompressionPreset.BALANCED.targetBitrateBps
        )
        val highEst = VideoEngine.estimateCompressedSize(
            durationMs,
            VideoEngine.CompressionPreset.HIGH.targetBitrateBps
        )

        assertTrue("Small preset must be smaller than Balanced", smallEst < balancedEst)
        assertTrue("Balanced preset must be smaller than High Quality", balancedEst < highEst)
        // 1 min of 1 Mbps video is ~7.5 MB + 1MB audio = ~8.5 MB = ~8.5 * 1024 * 1024 bytes
        assertTrue("Small file size should be within reasonable bounds (~6-12 MB)", smallEst in 6_000_000..12_000_000)
    }

    @Test
    fun testFileSizeFormatting() {
        assertEquals("0 B", FileManager.formatFileSize(0L))
        assertEquals("500 B", FileManager.formatFileSize(500L))
        assertEquals("1.5 KB", FileManager.formatFileSize(1536L))
        assertEquals("10 MB", FileManager.formatFileSize(10 * 1024 * 1024L))
        assertEquals("2.5 GB", FileManager.formatFileSize((2.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testCalculatePercentageSaved() {
        assertEquals(0, FileManager.calculatePercentageSaved(100L, 100L))
        assertEquals(0, FileManager.calculatePercentageSaved(100L, 150L))
        assertEquals(50, FileManager.calculatePercentageSaved(100L, 50L))
        assertEquals(75, FileManager.calculatePercentageSaved(200L, 50L))
        assertEquals(90, FileManager.calculatePercentageSaved(1000L, 100L))
    }
}
