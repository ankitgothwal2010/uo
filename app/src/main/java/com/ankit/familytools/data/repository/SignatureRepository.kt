package com.ankit.familytools.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ankit.familytools.data.model.SignatureItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

class SignatureRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("family_signatures_pref", Context.MODE_PRIVATE)
    private val signaturesDir = File(context.filesDir, "signatures").apply { mkdirs() }

    private val _signaturesFlow = MutableStateFlow<List<SignatureItem>>(emptyList())
    val signaturesFlow: StateFlow<List<SignatureItem>> = _signaturesFlow.asStateFlow()

    init {
        loadSignatures()
    }

    fun loadSignatures() {
        val jsonStr = prefs.getString("saved_signatures", "[]") ?: "[]"
        val list = mutableListOf<SignatureItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val item = SignatureItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    imagePath = obj.getString("imagePath"),
                    createdAt = obj.getLong("createdAt")
                )
                if (File(item.imagePath).exists()) {
                    list.add(item)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _signaturesFlow.value = list.sortedByDescending { it.createdAt }
    }

    fun saveSignature(name: String, bitmap: Bitmap): SignatureItem {
        val id = System.currentTimeMillis().toString()
        val file = File(signaturesDir, "sig_$id.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val item = SignatureItem(
            id = id,
            name = if (name.isBlank()) "Signature ${signaturesFlow.value.size + 1}" else name,
            imagePath = file.absolutePath
        )
        val current = _signaturesFlow.value.toMutableList()
        current.add(0, item)
        _signaturesFlow.value = current
        persist(current)
        return item
    }

    fun loadBitmap(item: SignatureItem): Bitmap? {
        val file = File(item.imagePath)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    fun deleteSignature(id: String) {
        val current = _signaturesFlow.value.toMutableList()
        val item = current.find { it.id == id }
        if (item != null) {
            current.remove(item)
            _signaturesFlow.value = current
            persist(current)
            try {
                File(item.imagePath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun persist(list: List<SignatureItem>) {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("imagePath", item.imagePath)
            obj.put("createdAt", item.createdAt)
            array.put(obj)
        }
        prefs.edit().putString("saved_signatures", array.toString()).apply()
    }
}
