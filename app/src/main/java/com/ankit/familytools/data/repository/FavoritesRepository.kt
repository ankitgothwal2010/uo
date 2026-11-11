package com.ankit.familytools.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

class FavoritesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("family_tools_favorites", Context.MODE_PRIVATE)

    private val _favoritesFlow = MutableStateFlow<Set<String>>(emptySet())
    val favoritesFlow: StateFlow<Set<String>> = _favoritesFlow.asStateFlow()

    init {
        val saved = prefs.getStringSet("favorite_tool_ids", setOf("pdf_sign", "video_compress", "pdf_merge", "image_crop")) ?: emptySet()
        _favoritesFlow.value = saved
    }

    fun isFavorite(toolId: String): Boolean {
        return _favoritesFlow.value.contains(toolId)
    }

    fun toggleFavorite(toolId: String) {
        val current = _favoritesFlow.value.toMutableSet()
        if (current.contains(toolId)) {
            current.remove(toolId)
        } else {
            current.add(toolId)
        }
        _favoritesFlow.value = current
        prefs.edit().putStringSet("favorite_tool_ids", current).apply()
    }
}
