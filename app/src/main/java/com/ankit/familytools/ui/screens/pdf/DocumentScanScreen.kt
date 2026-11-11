package com.ankit.familytools.ui.screens.pdf

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ankit.familytools.data.repository.HistoryRepository
import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.engine.image.ImageEngine
import com.ankit.familytools.engine.pdf.OcrEngine
import com.ankit.familytools.engine.pdf.PdfEngine
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.theme.AccentPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Document Scanner & OCR
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Camera capture & photo import, perspective crop/rotation, on-device OCR,
 * and text export to searchable PDF or clipboard.
 */

@Composable
fun DocumentScanScreen(
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultPdfFile by remember { mutableStateOf<File?>(null) }

    // Photo / File Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "scan_raw_")
                    val bmp = ImageEngine.decodeWithExif(temp, maxDimension = 2048)
                    withContext(Dispatchers.Main) {
                        capturedBitmap = bmp
                        recognizedText = ""
                    }
                    if (bmp != null) {
                        val ocrResult = OcrEngine.recognizeTextFromBitmap(bmp)
                        withContext(Dispatchers.Main) {
                            recognizedText = ocrResult.getOrDefault("No text recognized in document.")
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

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Scan & OCR",
                subtitle = "दस्तावेज़ स्कैन और टेक्स्ट निकालें",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Pick Image")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (capturedBitmap == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AccentPdf.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = AccentPdf,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scan Document or Photo",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "दस्तावेज़ की फोटो चुनें और उसमें से लिखा हुआ टेक्स्ट निकालें",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { imagePicker.launch("image/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose Photo / फोटो चुनें")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Document Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Scanned document",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // OCR Results Card
                    Text(
                        text = "Extracted Text / निकाला गया टेक्स्ट",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recognizedText,
                        onValueChange = { recognizedText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons: Copy, Export as PDF, Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Scanned Text", recognizedText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Text copied to clipboard! / टेक्स्ट कॉपी हो गया", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy / कॉपी")
                        }

                        Button(
                            onClick = {
                                if (recognizedText.isNotBlank()) {
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val out = FileManager.createOutputFile(context, "scanned_text_document", "pdf")
                                        val success = PdfEngine.textToPdf(recognizedText, out, "Scanned Document")
                                        withContext(Dispatchers.Main) {
                                            isProcessing = false
                                            if (success) {
                                                resultPdfFile = out
                                                historyRepo.addHistoryItem(
                                                    toolName = "Scan & OCR to PDF",
                                                    inputFileName = "Scanned Text",
                                                    outputFileName = out.name,
                                                    outputFilePath = out.absolutePath,
                                                    fileSizeBytes = out.length(),
                                                    mimeType = "application/pdf"
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save as PDF")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Processing document and extracting text…")
    }

    resultPdfFile?.let { file ->
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
            onDismiss = { resultPdfFile = null }
        )
    }
}
