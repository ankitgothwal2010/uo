package com.ankit.familytools

import com.ankit.familytools.data.model.ToolCatalog
import com.ankit.familytools.data.model.ToolCategory
import org.junit.Assert.*
import org.junit.Test

/**
 * AnkitG Family Tools - Catalog and Model Tests
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */
class ToolCatalogTest {

    @Test
    fun testToolCatalogIntegrity() {
        val tools = ToolCatalog.allTools
        assertTrue("Catalog should contain at least 25 tools", tools.size >= 25)

        val uniqueIds = tools.map { it.id }.toSet()
        assertEquals("All tools must have unique IDs", tools.size, uniqueIds.size)

        tools.forEach { tool ->
            assertFalse("Tool title must not be empty", tool.title.isBlank())
            assertFalse("Tool Hindi title must not be empty", tool.hindiTitle.isBlank())
            assertFalse("Tool description must not be empty", tool.description.isBlank())
            assertFalse("Tool route must not be empty", tool.route.isBlank())
            assertNotNull("Tool icon vector must be non-null", tool.getVectorIcon())
        }
    }

    @Test
    fun testPrimaryShortcutsAvailability() {
        val shortcuts = ToolCatalog.allTools.filter { it.isPrimaryShortcut }
        assertTrue("Should have at least 5 primary shortcuts", shortcuts.size >= 5)

        val shortcutIds = shortcuts.map { it.id }
        assertTrue(shortcutIds.contains("pdf_sign"))
        assertTrue(shortcutIds.contains("video_compress"))
        assertTrue(shortcutIds.contains("pdf_merge"))
        assertTrue(shortcutIds.contains("image_crop"))
        assertTrue(shortcutIds.contains("image_to_pdf"))
    }
}
