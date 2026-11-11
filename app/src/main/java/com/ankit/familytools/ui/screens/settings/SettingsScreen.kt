package com.ankit.familytools.ui.screens.settings
import androidx.compose.foundation.BorderStroke

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.ankit.familytools.data.repository.SignatureRepository
import com.ankit.familytools.engine.file.FileManager
import com.ankit.familytools.ui.components.CommonTopBar
import com.ankit.familytools.ui.theme.PrimaryBlue
import com.ankit.familytools.ui.theme.SuccessGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * AnkitG Family Tools - Settings & About Screen
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 */

@Composable
fun SettingsScreen(
    signatureRepo: SignatureRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val savedSignatures by signatureRepo.signaturesFlow.collectAsState()

    var cacheSizeBytes by remember { mutableStateOf(0L) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    fun calculateCache() {
        coroutineScope.launch(Dispatchers.IO) {
            var size = 0L
            context.cacheDir.walkTopDown().forEach {
                if (it.isFile) size += it.length()
            }
            withContext(Dispatchers.Main) {
                cacheSizeBytes = size
            }
        }
    }

    LaunchedEffect(Unit) {
        calculateCache()
    }

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Settings & About",
                subtitle = "सेटिंग्स और जानकारी",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // App Branding Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "AnkitG Family Tools",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0.0 • Native Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Developed by Ankit Gothwal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Copyright ©️ 2026 Ankit Gothwal. All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline & Privacy Guarantee Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "No internet required. Your family photos, documents, and videos never leave this phone.\nआपका डेटा पूरी तरह निजी है।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage & Cache Section
            Text(
                text = "Storage & Cache / स्टोरेज",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Temporary Files Cache", fontWeight = FontWeight.Medium)
                        Text(
                            "Size: ${FileManager.formatFileSize(cacheSizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                FileManager.cleanOldCache(context)
                                calculateCache()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Temporary cache cleared! / कैशे साफ़ हो गया", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clean Cache")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Signatures Management
            Text(
                text = "Saved Signatures / सहेजे गए हस्ताक्षर (${savedSignatures.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (savedSignatures.isEmpty()) {
                Text(
                    text = "No saved signatures yet. They will appear here once drawn in Sign PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                ) {
                    items(savedSignatures) { sig ->
                        val bmp = remember(sig.id) { signatureRepo.loadBitmap(sig) }
                        if (bmp != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(140.dp).fillMaxHeight(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = sig.name,
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentScale = ContentScale.Fit
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sig.name, fontSize = 11.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                        IconButton(
                                            onClick = { signatureRepo.deleteSignature(sig.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open Source Acknowledgements
            OutlinedButton(
                onClick = { showLicensesDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Source Licenses / थर्ड-पार्टी लाइसेंस")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Notices") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "AnkitG Family Tools incorporates the following free and open-source libraries:\n\n" +
                                "• Apache PDFBox-Android (Apache License 2.0)\n" +
                                "  Copyright 2018-2023 Tom Roush\n\n" +
                                "• AndroidX & Jetpack Compose (Apache License 2.0)\n" +
                                "  Copyright The Android Open Source Project\n\n" +
                                "• AndroidX Media3 Transformer & ExoPlayer (Apache License 2.0)\n" +
                                "  Copyright The Android Open Source Project\n\n" +
                                "• Google ML Kit Text Recognition (Apache License 2.0)\n" +
                                "  Copyright Google LLC\n\n" +
                                "• Coil Image Loader (Apache License 2.0)\n" +
                                "  Copyright 2023 Coil Contributors\n\n" +
                                "Application code and architecture:\n" +
                                "Developed by Ankit Gothwal\n" +
                                "Copyright ©️ 2026 Ankit Gothwal. All rights reserved.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) { Text("Close") }
            }
        )
    }
}
