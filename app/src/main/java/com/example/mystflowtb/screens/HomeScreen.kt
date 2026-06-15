package com.example.mystflowtb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    val roseGold = Color(0xFFD4A77D)
    val emeraldDeep = Color(0xFF00382B)

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = roseGold,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Te-ai autentificat cu succes",
                color = roseGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}