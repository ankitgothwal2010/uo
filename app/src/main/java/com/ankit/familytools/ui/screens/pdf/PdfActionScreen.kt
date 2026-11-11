package com.ankit.familytools.ui.screens.pdf

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ankit.familytools.data.repository.HistoryRepository
import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.engine.pdf.PdfEngine
import com.ankit.familytools.engine.pdf.PdfRendererHelper
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.theme.AccentPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Comprehensive PDF Action Screen
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Implements: Split PDF, Reorder/Rotate/Delete, Compress, Images to PDF,
 * Text to PDF, Extract Text, Watermark, Page Numbers, Password Protect & Unlock.
 */

@Composable
fun PdfActionScreen(
    action: String,
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputPdfFile by remember { mutableStateOf<File?>(null) }
    var inputPdfName by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf(0) }

    // Multi-Image picker for images_to_pdf
    val selectedImages = remember { mutableStateListOf<File>() }

    // Text to PDF state
    var textInputContent by remember { mutableStateOf("") }

    // Split / Extract state
    var splitPageRange by remember { mutableStateOf("1") }

    // Watermark state
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }

    // Password Protect / Unlock state
    var userPassword by remember { mutableStateOf("") }

    // Extracted Text state
    var extractedTextResult by remember { mutableStateOf("") }

    // Reorder state
    val pageOrderList = remember { mutableStateListOf<Int>() }

    // Processing & Output
    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val title = when (action) {
        "pdf_split" -> "Split PDF / अलग करें"
        "pdf_reorder" -> "Reorder Pages / क्रम बदलें"
        "pdf_compress" -> "Compress PDF / साइज़ घटाएं"
        "images_to_pdf" -> "Images to PDF / फोटो से पीडीएफ"
        "text_to_pdf" -> "Text to PDF / टेक्स्ट से पीडीएफ"
        "pdf_extract_text" -> "Extract Text / टेक्स्ट निकालें"
        "pdf_watermark" -> "Add Watermark / वॉटरमार्क"
        "pdf_page_numbers" -> "Page Numbers / पेज नंबर"
        "pdf_protect" -> "Password Protect / लॉक लगाएं"
        "pdf_unlock" -> "Unlock PDF / लॉक हटाएं"
        else -> "PDF Tool"
    }

    val singlePdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "pdf_act_")
                    val count = PdfRendererHelper.getPageCount(temp)
                    val name = FileManager.getFileName(context, it)
                    withContext(Dispatchers.Main) {
                        inputPdfFile = temp
                        inputPdfName = name
                        pageCount = count
                        splitPageRange = "1-$count"
                        pageOrderList.clear()
                        for (i in 0 until count) pageOrderList.add(i)

                        if (action == "pdf_extract_text") {
                            extractedTextResult = PdfEngine.extractText(temp)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    val multiImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                val list = mutableListOf<File>()
                uris.forEach { u ->
                    try {
                        val temp = FileManager.copyToTempFile(context, u, "img_pdf_")
                        list.add(temp)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.Main) {
                    selectedImages.addAll(list)
                    isProcessing = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = title,
                subtitle = "पारिवारिक पीडीएफ टूल",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (action) {
                "images_to_pdf" -> {
                    Text(
                        text = "Turn Photos into a clean PDF / फोटो से पीडीएफ बनाएं",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { multiImagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photos (${selectedImages.size} selected)")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedImages.isNotEmpty()) {
                        Text("${selectedImages.size} Photos Ready to Convert:", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isProcessing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val out = FileManager.createOutputFile(context, "photos_combined", "pdf")
                                    val success = PdfEngine.imagesToPdf(selectedImages, out)
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        if (success) {
                                            resultFile = out
                                            historyRepo.addHistoryItem("Images to PDF", "${selectedImages.size} photos", out.name, out.absolutePath, out.length(), "application/pdf")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Convert to PDF Now / पीडीएफ बनाएं", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "text_to_pdf" -> {
                    Text(
                        text = "Convert Text Notes to PDF / नोट्स को पीडीएफ बनाएं",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = textInputContent,
                        onValueChange = { textInputContent = it },
                        placeholder = { Text("Paste or type your text notes here…\nयहाँ अपना टेक्स्ट लिखें…") },
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (textInputContent.isNotBlank()) {
                                isProcessing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val out = FileManager.createOutputFile(context, "text_document", "pdf")
                                    val success = PdfEngine.textToPdf(textInputContent, out)
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        if (success) {
                                            resultFile = out
                                            historyRepo.addHistoryItem("Text to PDF", "Text Content", out.name, out.absolutePath, out.length(), "application/pdf")
                                        }
                                    }
                                }
                            }
                        },
                        enabled = textInputContent.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create PDF Document / पीडीएफ बनाएं", fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    // Requires single PDF input
                    if (inputPdfFile == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = AccentPdf, modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Choose PDF Document", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("पीडीएफ फाइल चुनें", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { singlePdfPicker.launch(arrayOf("application/pdf")) },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Select PDF / पीडीएफ चुनें")
                                }
                            }
                        }
                    } else {
                        // PDF selected, show tool specific controls
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AccentPdf, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(inputPdfName, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Pages: $pageCount • Size: ${FileManager.formatFileSize(inputPdfFile!!.length())}", fontSize = 12.sp)
                                }
                                TextButton(onClick = { singlePdfPicker.launch(arrayOf("application/pdf")) }) {
                                    Text("Change")
                                }
                            }
                        }

                        when (action) {
                            "pdf_split" -> {
                                Text("Enter Page Numbers / Ranges (e.g. 1-3, 5):", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = splitPageRange,
                                    onValueChange = { splitPageRange = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val pagesToExtract = mutableListOf<Int>()
                                            splitPageRange.split(",").forEach { part ->
                                                val trimmed = part.trim()
                                                if (trimmed.contains("-")) {
                                                    val parts = trimmed.split("-")
                                                    val start = (parts.getOrNull(0)?.toIntOrNull() ?: 1) - 1
                                                    val end = (parts.getOrNull(1)?.toIntOrNull() ?: pageCount) - 1
                                                    for (p in start..end) if (p in 0 until pageCount) pagesToExtract.add(p)
                                                } else {
                                                    val single = (trimmed.toIntOrNull() ?: 1) - 1
                                                    if (single in 0 until pageCount) pagesToExtract.add(single)
                                                }
                                            }
                                            val out = FileManager.createOutputFile(context, "split_${inputPdfName}", "pdf")
                                            val success = PdfEngine.extractPages(inputPdfFile!!, pagesToExtract.distinct(), out)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (success) {
                                                    resultFile = out
                                                    historyRepo.addHistoryItem("Split PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Split & Save PDF / अलग करें")
                                }
                            }

                            "pdf_compress" -> {
                                Text("Compress PDF stream objects and image data:", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val out = FileManager.createOutputFile(context, "compressed_${inputPdfName}", "pdf")
                                            val success = PdfEngine.compressPdf(inputPdfFile!!, out, 2)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (success) {
                                                    resultFile = out
                                                    historyRepo.addHistoryItem("Compress PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Compress PDF Now / साइज़ छोटा करें")
                                }
                            }

                            "pdf_extract_text" -> {
                                Text("Extracted PDF Text:", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = extractedTextResult,
                                    onValueChange = { extractedTextResult = it },
                                    modifier = Modifier.fillMaxWidth().height(260.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("PDF Text", extractedTextResult))
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Text / टेक्स्ट कॉपी करें")
                                }
                            }

                            "pdf_watermark" -> {
                                Text("Watermark Text / वॉटरमार्क टेक्स्ट:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = watermarkText,
                                    onValueChange = { watermarkText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val out = FileManager.createOutputFile(context, "watermarked_${inputPdfName}", "pdf")
                                            val success = PdfEngine.addWatermark(inputPdfFile!!, out, watermarkText)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (success) {
                                                    resultFile = out
                                                    historyRepo.addHistoryItem("Watermark PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Apply Watermark / वॉटरमार्क लगाएं")
                                }
                            }

                            "pdf_page_numbers" -> {
                                Text("Adds 'Page X of Y' numbers at the bottom center of each page.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val out = FileManager.createOutputFile(context, "numbered_${inputPdfName}", "pdf")
                                            val success = PdfEngine.addPageNumbers(inputPdfFile!!, out)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (success) {
                                                    resultFile = out
                                                    historyRepo.addHistoryItem("Page Numbers", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Add Page Numbers / पेज नंबर जोड़ें")
                                }
                            }

                            "pdf_protect" -> {
                                Text("Set Password to Lock PDF / पासवर्ड डालें:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = userPassword,
                                    onValueChange = { userPassword = it },
                                    placeholder = { Text("Enter Password / पासवर्ड लिखें") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (userPassword.isNotBlank()) {
                                            isProcessing = true
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val out = FileManager.createOutputFile(context, "protected_${inputPdfName}", "pdf")
                                                val success = PdfEngine.protectPdf(inputPdfFile!!, out, userPassword, userPassword)
                                                withContext(Dispatchers.Main) {
                                                    isProcessing = false
                                                    if (success) {
                                                        resultFile = out
                                                        historyRepo.addHistoryItem("Protect PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    enabled = userPassword.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Lock PDF with Password / लॉक लगाएं")
                                }
                            }

                            "pdf_unlock" -> {
                                Text("Enter Existing Password to Unlock / वर्तमान पासवर्ड डालें:", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = userPassword,
                                    onValueChange = { userPassword = it },
                                    placeholder = { Text("Enter current password / पासवर्ड लिखें") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (userPassword.isNotBlank()) {
                                            isProcessing = true
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val out = FileManager.createOutputFile(context, "unlocked_${inputPdfName}", "pdf")
                                                val success = PdfEngine.unlockPdf(inputPdfFile!!, out, userPassword)
                                                withContext(Dispatchers.Main) {
                                                    isProcessing = false
                                                    if (success) {
                                                        resultFile = out
                                                        historyRepo.addHistoryItem("Unlock PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                    } else {
                                                        Toast.makeText(context, "Incorrect password or unsupported encryption.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    enabled = userPassword.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Remove Password / पासवर्ड हटाएं")
                                }
                            }

                            "pdf_reorder" -> {
                                Text("Pages in Document (${pageOrderList.size}):", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                pageOrderList.forEachIndexed { idx, pageIdx ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Page ${pageIdx + 1}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                            IconButton(
                                                onClick = {
                                                    if (idx > 0) {
                                                        val tmp = pageOrderList[idx]
                                                        pageOrderList[idx] = pageOrderList[idx - 1]
                                                        pageOrderList[idx - 1] = tmp
                                                    }
                                                },
                                                enabled = idx > 0
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = null)
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (idx < pageOrderList.size - 1) {
                                                        val tmp = pageOrderList[idx]
                                                        pageOrderList[idx] = pageOrderList[idx + 1]
                                                        pageOrderList[idx + 1] = tmp
                                                    }
                                                },
                                                enabled = idx < pageOrderList.size - 1
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = null)
                                            }
                                            IconButton(
                                                onClick = {
                                                    if (pageOrderList.size > 1) {
                                                        pageOrderList.removeAt(idx)
                                                    }
                                                },
                                                enabled = pageOrderList.size > 1
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val out = FileManager.createOutputFile(context, "reordered_${inputPdfName}", "pdf")
                                            val success = PdfEngine.reorderPages(inputPdfFile!!, pageOrderList.toList(), out)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (success) {
                                                    resultFile = out
                                                    historyRepo.addHistoryItem("Reorder Pages", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Save Reordered Pages / सेव करें")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Processing PDF document…")
    }

    resultFile?.let { file ->
        ResultDialog(
            fileName = file.name,
            fileSizeStr = FileManager.formatFileSize(file.length()),
            onOpen = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open PDF"))
            },
            onShare = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share PDF"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
