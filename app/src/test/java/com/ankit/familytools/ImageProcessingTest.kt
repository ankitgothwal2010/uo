package com.ankit.familytools

import com.ankit.familytools.engine.image.ImageEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * AnkitG Family Tools - Image Processing Tests
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */
class ImageProcessingTest {

    @Test
    fun testCompressionEstimate() {
        val originalBytes = 10_000_000L // 10 MB
        val estHigh = ImageEngine.estimateCompressedSize(originalBytes, 90)
        val estMedium = ImageEngine.estimateCompressedSize(originalBytes, 50)
        val estLow = ImageEngine.estimateCompressedSize(originalBytes, 20)

        assertTrue("Higher quality must produce larger estimated size", estHigh > estMedium)
        assertTrue("Medium quality must produce larger estimated size than low", estMedium > estLow)
        assertTrue("Estimated size must be less than original", estMedium < originalBytes)
    }

    @Test
    fun testCropBoundsIntegrity() {
        // Test crop normalization calculations
        val origW = 1920
        val origH = 1080

        val normLeft = 0.1f
        val normTop = 0.2f
        val normRight = 0.9f
        val normBottom = 0.8f

        val x = (normLeft * origW).toInt()
        val y = (normTop * origH).toInt()
        val w = ((normRight - normLeft) * origW).toInt()
        val h = ((normBottom - normTop) * origH).toInt()

        assertEquals(192, x)
        assertEquals(216, y)
        assertEquals(1536, w)
        assertEquals(648, h)
        assertTrue(x + w <= origW)
        assertTrue(y + h <= origH)
    }
}
