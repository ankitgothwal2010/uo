package com.ankit.familytools.ui.screens.video

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.ankit.familytools.engine.video.VideoEngine
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.components.ProcessingDialog
import com.ankit.familytools.ui.components.ResultDialog
import com.ankit.familytools.ui.theme.AccentVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Video Actions Screen (Trim, Mute, Extract Audio, Extract Frame)
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

@Composable
fun VideoActionScreen(
    action: String = "trim",
    historyRepo: HistoryRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputVideoFile by remember { mutableStateOf<File?>(null) }
    var inputVideoName by remember { mutableStateOf("") }
    var videoMetadata by remember { mutableStateOf<VideoEngine.VideoMetadata?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Trim state
    var startMs by remember { mutableStateOf(0f) }
    var endMs by remember { mutableStateOf(10_000f) }

    // Frame extract state
    var frameTimeSec by remember { mutableStateOf(1f) }

    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val screenTitle = when (action) {
        "mute" -> "Mute Video / आवाज़ हटाएं"
        "extract_audio" -> "Extract Audio / ऑडियो निकालें"
        "extract_frame" -> "Extract Photo / फोटो निकालें"
        else -> "Trim Video / वीडियो काटें"
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                try {
                    val temp = FileManager.copyToTempFile(context, it, "video_act_")
                    val meta = VideoEngine.getVideoMetadata(temp)
                    val thumb = VideoEngine.extractFrameAtTime(temp, 1_000_000L)
                    val name = FileManager.getFileName(context, it)
                    withContext(Dispatchers.Main) {
                        inputVideoFile = temp
                        inputVideoName = name
                        videoMetadata = meta
                        previewBitmap = thumb
                        startMs = 0f
                        endMs = (meta.durationMs.toFloat()).coerceAtLeast(1000f)
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
                title = screenTitle,
                subtitle = "पारिवारिक वीडियो टूल",
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
                .padding(16.dp)
        ) {
            if (inputVideoFile == null) {
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
                                    .background(AccentVideo.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AccentVideo, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Select Video", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("वीडियो फाइल चुनें", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { videoPicker.launch("video/*") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
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
                    // Preview Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                previewBitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inputVideoName, fontWeight = FontWeight.Bold, maxLines = 1)
                                videoMetadata?.let { meta ->
                                    Text("Duration: ${VideoEngine.formatDuration(meta.durationMs)}", fontSize = 12.sp)
                                    Text("Size: ${FileManager.formatFileSize(meta.fileSizeBytes)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (action) {
                        "trim" -> {
                            val duration = videoMetadata?.durationMs?.toFloat() ?: 1000f
                            Text("Start Time: ${VideoEngine.formatDuration(startMs.toLong())}", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = startMs,
                                onValueChange = { if (it < endMs) startMs = it },
                                valueRange = 0f..duration
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("End Time: ${VideoEngine.formatDuration(endMs.toLong())}", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = endMs,
                                onValueChange = { if (it > startMs) endMs = it },
                                valueRange = 0f..duration
                            )

                            Text(
                                text = "Selected Trim Duration: ${VideoEngine.formatDuration((endMs - startMs).toLong())}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val out = FileManager.createOutputFile(context, "trimmed_${inputVideoName}", "mp4")
                                        val success = VideoEngine.processVideo(
                                            context = context,
                                            inputFile = inputVideoFile!!,
                                            outputFile = out,
                                            startMs = startMs.toLong(),
                                            endMs = endMs.toLong(),
                                            onProgress = {}
                                        )
                                        withContext(Dispatchers.Main) {
                                            isProcessing = false
                                            if (success) {
                                                resultFile = out
                                                historyRepo.addHistoryItem("Trim Video", inputVideoName, out.name, out.absolutePath, out.length(), "video/mp4")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trim Video / वीडियो काटें", fontWeight = FontWeight.Bold)
                            }
                        }

                        "mute" -> {
                            Text("Remove all audio tracks and background sounds from this video.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val out = FileManager.createOutputFile(context, "muted_${inputVideoName}", "mp4")
                                        val success = VideoEngine.processVideo(
                                            context = context,
                                            inputFile = inputVideoFile!!,
                                            outputFile = out,
                                            muteAudio = true,
                                            onProgress = {}
                                        )
                                        withContext(Dispatchers.Main) {
                                            isProcessing = false
                                            if (success) {
                                                resultFile = out
                                                historyRepo.addHistoryItem("Mute Video", inputVideoName, out.name, out.absolutePath, out.length(), "video/mp4")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.VolumeOff, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mute Audio & Save / आवाज़ बंद करें", fontWeight = FontWeight.Bold)
                            }
                        }

                        "extract_audio" -> {
                            Text("Extract audio soundtrack as an AAC / M4A audio file.", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    isProcessing = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val out = FileManager.createOutputFile(context, "audio_${inputVideoName.substringBeforeLast('.')}", "m4a")
                                        val success = VideoEngine.processVideo(
                                            context = context,
                                            inputFile = inputVideoFile!!,
                                            outputFile = out,
                                            extractAudioOnly = true,
                                            onProgress = {}
                                        )
                                        withContext(Dispatchers.Main) {
                                            isProcessing = false
                                            if (success) {
                                                resultFile = out
                                                historyRepo.addHistoryItem("Extract Audio", inputVideoName, out.name, out.absolutePath, out.length(), "audio/mp4")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Extract Audio File / ऑडियो निकालें", fontWeight = FontWeight.Bold)
                            }
                        }

                        "extract_frame" -> {
                            val totalSec = ((videoMetadata?.durationMs ?: 1000L) / 1000f)
                            Text("Select Timestamp: ${VideoEngine.formatDuration((frameTimeSec * 1000).toLong())}", fontWeight = FontWeight.SemiBold)
                            Slider(
                                value = frameTimeSec,
                                onValueChange = {
                                    frameTimeSec = it
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val f = VideoEngine.extractFrameAtTime(inputVideoFile!!, (it * 1_000_000).toLong())
                                        withContext(Dispatchers.Main) {
                                            if (f != null) previewBitmap = f
                                        }
                                    }
                                },
                                valueRange = 0f..totalSec
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (previewBitmap != null) {
                                        isProcessing = true
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val out = FileManager.createOutputFile(context, "frame_${inputVideoName}", "jpg")
                                            val success = ImageEngine.saveBitmap(previewBitmap!!, out, 95)
                                            withContext(Dispatchers.Main) {
                                                isProcessing = false
                                                if (success) {
                                                    resultFile = out
                                                    historyRepo.addHistoryItem("Extract Frame", inputVideoName, out.name, out.absolutePath, out.length(), "image/jpeg")
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Photo Frame / फोटो सेव करें", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (isProcessing) {
        ProcessingDialog(statusText = "Processing video…")
    }

    resultFile?.let { file ->
        ResultDialog(
            fileName = file.name,
            fileSizeStr = FileManager.formatFileSize(file.length()),
            onOpen = {
                val uri = FileManager.getShareableUri(context, file)
                val mime = if (file.extension == "m4a") "audio/*" else if (file.extension == "jpg") "image/*" else "video/*"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mime)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open File"))
            },
            onShare = {
                val uri = FileManager.getShareableUri(context, file)
                val mime = if (file.extension == "m4a") "audio/*" else if (file.extension == "jpg") "image/*" else "video/*"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share File"))
            },
            onDismiss = { resultFile = null }
        )
    }
}
