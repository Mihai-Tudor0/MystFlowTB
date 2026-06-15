package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystflowtb.R // Asigură-te că acest import se potrivește cu numele pachetului tău

@Composable
fun WelcomeScreen(
    onNextClicked: () -> Unit
) {
    val emeraldDeep = Color(0xFF00382B)
    val roseGold = Color(0xFFD4A77D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Secțiunea de sus: Logo și Titlu ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp)
        ) {
            // Sigla ta pusă într-un cerc
            Image(
                painter = painterResource(id = R.drawable.mystflow_logo),
                contentDescription = "MystFlow Logo",
                modifier = Modifier
                    .size(240.dp)                 // Mărim puțin dimensiunea, deoarece decuparea va tăia marginile
                    .clip(CircleShape),           // Tăiem colțurile albe într-un cerc perfect
                contentScale = ContentScale.Crop  // OBLIGATORIU: Crop va face imaginea să umple cercul și va elimina fundalul exterior
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to MystFlow !",
                color = roseGold,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- Secțiunea de jos: Butonul Next ---
        Button(
            onClick = onNextClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = roseGold
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "NEXT",
                color = emeraldDeep,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}