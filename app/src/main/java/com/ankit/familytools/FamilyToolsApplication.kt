package com.ankit.familytools

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

/**
 * AnkitG Family Tools - Application Class
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */
class FamilyToolsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize PDFBox-Android resources for font handling and PDF operations
        PDFBoxResourceLoader.init(applicationContext)
    }
}
