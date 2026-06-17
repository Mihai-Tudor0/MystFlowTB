package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystflowtb.data.model.UserProfile
import com.example.mystflowtb.ui.viewmodel.BankingUiState
import com.example.mystflowtb.ui.viewmodel.BankingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    bankingViewModel: BankingViewModel,
    userProfile: UserProfile?,
    onBack: () -> Unit
) {
    val roseGold = Color(0xFFD4A77D)
    val emeraldDeep = Color(0xFF00382B)
    val cardBackground = Color(0xFF002920)

    val bankingState by bankingViewModel.bankingState.collectAsState()
    val recipientLookup by bankingViewModel.recipientLookup.collectAsState()
    
    val cards by bankingViewModel.cards.collectAsState()
    val selectedCardIndex by bankingViewModel.selectedCardIndex.collectAsState()
    val selectedCard = if (cards.isNotEmpty() && selectedCardIndex < cards.size) cards[selectedCardIndex] else null

    var recipientCardNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val balance = selectedCard?.balance ?: 0.0

    // Look up recipient as the user types
    LaunchedEffect(recipientCardNumber) {
        val digits = recipientCardNumber.replace(" ", "")
        if (digits.length == 16) {
            bankingViewModel.lookupRecipient(digits)
        } else {
            bankingViewModel.clearRecipientLookup()
        }
    }

    // Navigate back on success after a short delay
    LaunchedEffect(bankingState) {
        if (bankingState is BankingUiState.Success) {
            kotlinx.coroutines.delay(1500)
            bankingViewModel.resetState()
            bankingViewModel.clearRecipientLookup()
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
                bankingViewModel.clearRecipientLookup()
                onBack()
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Înapoi",
                    tint = roseGold
                )
            }
            Text(
                text = "Transfer",
                color = roseGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ================= BALANCE REMINDER =================
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Balanță card curent",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = formatBalance(balance),
                        color = roseGold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = roseGold.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ================= RECIPIENT CARD INPUT =================
        Text(
            text = "Card destinatar",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = recipientCardNumber,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                if (digitsOnly.length <= 16) {
                    recipientCardNumber = digitsOnly.chunked(4).joinToString(" ")
                }
            },
            label = { Text("Număr card destinatar") },
            placeholder = { Text("4821 XXXX XXXX XXXX") },
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
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = roseGold.copy(alpha = 0.7f)
                )
            }
        )

        // ================= RECIPIENT PREVIEW =================
        Spacer(modifier = Modifier.height(8.dp))
        if (recipientLookup != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF143D1A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Destinatar: ${recipientLookup!!.firstName} ${recipientLookup!!.lastName}",
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            val digits = recipientCardNumber.replace(" ", "")
            if (digits.length == 16) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3D3014)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Card negăsit. Verifică numărul.",
                            color = Color(0xFFFFC107),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ================= AMOUNT INPUT =================
        Text(
            text = "Sumă transfer (RON)",
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { input ->
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

        // Insufficient funds warning
        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
        if (parsedAmount > balance && parsedAmount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ Fonduri insuficiente",
                color = Color(0xFFFF6B6B),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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

        // ================= TRANSFER BUTTON =================
        val recipientDigits = recipientCardNumber.replace(" ", "")
        val isValid = recipientDigits.length == 16 &&
                parsedAmount > 0 &&
                parsedAmount <= balance &&
                recipientLookup != null &&
                selectedCard != null

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
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = emeraldDeep,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "TRANSFERĂ",
                    color = emeraldDeep,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }

    // ================= CONFIRM DIALOG =================
    if (showConfirmDialog && recipientLookup != null) {
        val parsedAmountForDialog = amount.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Text(
                    "Confirmare transfer",
                    color = roseGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Transferi ${formatBalance(parsedAmountForDialog)} către:",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${recipientLookup!!.firstName} ${recipientLookup!!.lastName}",
                        color = roseGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Card: ${formatCardNumber(recipientLookup!!.cardNumber)}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Balanța după transfer: ${formatBalance(balance - parsedAmountForDialog)}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            },
            containerColor = cardBackground,
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    selectedCard?.id?.let { cardId ->
                        bankingViewModel.transfer(cardId, recipientCardNumber, parsedAmountForDialog)
                    }
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
