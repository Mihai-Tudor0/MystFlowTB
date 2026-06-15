package com.example.mystflowtb.screens

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SetupScreen(
    method: String,
    onFinished: (String) -> Unit
) {
    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)

    // --- STATE PIN ---
    var pinInput by remember { mutableStateOf("") }

    // --- STATE PATTERN ---
    var selectedDots by remember { mutableStateOf(listOf<Int>()) }
    var lastValidPattern by remember { mutableStateOf("") }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }
    var containerSize by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TITLU
        Text(
            text = if (method == "PIN") "Set your 6 digits password" else "Sel your pattern",
            color = roseGold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )

        // --- ZONA DE INPUT DINAMICĂ ---
        if (method == "PIN") {
            // ================= ECRAN PIN =================
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Vizualizare puncte PIN
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(2.dp, roseGold, CircleShape)
                                .background(if (index < pinInput.length) roseGold else Color.Transparent, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Tastatură Numerică
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "delete")
                )

                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { char ->
                            IconButton(
                                onClick = {
                                    when (char) {
                                        "delete" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                        "C" -> pinInput = ""
                                        else -> if (pinInput.length < 6) pinInput += char
                                    }
                                },
                                modifier = Modifier.size(75.dp)
                            ) {
                                if (char == "delete") {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = roseGold)
                                } else {
                                    Text(text = char, color = Color.White, fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ================= ECRAN PATTERN =================
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .pointerInteropFilter { event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                selectedDots = emptyList()
                                currentTouchPoint = Offset(event.x, event.y)
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val touchPoint = Offset(event.x, event.y)
                                currentTouchPoint = touchPoint
                                val dotIndex = getDotAtPosition(touchPoint, containerSize)
                                if (dotIndex != null && !selectedDots.contains(dotIndex)) {
                                    selectedDots = selectedDots + dotIndex
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                if (selectedDots.size >= 4) {
                                    lastValidPattern = selectedDots.joinToString("")
                                }
                                selectedDots = emptyList()
                                currentTouchPoint = null
                            }
                        }
                        true
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    containerSize = size.width
                    val spacing = size.width / 3
                    val margin = spacing / 2
                    val dotPositions = (0..8).map { i ->
                        Offset(margin + (i % 3) * spacing, margin + (i / 3) * spacing)
                    }

                    // Desenare linii selectate
                    if (selectedDots.isNotEmpty()) {
                        for (i in 0 until selectedDots.size - 1) {
                            drawLine(
                                color = roseGold.copy(0.6f),
                                start = dotPositions[selectedDots[i]],
                                end = dotPositions[selectedDots[i+1]],
                                strokeWidth = 8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        // Linia elastică către deget
                        currentTouchPoint?.let {
                            drawLine(
                                color = roseGold.copy(0.4f),
                                start = dotPositions[selectedDots.last()],
                                end = it,
                                strokeWidth = 8.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Desenare buline (Nucleu + Halo)
                    dotPositions.forEachIndexed { index, pos ->
                        val isSelected = selectedDots.contains(index)
                        drawCircle(
                            color = if (isSelected) roseGold.copy(0.3f) else Color.White.copy(0.1f),
                            radius = 30.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = if (isSelected) roseGold else Color.White,
                            radius = 8.dp.toPx(),
                            center = pos
                        )
                    }
                }
            }
        }

        // --- BUTON SALVARE ---
        Button(
            onClick = {
                if (method == "PIN") onFinished(pinInput) else onFinished(lastValidPattern)
            },
            enabled = if (method == "PIN") pinInput.length == 6 else lastValidPattern.length >= 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = roseGold,
                disabledContainerColor = roseGold.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "SAVE",
                color = emeraldDeep,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// Funcție pentru detectarea bulinei (Rămâne neschimbată)
fun getDotAtPosition(touch: Offset, containerSize: Float): Int? {
    val spacing = containerSize / 3
    val margin = spacing / 2
    val hitRadius = spacing / 3
    for (i in 0..8) {
        val dotPos = Offset(margin + (i % 3) * spacing, margin + (i / 3) * spacing)
        val distance = sqrt((touch.x - dotPos.x).pow(2) + (touch.y - dotPos.y).pow(2))
        if (distance < hitRadius) return i
    }
    return null
}