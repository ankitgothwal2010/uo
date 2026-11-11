package com.ankit.familytools.ui.screens.image

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ankit.familytools.data.repository.HistoryRepository
import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.engine.image.ImageEngine
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.theme.AccentImage
import com.ankit.familytools.ui.theme.SuccessGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Image Compression & Format Conversion
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

@Composable
fun ImageCompressScreen(
    action: String = "compress",
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputImageFile by remember { mutableStateOf<File?>(null) }
    var inputImageName by remember { mutableStateOf("") }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var quality by remember { mutableStateOf(75) } // 1-100
    var selectedFormat by remember { mutableStateOf("JPEG") } // JPEG, PNG, WEBP

    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var savedPercent by remember { mutableStateOf(0) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "img_comp_")
                    val bmp = ImageEngine.decodeWithExif(temp, maxDimension = 2048)
                    val name = FileManager.getFileName(context, it)
                    withContext(Dispatchers.Main) {
                        inputImageFile = temp
                        inputImageName = name
                        currentBitmap = bmp
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
                title = if (action == "convert") "Convert Format" else "Compress Image",
                subtitle = "फोटो कंप्रेस और फॉर्मेट बदलें",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (inputImageFile != null) {
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Change Image")
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
                .padding(16.dp)
        ) {
            if (inputImageFile == null) {
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
                                    .background(AccentImage.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Compress, contentDescription = null, tint = AccentImage, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select Photo to Compress", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("फोटो की क्वालिटी बनाए रखते हुए साइज़ छोटा करें", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    // Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        currentBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Size Comparison Box
                    val estimatedSize = ImageEngine.estimateCompressedSize(inputImageFile!!.length(), quality)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Original Size", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FileManager.formatFileSize(inputImageFile!!.length()), fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                            Column {
                                Text("Estimated Output", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("~${FileManager.formatFileSize(estimatedSize)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quality Slider
                    Text("Compression Quality: $quality%", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 10f..95f,
                        steps = 16
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Format Picker
                    Text("Output Format / फॉर्मेट:", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("JPEG", "PNG", "WEBP").forEach { fmt ->
                            FilterChip(
                                selected = selectedFormat == fmt,
                                onClick = { selectedFormat = fmt },
                                label = { Text(fmt) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val ext = when (selectedFormat) {
                                    "PNG" -> "png"
                                    "WEBP" -> "webp"
                                    else -> "jpg"
                                }
                                val compressFormat = when (selectedFormat) {
                                    "PNG" -> Bitmap.CompressFormat.PNG
                                    "WEBP" -> Bitmap.CompressFormat.WEBP
                                    else -> Bitmap.CompressFormat.JPEG
                                }
                                val out = FileManager.createOutputFile(context, "compressed_${inputImageName}", ext)
                                val success = ImageEngine.compressImage(inputImageFile!!, out, quality, compressFormat)
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) {
                                        resultFile = out
                                        savedPercent = FileManager.calculatePercentageSaved(inputImageFile!!.length(), out.length())
                                        historyRepo.addHistoryItem("Compress Image", inputImageName, out.name, out.absolutePath, out.length(), "image/$ext")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Compress, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compress & Save / सेव करें", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Compressing photo…")
    }

    resultFile?.let { file ->
        ResultDialog(
            fileName = file.name,
            fileSizeStr = FileManager.formatFileSize(file.length()),
            savedPercentage = savedPercent,
            onOpen = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open Image"))
            },
            onShare = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Image"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
