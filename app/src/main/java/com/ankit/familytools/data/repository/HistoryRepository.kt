package com.ankit.familytools.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ankit.familytools.data.model.ProcessingHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

class HistoryRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("family_tools_history", Context.MODE_PRIVATE)

    private val _historyFlow = MutableStateFlow<List<ProcessingHistoryItem>>(emptyList())
    val historyFlow: StateFlow<List<ProcessingHistoryItem>> = _historyFlow.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        val jsonStr = prefs.getString("history_items", "[]") ?: "[]"
        val list = mutableListOf<ProcessingHistoryItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val item = ProcessingHistoryItem(
                    id = obj.getString("id"),
                    toolName = obj.getString("toolName"),
                    inputFileName = obj.getString("inputFileName"),
                    outputFileName = obj.getString("outputFileName"),
                    outputFilePath = obj.getString("outputFilePath"),
                    timestamp = obj.getLong("timestamp"),
                    fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                    mimeType = obj.optString("mimeType", "*/*")
                )
                // Only include if file actually exists or is a valid URI
                if (File(item.outputFilePath).exists() || item.outputFilePath.startsWith("content://")) {
                    list.add(item)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _historyFlow.value = list.sortedByDescending { it.timestamp }
    }

    fun addHistoryItem(
        toolName: String,
        inputFileName: String,
        outputFileName: String,
        outputFilePath: String,
        fileSizeBytes: Long = 0L,
        mimeType: String = "*/*"
    ) {
        val current = _historyFlow.value.toMutableList()
        val newItem = ProcessingHistoryItem(
            toolName = toolName,
            inputFileName = inputFileName,
            outputFileName = outputFileName,
            outputFilePath = outputFilePath,
            fileSizeBytes = fileSizeBytes,
            mimeType = mimeType
        )
        current.add(0, newItem)
        // Keep last 100 operations
        val trimmed = if (current.size > 100) current.subList(0, 100) else current
        _historyFlow.value = trimmed
        persist(trimmed)
    }

    fun deleteItem(id: String) {
        val current = _historyFlow.value.toMutableList()
        val item = current.find { it.id == id }
        if (item != null) {
            current.remove(item)
            _historyFlow.value = current
            persist(current)
            // Remove file if local
            try {
                val f = File(item.outputFilePath)
                if (f.exists() && f.isFile) {
                    f.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAll() {
        _historyFlow.value = emptyList()
        prefs.edit().remove("history_items").apply()
    }

    private fun persist(list: List<ProcessingHistoryItem>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("toolName", item.toolName)
            obj.put("inputFileName", item.inputFileName)
            obj.put("outputFileName", item.outputFileName)
            obj.put("outputFilePath", item.outputFilePath)
            obj.put("timestamp", item.timestamp)
            obj.put("fileSizeBytes", item.fileSizeBytes)
            obj.put("mimeType", item.mimeType)
            array.put(obj)
        }
        prefs.edit().putString("history_items", array.toString()).apply()
    }
}
