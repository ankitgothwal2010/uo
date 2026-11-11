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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Image Editor (Crop, Rotate, Resize, Adjust Colors, EXIF)
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

@Composable
fun ImageEditScreen(
    initialAction: String = "crop",
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputImageFile by remember { mutableStateOf<File?>(null) }
    var inputImageName by remember { mutableStateOf("") }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Editor controls
    var activeTab by remember { mutableStateOf(initialAction) } // "crop", "rotate", "resize", "adjust", "exif"
    var cropRatio by remember { mutableStateOf("1:1") } // "1:1", "4:3", "16:9", "Free"
    var rotationAngle by remember { mutableStateOf(0f) }
    var flipH by remember { mutableStateOf(false) }

    // Color adjustments
    var brightness by remember { mutableStateOf(0f) } // -100..100
    var contrast by remember { mutableStateOf(1f) } // 0.5..2.0
    var saturation by remember { mutableStateOf(1f) } // 0..2.0

    // Resize controls
    var resizePercent by remember { mutableStateOf(50) } // 25, 50, 75

    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "img_edit_")
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
                title = "Edit Image",
                subtitle = "फोटो संपादित करें (क्रॉप, रोटेट, रिसाइज़)",
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
                .padding(horizontal = 16.dp)
        ) {
            if (inputImageFile == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = AccentImage,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select Photo to Edit",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "फोटो को क्रॉप, रोटेट, रिसाइज़ या सुधारने के लिए चुनें",
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
                    // Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Operation Tabs
                    ScrollableTabRow(
                        selectedTabIndex = when (activeTab) {
                            "crop" -> 0
                            "rotate" -> 1
                            "resize" -> 2
                            "adjust" -> 3
                            else -> 4
                        },
                        edgePadding = 0.dp
                    ) {
                        Tab(selected = activeTab == "crop", onClick = { activeTab = "crop" }, text = { Text("Crop / काटें") })
                        Tab(selected = activeTab == "rotate", onClick = { activeTab = "rotate" }, text = { Text("Rotate / घुमाएं") })
                        Tab(selected = activeTab == "resize", onClick = { activeTab = "resize" }, text = { Text("Resize / साइज़") })
                        Tab(selected = activeTab == "adjust", onClick = { activeTab = "adjust" }, text = { Text("Colors / रंग") })
                        Tab(selected = activeTab == "exif", onClick = { activeTab = "exif" }, text = { Text("EXIF / डेटा") })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Specific Controls
                    when (activeTab) {
                        "crop" -> {
                            Text("Aspect Ratio / अनुपात:", fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("1:1", "4:3", "16:9", "Free").forEach { ratio ->
                                    FilterChip(
                                        selected = cropRatio == ratio,
                                        onClick = { cropRatio = ratio },
                                        label = { Text(ratio) }
                                    )
                                }
                            }
                        }
                        "rotate" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { rotationAngle = (rotationAngle + 90f) % 360f },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.RotateRight, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rotate 90°")
                                }
                                OutlinedButton(
                                    onClick = { flipH = !flipH },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Flip, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Flip H")
                                }
                            }
                        }
                        "resize" -> {
                            Text("Scale Percentage / प्रतिशत:", fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(25, 50, 75).forEach { pct ->
                                    FilterChip(
                                        selected = resizePercent == pct,
                                        onClick = { resizePercent = pct },
                                        label = { Text("$pct%") }
                                    )
                                }
                            }
                        }
                        "adjust" -> {
                            Text("Brightness (${brightness.toInt()}):", fontSize = 13.sp)
                            Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -50f..50f)
                            Text("Contrast (${String.format("%.1f", contrast)}):", fontSize = 13.sp)
                            Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0.5f..2.0f)
                            Text("Saturation (${String.format("%.1f", saturation)}):", fontSize = 13.sp)
                            Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..2.0f)
                        }
                        "exif" -> {
                            Text(
                                text = "Removes GPS location, camera details, date & personal metadata.\nफोटो से आपकी लोकेशन और फोन की जानकारी हटाई जाएगी।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Execute Button
                    Button(
                        onClick = {
                            isProcessing = true
                            coroutineScope.launch(Dispatchers.IO) {
                                val out = FileManager.createOutputFile(context, "edited_${inputImageName}", "jpg")
                                val success = when (activeTab) {
                                    "crop" -> {
                                        // Standard centered ratio crop
                                        val (nl, nt, nr, nb) = when (cropRatio) {
                                            "1:1" -> listOf(0.1f, 0.1f, 0.9f, 0.9f)
                                            "4:3" -> listOf(0.05f, 0.1f, 0.95f, 0.77f)
                                            "16:9" -> listOf(0.05f, 0.2f, 0.95f, 0.7f)
                                            else -> listOf(0.05f, 0.05f, 0.95f, 0.95f)
                                        }
                                        ImageEngine.cropImage(inputImageFile!!, out, nl, nt, nr, nb)
                                    }
                                    "rotate" -> ImageEngine.rotateAndFlip(inputImageFile!!, out, rotationAngle, flipH)
                                    "resize" -> {
                                        val w = ((currentBitmap?.width ?: 1000) * (resizePercent / 100f)).toInt()
                                        val h = ((currentBitmap?.height ?: 1000) * (resizePercent / 100f)).toInt()
                                        ImageEngine.resizeImage(inputImageFile!!, out, w, h)
                                    }
                                    "adjust" -> ImageEngine.adjustColors(inputImageFile!!, out, brightness, contrast, saturation)
                                    else -> ImageEngine.removeExif(inputImageFile!!, out)
                                }

                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) {
                                        resultFile = out
                                        historyRepo.addHistoryItem(
                                            toolName = "Edit Image (${activeTab.replaceFirstChar { it.uppercase() }})",
                                            inputFileName = inputImageName,
                                            outputFileName = out.name,
                                            outputFilePath = out.absolutePath,
                                            fileSizeBytes = out.length(),
                                            mimeType = "image/jpeg"
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply & Save / सेव करें", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Processing and optimizing image…")
    }

    resultFile?.let { file ->
        ResultDialog(
            fileName = file.name,
            fileSizeStr = FileManager.formatFileSize(file.length()),
            onOpen = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/jpeg")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open Image"))
            },
            onShare = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Image"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
