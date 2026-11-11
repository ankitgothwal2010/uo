package com.ankit.familytools

import com.ankit.familytools.engine.pdf.PdfSigner
import org.junit.Assert.*
import org.junit.Test

/**
 * AnkitG Family Tools - PDF Coordinate Mapping Tests
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */
class PdfCoordinateTest {

    private val a4Width = 595.0f
    private val a4Height = 842.0f

    @Test
    fun testStandardRotationCoordinateMapping() {
        // Place signature at center of screen: normX = 0.3f, normY = 0.4f, normW = 0.4f, normH = 0.2f
        val bounds = PdfSigner.mapToPdfCoordinates(
            pageWidth = a4Width,
            pageHeight = a4Height,
            pageRotation = 0,
            normX = 0.3f,
            normY = 0.4f,
            normWidth = 0.4f,
            normHeight = 0.2f
        )

        assertEquals(0f, bounds.rotation, 0.001f)
        assertEquals(a4Width * 0.4f, bounds.width, 0.1f)
        assertEquals(a4Height * 0.2f, bounds.height, 0.1f)
        assertEquals(a4Width * 0.3f, bounds.x, 0.1f)
        // In PDF (origin bottom-left), y = (1.0 - 0.4 - 0.2) * height = 0.4 * height
        assertEquals(a4Height * 0.4f, bounds.y, 0.1f)
    }

    @Test
    fun testCornerPlacementBounds() {
        // Top-left corner placement
        val topLeft = PdfSigner.mapToPdfCoordinates(
            pageWidth = a4Width,
            pageHeight = a4Height,
            pageRotation = 0,
            normX = 0f,
            normY = 0f,
            normWidth = 0.2f,
            normHeight = 0.1f
        )
        assertEquals(0f, topLeft.x, 0.001f)
        assertEquals(a4Height * 0.9f, topLeft.y, 0.1f)

        // Bottom-left corner placement
        val bottomLeft = PdfSigner.mapToPdfCoordinates(
            pageWidth = a4Width,
            pageHeight = a4Height,
            pageRotation = 0,
            normX = 0f,
            normY = 0.9f,
            normWidth = 0.2f,
            normHeight = 0.1f
        )
        assertEquals(0f, bottomLeft.x, 0.001f)
        assertEquals(0f, bottomLeft.y, 0.001f)
    }

    @Test
    fun testRotated90DegreesMapping() {
        val bounds = PdfSigner.mapToPdfCoordinates(
            pageWidth = a4Width,
            pageHeight = a4Height,
            pageRotation = 90,
            normX = 0.2f,
            normY = 0.3f,
            normWidth = 0.4f,
            normHeight = 0.2f
        )
        assertEquals(90f, bounds.rotation, 0.001f)
        assertTrue(bounds.x >= 0f)
        assertTrue(bounds.y >= 0f)
        assertTrue(bounds.width > 0f)
        assertTrue(bounds.height > 0f)
    }

    @Test
    fun testOutOfBoundsClamping() {
        val bounds = PdfSigner.mapToPdfCoordinates(
            pageWidth = a4Width,
            pageHeight = a4Height,
            pageRotation = 0,
            normX = -0.5f,
            normY = 1.5f,
            normWidth = 2.0f,
            normHeight = 3.0f
        )
        // Coordinates and dimensions must be safely clamped
        assertTrue("X must be >= 0", bounds.x >= 0f)
        assertTrue("Y must be >= 0", bounds.y >= 0f)
        assertTrue("Width must be <= pageWidth", bounds.width <= a4Width)
        assertTrue("Height must be <= pageHeight", bounds.height <= a4Height)
    }
}
