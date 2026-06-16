package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystflowtb.ui.viewmodel.BankingUiState
import com.example.mystflowtb.ui.viewmodel.BankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    bankingViewModel: BankingViewModel,
    onBack: () -> Unit
) {
    val roseGold = Color(0xFFD4A77D)
    val emeraldDeep = Color(0xFF00382B)
    val cardBackground = Color(0xFF002920)

    val bankingState by bankingViewModel.bankingState.collectAsState()

    var externalCardNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Navigate back on success after a short delay
    LaunchedEffect(bankingState) {
        if (bankingState is BankingUiState.Success) {
            kotlinx.coroutines.delay(1500)
            bankingViewModel.resetState()
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // ================= TOP BAR =================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                bankingViewModel.resetState()
                onBack()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Înapoi",
                    tint = roseGold
                )
            }
            Text(
                text = "Alimentare cont",
                color = roseGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ================= INFO CARD =================
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = roseGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Introdu datele cardului extern și suma dorită",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ================= EXTERNAL CARD INPUT =================
        Text(
            text = "Card sursă (extern)",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = externalCardNumber,
            onValueChange = { input ->
                // Allow only digits, max 16
                val digitsOnly = input.filter { it.isDigit() }
                if (digitsOnly.length <= 16) {
                    // Format with spaces every 4 digits
                    externalCardNumber = digitsOnly.chunked(4).joinToString(" ")
                }
            },
            label = { Text("Număr card (16 cifre)") },
            placeholder = { Text("1234 5678 9012 3456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = roseGold,
                unfocusedBorderColor = roseGold.copy(alpha = 0.5f),
                focusedLabelColor = roseGold,
                unfocusedLabelColor = roseGold.copy(alpha = 0.6f),
                cursorColor = roseGold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            leadingIcon = {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = roseGold.copy(alpha = 0.7f)
                )
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ================= AMOUNT INPUT =================
        Text(
            text = "Sumă (RON)",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { input ->
                // Allow digits and one decimal point, max 2 decimal places
                val filtered = input.filter { it.isDigit() || it == '.' }
                val parts = filtered.split(".")
                if (parts.size <= 2 && (parts.getOrNull(1)?.length ?: 0) <= 2) {
                    amount = filtered
                }
            },
            label = { Text("Sumă") },
            placeholder = { Text("100.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = roseGold,
                unfocusedBorderColor = roseGold.copy(alpha = 0.5f),
                focusedLabelColor = roseGold,
                unfocusedLabelColor = roseGold.copy(alpha = 0.6f),
                cursorColor = roseGold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            leadingIcon = {
                Text(
                    "RON",
                    color = roseGold.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick amount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("50", "100", "250", "500").forEach { quickAmount ->
                OutlinedButton(
                    onClick = { amount = quickAmount },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = roseGold
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        // Use default border but with roseGold tint
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(quickAmount, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ================= STATUS MESSAGES =================
        when (bankingState) {
            is BankingUiState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D1414)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (bankingState as BankingUiState.Error).message,
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            is BankingUiState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF143D1A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = (bankingState as BankingUiState.Success).message,
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            else -> {}
        }

        // ================= TOP UP BUTTON =================
        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
        val externalDigits = externalCardNumber.replace(" ", "")
        val isValid = externalDigits.length == 16 && parsedAmount > 0

        Button(
            onClick = { showConfirmDialog = true },
            enabled = isValid && bankingState !is BankingUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = roseGold,
                disabledContainerColor = roseGold.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (bankingState is BankingUiState.Loading) {
                CircularProgressIndicator(
                    color = emeraldDeep,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = emeraldDeep,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ALIMENTEAZĂ CONT",
                    color = emeraldDeep,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }

    // ================= CONFIRM DIALOG =================
    if (showConfirmDialog) {
        val parsedAmountForDialog = amount.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    "Confirmare alimentare",
                    color = roseGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Alimentezi contul cu ${formatBalance(parsedAmountForDialog)} de pe cardul ****${externalCardNumber.replace(" ", "").takeLast(4)}?",
                    color = Color.White
                )
            },
            containerColor = cardBackground,
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    bankingViewModel.topUp(parsedAmountForDialog)
                }) {
                    Text("CONFIRMĂ", color = roseGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Anulează", color = Color.Gray)
                }
            }
        )
    }
}
