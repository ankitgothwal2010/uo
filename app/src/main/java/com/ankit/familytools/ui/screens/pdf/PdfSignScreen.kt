package com.ankit.familytools.ui.screens.pdf
import androidx.compose.foundation.BorderStroke

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ankit.familytools.data.model.PageSignData
import com.ankit.familytools.data.repository.HistoryRepository
import com.ankit.familytools.data.repository.SignatureRepository
import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.engine.pdf.PdfRendererHelper
import com.ankit.familytools.engine.pdf.PdfSigner
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.components.SignatureDrawingCanvas
import com.ankit.familytools.ui.theme.AccentPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * AnkitG Family Tools - PDF Signer (Enhanced Workflow)
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

@Composable
fun PdfSignScreen(
    signatureRepo: SignatureRepository,
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputPdfFile by remember { mutableStateOf<File?>(null) }
    var inputPdfName by remember { mutableStateOf("") }
    var pageCount by remember { mutableStateOf(0) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val pageSigns = remember { mutableStateMapOf<Int, PageSignData>() }

    var activeSigBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var normX by remember { mutableStateOf(0.35f) }
    var normY by remember { mutableStateOf(0.65f) }
    var normW by remember { mutableStateOf(0.35f) }
    var normH by remember { mutableStateOf(0.12f) }
    var typedName by remember { mutableStateOf("") }
    var addDateStamp by remember { mutableStateOf(true) }

    var showDrawDialog by remember { mutableStateOf(false) }
    var showSavedSigsDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val savedSignatures by signatureRepo.signaturesFlow.collectAsState()

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "sign_src_")
                    val count = PdfRendererHelper.getPageCount(temp)
                    val firstPage = PdfRendererHelper.renderPageToBitmap(temp, 0)
                    withContext(Dispatchers.Main) {
                        inputPdfFile = temp
                        inputPdfName = FileManager.getFileName(context, it)
                        pageCount = count
                        currentPageIndex = 0
                        currentPageBitmap = firstPage
                        pageSigns.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    fun loadPage(index: Int) {
        val file = inputPdfFile ?: return
        coroutineScope.launch(Dispatchers.IO) {
            val bmp = PdfRendererHelper.renderPageToBitmap(file, index)
            withContext(Dispatchers.Main) {
                currentPageIndex = index
                currentPageBitmap = bmp
                val existing = pageSigns[index]
                if (existing != null) {
                    activeSigBitmap = existing.signatureBitmap
                    normX = existing.normX
                    normY = existing.normY
                    normW = existing.normWidth
                    normH = existing.normHeight
                    typedName = existing.typedName ?: ""
                }
            }
        }
    }

    fun saveCurrentPageSign() {
        if (activeSigBitmap != null) {
            val dateStr = if (addDateStamp) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            } else null

            pageSigns[currentPageIndex] = PageSignData(
                pageIndex = currentPageIndex,
                signatureBitmap = activeSigBitmap,
                normX = normX,
                normY = normY,
                normWidth = normW,
                normHeight = normH,
                typedName = if (typedName.isNotBlank()) typedName else null,
                dateText = dateStr
            )
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Sign PDF",
                subtitle = "दस्तावेज़ पर हस्ताक्षर करें",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (inputPdfFile != null) {
                        IconButton(onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Change PDF")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (inputPdfFile == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                Icon(Icons.Default.Draw, contentDescription = null, tint = AccentPdf, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select PDF to Sign", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("साइन करने के लिए पीडीएफ फाइल चुनें", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose PDF / पीडीएफ चुनें")
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(inputPdfName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("Page ${currentPageIndex + 1} of $pageCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (currentPageIndex > 0) loadPage(currentPageIndex - 1) }, enabled = currentPageIndex > 0) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                            }
                            Text("${currentPageIndex + 1} / $pageCount", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (currentPageIndex < pageCount - 1) loadPage(currentPageIndex + 1) }, enabled = currentPageIndex < pageCount - 1) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                            }
                        }
                    }

                    // Interactive PDF Page Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentPageBitmap != null) {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val containerW = maxWidth
                                val containerH = maxHeight

                                Image(
                                    bitmap = currentPageBitmap!!.asImageBitmap(),
                                    contentDescription = "PDF Page Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )

                                // Draggable and Resizable Signature Box
                                if (activeSigBitmap != null) {
                                    val sigW = (containerW * normW).coerceAtLeast(100.dp)
                                    val sigH = (containerH * normH).coerceAtLeast(40.dp)
                                    val posX = (containerW * normX).coerceIn(0.dp, containerW - sigW)
                                    val posY = (containerH * normY).coerceIn(0.dp, containerH - sigH)

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(posX.roundToPx(), posY.roundToPx()) }
                                            .size(sigW, sigH)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.90f), RoundedCornerShape(8.dp))
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val newX = normX + (dragAmount.x / containerW.toPx())
                                                    val newY = normY + (dragAmount.y / containerH.toPx())
                                                    normX = newX.coerceIn(0f, 1f - normW)
                                                    normY = newY.coerceIn(0f, 1f - normH)
                                                }
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(4.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Image(
                                                bitmap = activeSigBitmap!!.asImageBitmap(),
                                                contentDescription = "Signature",
                                                modifier = Modifier.fillMaxWidth().weight(1f),
                                                contentScale = ContentScale.Fit
                                            )
                                            if (typedName.isNotBlank()) {
                                                Text(typedName, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                            if (addDateStamp) {
                                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Color.DarkGray)
                                            }
                                        }

                                        // Resize handle
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .align(Alignment.BottomEnd)
                                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                                .pointerInput(Unit) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()
                                                        normW = (normW + dragAmount.x / containerW.toPx()).coerceIn(0.2f, 0.8f)
                                                        normH = (normH + dragAmount.y / containerH.toPx()).coerceIn(0.08f, 0.4f)
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        Text(
                                            text = "Tap 'Draw Signature' below to sign\nहस्ताक्षर लगाने के लिए नीचे बटन दबाएं",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Direct Position Fine-Tuning Controls
                    if (activeSigBitmap != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Move: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { normY = (normY - 0.05f).coerceAtLeast(0f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { normY = (normY + 0.05f).coerceAtMost(1f - normH) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { normX = (normX - 0.05f).coerceAtLeast(0f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Left", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { normX = (normX + 0.05f).coerceAtMost(1f - normW) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Right", modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Size: ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { normW = (normW * 1.15f).coerceAtMost(0.8f); normH = (normH * 1.15f).coerceAtMost(0.4f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Bigger", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { normW = (normW * 0.85f).coerceAtLeast(0.2f); normH = (normH * 0.85f).coerceAtLeast(0.08f) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Smaller", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Controls Row: Draw, Choose Saved
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDrawDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Draw / बनाएं", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showSavedSigsDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saved (${savedSignatures.size})", fontSize = 13.sp)
                        }
                    }

                    // Optional Typed Name and Date toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedName,
                            onValueChange = { typedName = it },
                            placeholder = { Text("Name / नाम (Optional)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = addDateStamp, onCheckedChange = { addDateStamp = it })
                            Text("Date", fontSize = 13.sp)
                        }
                    }

                    // Final Burn & Save Button
                    Button(
                        onClick = {
                            saveCurrentPageSign()
                            if (pageSigns.isNotEmpty()) {
                                isProcessing = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val out = FileManager.createOutputFile(context, "signed_${inputPdfName}", "pdf")
                                    val success = PdfSigner.signPdf(inputPdfFile!!, out, pageSigns.values.toList())
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        if (success) {
                                            resultFile = out
                                            historyRepo.addHistoryItem("Sign PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                        }
                                    }
                                }
                            }
                        },
                        enabled = activeSigBitmap != null || pageSigns.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Signature & Save / साइन करके सेव करें", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Finger Drawing Dialog with Name & Save Option
    if (showDrawDialog) {
        Dialog(onDismissRequest = { showDrawDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().height(480.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Draw Signature / हस्ताक्षर बनाएं", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    SignatureDrawingCanvas(
                        modifier = Modifier.weight(1f),
                        initialName = "Family Signature ${savedSignatures.size + 1}",
                        onExportReady = { bmp, name, saveFuture ->
                            activeSigBitmap = bmp
                            showDrawDialog = false
                            if (saveFuture) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    signatureRepo.saveSignature(name, bmp)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Saved Signatures Dialog
    if (showSavedSigsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedSigsDialog = false },
            title = { Text("Choose Saved Signature / सहेजा हुआ हस्ताक्षर") },
            text = {
                if (savedSignatures.isEmpty()) {
                    Text("No saved signatures yet. Use 'Draw' to create one.\nकोई हस्ताक्षर सहेजा नहीं गया है।")
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(savedSignatures) { _, item ->
                            val bmp = remember(item.id) { signatureRepo.loadBitmap(item) }
                            if (bmp != null) {
                                Card(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .fillMaxHeight()
                                        .clickable {
                                            activeSigBitmap = bmp
                                            showSavedSigsDialog = false
                                        },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = item.name,
                                            modifier = Modifier.weight(1f).fillMaxWidth(),
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(text = item.name, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSavedSigsDialog = false }) { Text("Close") }
            }
        )
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Embedding signature and generating PDF…")
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
                context.startActivity(Intent.createChooser(intent, "Share Signed PDF"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
