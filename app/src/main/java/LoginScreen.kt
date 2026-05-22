package com.example.mystflowtb.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
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
fun LoginScreen(
    authMethod: String,
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)

    // State-uri pentru ambele metode
    var loginInput by remember { mutableStateOf("") }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }
    var containerSize by remember { mutableStateOf(0f) }

    // Curățăm input-ul la schimbarea metodei
    LaunchedEffect(authMethod) { loginInput = "" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(emeraldDeep)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. TITLU ---
        Text(
            text = "Authentification in MystFlowTB",
            color = roseGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 40.dp)
        )

        // --- 2. ZONA DE INTERACȚIUNE ---
        if (authMethod == "PIN") {
            // ================= ECRAN PIN =================
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Indicatori 6 puncte
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    repeat(6) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .border(2.dp, roseGold, CircleShape)
                                .background(if (index < loginInput.length) roseGold else Color.Transparent, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Tastatura numerică completă
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "delete")
                )

                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { char ->
                            IconButton(
                                onClick = {
                                    when (char) {
                                        "delete" -> if (loginInput.isNotEmpty()) loginInput = loginInput.dropLast(1)
                                        "C" -> loginInput = ""
                                        else -> if (loginInput.length < 6) {
                                            loginInput += char
                                            if (loginInput.length == 6) onLoginSuccess()
                                        }
                                    }
                                },
                                modifier = Modifier.size(70.dp)
                            ) {
                                if (char == "delete") Icon(Icons.Default.Delete, null, tint = roseGold)
                                else if (char == "C") Text("C", color = roseGold, fontSize = 24.sp)
                                else Text(text = char, color = Color.White, fontSize = 28.sp)
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
                        val touchPoint = Offset(event.x, event.y)
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                loginInput = ""
                                currentTouchPoint = touchPoint
                            }
                            MotionEvent.ACTION_MOVE -> {
                                currentTouchPoint = touchPoint
                                if (containerSize > 0f) {
                                    val spacing = containerSize / 3
                                    val margin = spacing / 2
                                    val hitRadius = spacing / 3
                                    for (i in 0..8) {
                                        val dotPos = Offset(margin + (i % 3) * spacing, margin + (i / 3) * spacing)
                                        val dist = sqrt((touchPoint.x - dotPos.x).pow(2) + (touchPoint.y - dotPos.y).pow(2))
                                        if (dist < hitRadius && !loginInput.contains(i.toString())) {
                                            loginInput += i.toString()
                                        }
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                if (loginInput.length >= 4) onLoginSuccess()
                                loginInput = ""
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
                    val dotPositions = (0..8).map { i -> Offset(margin + (i % 3) * spacing, margin + (i / 3) * spacing) }

                    // Desenare linii
                    val selectedIndices = loginInput.map { it.toString().toInt() }
                    if (selectedIndices.isNotEmpty()) {
                        for (i in 0 until selectedIndices.size - 1) {
                            drawLine(roseGold.copy(0.6f), dotPositions[selectedIndices[i]], dotPositions[selectedIndices[i + 1]], 8.dp.toPx(), StrokeCap.Round)
                        }
                        currentTouchPoint?.let { drawLine(roseGold.copy(0.4f), dotPositions[selectedIndices.last()], it, 8.dp.toPx(), StrokeCap.Round) }
                    }

                    // Desenare buline
                    dotPositions.forEachIndexed { index, pos ->
                        val isSelected = loginInput.contains(index.toString())
                        drawCircle(if (isSelected) roseGold.copy(0.3f) else Color.White.copy(0.1f), 30.dp.toPx(), pos)
                        drawCircle(if (isSelected) roseGold else Color.White, 8.dp.toPx(), pos)
                    }
                }
            }
        }

        // --- 3. JOS: AMPRENTĂ ȘI NAVIGARE ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { /* Biometrie */ }) {
                Icon(Icons.Default.Fingerprint, null, tint = roseGold, modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? Register", color = roseGold.copy(0.8f))
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}