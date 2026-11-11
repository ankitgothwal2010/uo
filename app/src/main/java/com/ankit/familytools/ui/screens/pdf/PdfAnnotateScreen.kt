package com.ankit.familytools.ui.screens.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ankit.familytools.data.repository.HistoryRepository
import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.engine.pdf.PdfRendererHelper
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.theme.AccentPdf
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - PDF Annotation Screen
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Freehand drawing, highlighter, text notes, and burning annotations into PDF pages.
 */

data class AnnotationStroke(
    val path: AndroidPath,
    val color: Int,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
)

@Composable
fun PdfAnnotateScreen(
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

    // Tools: "pen", "highlighter"
    var activeTool by remember { mutableStateOf("pen") }
    var selectedColor by remember { mutableStateOf(android.graphics.Color.RED) }

    val strokes = remember { mutableStateListOf<AnnotationStroke>() }
    var activeStrokePath by remember { mutableStateOf<AndroidPath?>(null) }

    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "annot_src_")
                    val count = PdfRendererHelper.getPageCount(temp)
                    val firstBmp = PdfRendererHelper.renderPageToBitmap(temp, 0)
                    withContext(Dispatchers.Main) {
                        inputPdfFile = temp
                        inputPdfName = FileManager.getFileName(context, it)
                        pageCount = count
                        currentPageIndex = 0
                        currentPageBitmap = firstBmp
                        strokes.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    fun loadPage(idx: Int) {
        val f = inputPdfFile ?: return
        coroutineScope.launch(Dispatchers.IO) {
            val bmp = PdfRendererHelper.renderPageToBitmap(f, idx)
            withContext(Dispatchers.Main) {
                currentPageIndex = idx
                currentPageBitmap = bmp
                strokes.clear()
            }
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Annotate PDF",
                subtitle = "पीडीएफ पर लिखें व हाइलाइट करें",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (inputPdfFile != null) {
                        IconButton(onClick = { pdfPicker.launch(arrayOf("application/pdf")) }) {
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
                .padding(horizontal = 16.dp)
        ) {
            if (inputPdfFile == null) {
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
                            Icon(Icons.Default.Draw, contentDescription = null, tint = AccentPdf, modifier = Modifier.size(60.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select PDF to Annotate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("पीडीएफ पर हाथ से लिखने या हाइलाइट करने के लिए फाइल चुनें", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { pdfPicker.launch(arrayOf("application/pdf")) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Choose PDF / पीडीएफ चुनें")
                            }
                        }
                    }
                }
            } else {
                // Document Info & Page Navigator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Page ${currentPageIndex + 1} of $pageCount",
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { if (currentPageIndex > 0) loadPage(currentPageIndex - 1) }, enabled = currentPageIndex > 0) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                        }
                        IconButton(onClick = { if (currentPageIndex < pageCount - 1) loadPage(currentPageIndex + 1) }, enabled = currentPageIndex < pageCount - 1) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }

                // Interactive Annotation Surface
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
                        val baseBmp = currentPageBitmap!!
                        // Composite overlay bitmap
                        val overlayBitmap = remember(currentPageIndex, strokes.size, activeStrokePath) {
                            val w = baseBmp.width
                            val h = baseBmp.height
                            val composite = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                            val canvas = AndroidCanvas(composite)
                            canvas.drawBitmap(baseBmp, 0f, 0f, null)

                            val paint = AndroidPaint().apply {
                                isAntiAlias = true
                                style = AndroidPaint.Style.STROKE
                                strokeCap = AndroidPaint.Cap.ROUND
                                strokeJoin = AndroidPaint.Join.ROUND
                            }

                            strokes.forEach { s ->
                                paint.color = s.color
                                paint.strokeWidth = s.strokeWidth
                                if (s.isHighlighter) paint.alpha = 100 else paint.alpha = 255
                                canvas.drawPath(s.path, paint)
                            }

                            activeStrokePath?.let { p ->
                                paint.color = selectedColor
                                paint.strokeWidth = if (activeTool == "highlighter") 28f else 8f
                                if (activeTool == "highlighter") paint.alpha = 100 else paint.alpha = 255
                                canvas.drawPath(p, paint)
                            }

                            composite
                        }

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(currentPageIndex, activeTool, selectedColor) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val scaleX = baseBmp.width.toFloat() / size.width.toFloat()
                                            val scaleY = baseBmp.height.toFloat() / size.height.toFloat()
                                            val p = AndroidPath().apply {
                                                moveTo(offset.x * scaleX, offset.y * scaleY)
                                            }
                                            activeStrokePath = p
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val scaleX = baseBmp.width.toFloat() / size.width.toFloat()
                                            val scaleY = baseBmp.height.toFloat() / size.height.toFloat()
                                            activeStrokePath?.lineTo(change.position.x * scaleX, change.position.y * scaleY)
                                        },
                                        onDragEnd = {
                                            activeStrokePath?.let {
                                                strokes.add(
                                                    AnnotationStroke(
                                                        path = it,
                                                        color = selectedColor,
                                                        strokeWidth = if (activeTool == "highlighter") 28f else 8f,
                                                        isHighlighter = (activeTool == "highlighter")
                                                    )
                                                )
                                                activeStrokePath = null
                                            }
                                        },
                                        onDragCancel = { activeStrokePath = null }
                                    )
                                }
                        ) {
                            Image(
                                bitmap = overlayBitmap.asImageBitmap(),
                                contentDescription = "Annotated PDF",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toolbar: Pen, Highlighter, Colors, Undo, Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = activeTool == "pen",
                            onClick = { activeTool = "pen" },
                            label = { Text("Pen / पेन") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = activeTool == "highlighter",
                            onClick = { activeTool = "highlighter" },
                            label = { Text("Highlight") },
                            leadingIcon = { Icon(Icons.Default.Highlight, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.size - 1) },
                            enabled = strokes.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo")
                        }
                        IconButton(
                            onClick = { strokes.clear() },
                            enabled = strokes.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }

                // Color Palette
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        android.graphics.Color.RED to Color.Red,
                        android.graphics.Color.BLUE to Color.Blue,
                        android.graphics.Color.YELLOW to Color.Yellow,
                        android.graphics.Color.GREEN to Color.Green,
                        android.graphics.Color.BLACK to Color.Black
                    ).forEach { (aColor, cColor) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(cColor)
                                .border(
                                    if (selectedColor == aColor) 3.dp else 1.dp,
                                    if (selectedColor == aColor) MaterialTheme.colorScheme.primary else Color.Gray,
                                    CircleShape
                                )
                                .clickable { selectedColor = aColor }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val doc = PDDocument.load(inputPdfFile!!)
                                val page = doc.getPage(currentPageIndex)
                                val mediaBox = page.cropBox ?: page.mediaBox

                                // Render strokes overlay to transparent bitmap
                                val w = mediaBox.width.toInt()
                                val h = mediaBox.height.toInt()
                                val annotBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                                val canvas = AndroidCanvas(annotBmp)

                                val scaleX = w.toFloat() / (currentPageBitmap?.width ?: w).toFloat()
                                val scaleY = h.toFloat() / (currentPageBitmap?.height ?: h).toFloat()

                                val paint = AndroidPaint().apply {
                                    isAntiAlias = true
                                    style = AndroidPaint.Style.STROKE
                                    strokeCap = AndroidPaint.Cap.ROUND
                                    strokeJoin = AndroidPaint.Join.ROUND
                                }

                                strokes.forEach { s ->
                                    paint.color = s.color
                                    paint.strokeWidth = s.strokeWidth * scaleX
                                    if (s.isHighlighter) paint.alpha = 110 else paint.alpha = 255
                                    canvas.drawPath(s.path, paint)
                                }

                                val pdImage = LosslessFactory.createFromImage(doc, annotBmp)
                                PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                                    cs.drawImage(pdImage, 0f, 0f, mediaBox.width, mediaBox.height)
                                }

                                val out = FileManager.createOutputFile(context, "annotated_${inputPdfName}", "pdf")
                                doc.save(out)
                                doc.close()

                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    resultFile = out
                                    historyRepo.addHistoryItem("Annotate PDF", inputPdfName, out.name, out.absolutePath, out.length(), "application/pdf")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) { isProcessing = false }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Annotated PDF / सेव करें", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Saving annotations into PDF…")
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
                context.startActivity(Intent.createChooser(intent, "Share Annotated PDF"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
