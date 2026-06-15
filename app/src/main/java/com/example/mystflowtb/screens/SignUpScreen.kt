package com.example.mystflowtb.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpScreen(onMethodSelected: (String) -> Unit) {
    // Starea care reține ce buton am apăsat
    var tempSelection by remember { mutableStateOf("") }

    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose your security method",
            color = roseGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Buton Opțiune: Parolă 6 cifre
        OutlinedButton(
            onClick = { tempSelection = "PIN" },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (tempSelection == "PIN") roseGold.copy(alpha = 0.1f) else Color.Transparent,
                contentColor = roseGold
            ),
            border = BorderStroke(
                width = if (tempSelection == "PIN") 3.dp else 1.dp,
                color = roseGold
            )
        ) {
            Text("6 digits passowrd", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buton Opțiune: Lock Pattern
        OutlinedButton(
            onClick = { tempSelection = "Pattern" },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (tempSelection == "Pattern") roseGold.copy(alpha = 0.1f) else Color.Transparent,
                contentColor = roseGold
            ),
            border = BorderStroke(
                width = if (tempSelection == "Pattern") 3.dp else 1.dp,
                color = roseGold
            )
        ) {
            Text("Lock Pattern", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Butonul final "Alege"
        Button(
            onClick = { if (tempSelection.isNotEmpty()) onMethodSelected(tempSelection) },
            enabled = tempSelection.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = roseGold,
                disabledContainerColor = roseGold.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Choose", color = emeraldDeep, fontWeight = FontWeight.ExtraBold)
        }
    }
}