package com.ankit.familytools.ui.screens.video
import androidx.compose.foundation.BorderStroke

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
import com.ankit.familytools.engine.video.VideoEngine
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.theme.AccentVideo
import com.ankit.familytools.ui.theme.SuccessGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Video Compressor (Core Mandatory Feature)
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Transcodes video with Android Media3 Transformer, presets (Small, Balanced, High Quality),
 * live progress reporting, estimated size calculations, and output validation.
 */

@Composable
fun VideoCompressScreen(
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputVideoFile by remember { mutableStateOf<File?>(null) }
    var inputVideoName by remember { mutableStateOf("") }
    var videoMetadata by remember { mutableStateOf<VideoEngine.VideoMetadata?>(null) }
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var selectedPreset by remember { mutableStateOf(VideoEngine.CompressionPreset.BALANCED) }
    var isProcessing by remember { mutableStateOf(false) }
    var processingProgress by remember { mutableStateOf(0f) }
    var activeJob by remember { mutableStateOf<Job?>(null) }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var savedPercent by remember { mutableStateOf(0) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "video_raw_")
                    val meta = VideoEngine.getVideoMetadata(temp)
                    val thumb = VideoEngine.extractFrameAtTime(temp, 1_000_000L) // Frame at 1 sec
                    val name = FileManager.getFileName(context, it)
                    withContext(Dispatchers.Main) {
                        inputVideoFile = temp
                        inputVideoName = name
                        videoMetadata = meta
                        thumbnailBitmap = thumb
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
                title = "Compress Video",
                subtitle = "वीडियो साइज़ छोटा करें",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (inputVideoFile != null) {
                        IconButton(onClick = { videoPicker.launch("video/*") }) {
                            Icon(Icons.Default.VideoCall, contentDescription = "Change Video")
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
            if (inputVideoFile == null) {
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
                                    .background(AccentVideo.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Compress,
                                    contentDescription = null,
                                    tint = AccentVideo,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select Video to Compress",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "व्हाट्सएप या शेयरिंग के लिए वीडियो साइज़ घटाएं",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { videoPicker.launch("video/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose Video / वीडियो चुनें")
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
                    // Video Card with Thumbnail and Metadata
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (thumbnailBitmap != null) {
                                    Image(
                                        bitmap = thumbnailBitmap!!.asImageBitmap(),
                                        contentDescription = "Thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inputVideoName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                videoMetadata?.let { meta ->
                                    Text(
                                        text = "Duration: ${VideoEngine.formatDuration(meta.durationMs)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Resolution: ${meta.width} × ${meta.height}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Size: ${FileManager.formatFileSize(meta.fileSizeBytes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Choose Compression Preset / क्वालिटी चुनें:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Cards: Small, Balanced, High Quality
                    VideoEngine.CompressionPreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        val estimatedBytes = videoMetadata?.let {
                            VideoEngine.estimateCompressedSize(it.durationMs, preset.targetBitrateBps)
                        } ?: 0L

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { selectedPreset = preset }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPreset = preset }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.label,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = preset.hindiLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Target: ${preset.maxDimension}p • Est: ~${FileManager.formatFileSize(estimatedBytes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Start Compression Button
                    Button(
                        onClick = {
                            isProcessing = true
                            processingProgress = 0f
                            val job = coroutineScope.launch(Dispatchers.IO) {
                                val out = FileManager.createOutputFile(context, "compressed_${inputVideoName}", "mp4")
                                val success = VideoEngine.processVideo(
                                    context = context,
                                    inputFile = inputVideoFile!!,
                                    outputFile = out,
                                    preset = selectedPreset,
                                    onProgress = { p -> processingProgress = p }
                                )
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    if (success) {
                                        resultFile = out
                                        savedPercent = FileManager.calculatePercentageSaved(
                                            inputVideoFile!!.length(),
                                            out.length()
                                        )
                                        historyRepo.addHistoryItem(
                                            toolName = "Compress Video",
                                            inputFileName = inputVideoName,
                                            outputFileName = out.name,
                                            outputFilePath = out.absolutePath,
                                            fileSizeBytes = out.length(),
                                            mimeType = "video/mp4"
                                        )
                                    } else {
                                        Toast.makeText(context, "Video compression encountered an error. Please try another preset.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            activeJob = job
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Compress, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compress Video Now / कंप्रेस करें", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(
            statusText = "Transcoding & compressing video…",
            progress = if (processingProgress > 0f) processingProgress else null,
            onCancel = {
                activeJob?.cancel()
                isProcessing = false
            }
        )
    }

    resultFile?.let { file ->
        ResultDialog(
            fileName = file.name,
            fileSizeStr = FileManager.formatFileSize(file.length()),
            savedPercentage = savedPercent,
            onOpen = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "video/mp4")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Play Video"))
            },
            onShare = {
                val uri = FileManager.getShareableUri(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/mp4"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Compressed Video"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
