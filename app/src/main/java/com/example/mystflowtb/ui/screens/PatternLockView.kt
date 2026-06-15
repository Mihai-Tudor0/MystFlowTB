package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    onPatternCompleted: (List<Int>) -> Unit,
    color: Color = Color(0xFF0047AB)
) {
    // Lista de puncte selectate (0-8 pentru o matrice 3x3)
    var selectedPoints by remember { mutableStateOf(listOf<Int>()) }
    // Poziția actuală a degetului pentru a trasa linia "în timp real"
    var currentFingerPos by remember { mutableStateOf<Offset?>(null) }

    // Centrele celor 9 puncte (se calculează la desenare)
    val dotCenters = remember { mutableListOf<Offset>() }

    Canvas(
        modifier = Modifier
            .size(300.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedPoints = emptyList()
                        checkHit(offset, dotCenters)?.let { selectedPoints = listOf(it) }
                    },
                    onDrag = { change, _ ->
                        currentFingerPos = change.position
                        checkHit(change.position, dotCenters)?.let { hitIndex ->
                            if (hitIndex !in selectedPoints) {
                                selectedPoints = selectedPoints + hitIndex
                            }
                        }
                    },
                    onDragEnd = {
                        onPatternCompleted(selectedPoints)
                        currentFingerPos = null
                    }
                )
            }
    ) {
        val sizeSide = size.width
        val spacing = sizeSide / 4

        // Calculăm pozițiile punctelor (matrice 3x3)
        dotCenters.clear()
        for (row in 1..3) {
            for (col in 1..3) {
                dotCenters.add(Offset(col * spacing, row * spacing))
            }
        }

        // 1. Desenăm liniile dintre punctele selectate
        if (selectedPoints.isNotEmpty()) {
            for (i in 0 until selectedPoints.size - 1) {
                drawLine(
                    color = color.copy(alpha = 0.5f),
                    start = dotCenters[selectedPoints[i]],
                    end = dotCenters[selectedPoints[i + 1]],
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }
            // 2. Desenăm linia de la ultimul punct la deget
            currentFingerPos?.let { finger ->
                drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = dotCenters[selectedPoints.last()],
                    end = finger,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 3. Desenăm cele 9 puncte (cercuri)
        dotCenters.forEachIndexed { index, center ->
            val isSelected = index in selectedPoints
            drawCircle(
                color = if (isSelected) color else Color.LightGray,
                radius = if (isSelected) 25f else 15f,
                center = center
            )
        }
    }
}

// Funcție care verifică dacă degetul a atins un punct
private fun checkHit(offset: Offset, centers: List<Offset>): Int? {
    centers.forEachIndexed { index, center ->
        val distance = sqrt((offset.x - center.x).pow(2) + (offset.y - center.y).pow(2))
        if (distance < 60f) return index
    }
    return null
}