package com.ankit.familytools.ui.screens.image

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
 * AnkitG Family Tools - Photo Collage Maker
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

@Composable
fun ImageCollageScreen(
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedImages = remember { mutableStateListOf<File>() }
    var columns by remember { mutableStateOf(2) }
    var borderPx by remember { mutableStateOf(16) }
    var isWhiteBorder by remember { mutableStateOf(true) }

    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val multiPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                val list = mutableListOf<File>()
                uris.take(6).forEach { u ->
                    try {
                        val temp = FileManager.copyToTempFile(context, u, "collage_item_")
                        list.add(temp)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.Main) {
                    selectedImages.clear()
                    selectedImages.addAll(list)
                    isProcessing = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Photo Collage",
                subtitle = "फैमिली फोटो कोलाज बनाएं",
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
            Text(
                text = "Select 2 to 6 photos / 2 से 6 फोटो चुनें",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { multiPicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Photos (${selectedImages.size} / 6 selected)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedImages.isNotEmpty()) {
                // Photo Grid Preview
                Text("Selected Photos Grid:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWhiteBorder) Color.LightGray.copy(alpha = 0.3f) else Color.DarkGray)
                        .padding(8.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages) { file ->
                            Image(
                                painter = rememberAsyncImagePainter(file),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Layout Options
                Text("Columns / लेआउट कॉलम:", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(2, 3).forEach { cols ->
                        FilterChip(
                            selected = columns == cols,
                            onClick = { columns = cols },
                            label = { Text("$cols Columns") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Border Color
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Border: ", fontWeight = FontWeight.SemiBold)
                    FilterChip(
                        selected = isWhiteBorder,
                        onClick = { isWhiteBorder = true },
                        label = { Text("White / सफेद") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = !isWhiteBorder,
                        onClick = { isWhiteBorder = false },
                        label = { Text("Black / काला") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val out = FileManager.createOutputFile(context, "collage_result", "jpg")
                            val color = if (isWhiteBorder) AndroidColor.WHITE else AndroidColor.BLACK
                            val success = ImageEngine.createCollage(selectedImages.toList(), out, columns, borderPx, color)
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                if (success) {
                                    resultFile = out
                                    historyRepo.addHistoryItem("Photo Collage", "${selectedImages.size} photos", out.name, out.absolutePath, out.length(), "image/jpeg")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Collage / कोलाज बनाएं", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Compositing photo collage…")
    }

    resultFile?.let { file ->
        ResultDialog(
            fileName = file.name,
            fileSizeStr = FileManager.formatFileSize(file.length()),
            onOpen = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open Collage"))
            },
            onShare = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Collage"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
