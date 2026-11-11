package com.ankit.familytools.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AnkitG Family Tools - Signature Drawing Canvas
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Real-time finger drawing with stroke points, save name field, and high-quality bitmap capture.
 */

data class StrokeLine(
    val points: List<Offset>,
    val color: Color = Color(0xFF1E3A8A),
    val strokeWidth: Float = 8f
)

@Composable
fun SignatureDrawingCanvas(
    modifier: Modifier = Modifier,
    initialName: String = "My Signature",
    strokeColor: Color = Color(0xFF1E3A8A),
    strokeWidth: Float = 8f,
    onExportReady: (Bitmap, String, Boolean) -> Unit
) {
    val strokes = remember { mutableStateListOf<StrokeLine>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }

    var sigName by remember { mutableStateOf(initialName) }
    var saveForFuture by remember { mutableStateOf(true) }

    fun captureBitmap(widthPx: Int = 800, heightPx: Int = 400): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)

        val paint = AndroidPaint().apply {
            isAntiAlias = true
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            color = android.graphics.Color.rgb(30, 58, 138)
            this.strokeWidth = strokeWidth
        }

        strokes.forEach { line ->
            if (line.points.size > 1) {
                val path = AndroidPath().apply {
                    moveTo(line.points.first().x, line.points.first().y)
                    for (i in 1 until line.points.size) {
                        lineTo(line.points[i].x, line.points[i].y)
                    }
                }
                canvas.drawPath(path, paint)
            }
        }

        return bitmap
    }

    Column(modifier = modifier) {
        // Name Input & Save Checkbox
        OutlinedTextField(
            value = sigName,
            onValueChange = { sigName = it },
            label = { Text("Signature Name / नाम (e.g. Papa's Sign)", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = saveForFuture, onCheckedChange = { saveForFuture = it })
            Text("Save for future use / भविष्य के लिए सहेजें", fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Drawing Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentPoints.size > 1) {
                                strokes.add(StrokeLine(currentPoints.toList(), strokeColor, strokeWidth))
                            }
                            currentPoints.clear()
                        },
                        onDragCancel = {
                            currentPoints.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                strokes.forEach { line ->
                    if (line.points.size > 1) {
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(line.points.first().x, line.points.first().y)
                            for (i in 1 until line.points.size) {
                                lineTo(line.points[i].x, line.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = line.color,
                            style = Stroke(width = line.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                if (currentPoints.size > 1) {
                    val activePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = activePath,
                        color = strokeColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            if (strokes.isEmpty() && currentPoints.isEmpty()) {
                Text(
                    text = "Sign with finger here\nयहाँ अपनी उंगली से दस्तखत करें",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Undo, Clear, Apply
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                OutlinedButton(
                    onClick = {
                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.size - 1)
                    },
                    enabled = strokes.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { strokes.clear() },
                    enabled = strokes.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            Button(
                onClick = {
                    if (strokes.isNotEmpty()) {
                        val bmp = captureBitmap()
                        onExportReady(bmp, sigName, saveForFuture)
                    }
                },
                enabled = strokes.isNotEmpty(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Use Signature / लगाएं", fontWeight = FontWeight.Bold)
            }
        }
    }
}
