package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mystflowtb.AiViewModel
import com.example.mystflowtb.data.model.Transaction
import com.example.mystflowtb.data.model.UserProfile
import com.example.mystflowtb.ui.viewmodel.BankingViewModel
import java.text.NumberFormat
import java.util.Locale
import io.github.jan.supabase.auth.auth
import com.example.mystflowtb.data.remote.SupabaseProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    aiViewModel: AiViewModel,
    bankingViewModel: BankingViewModel,
    userProfile: UserProfile?,
    onNavigateToTopUp: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onLogout: () -> Unit
) {
    val roseGold = Color(0xFFD4A77D)
    val emeraldDeep = Color(0xFF00382B)
    val cardBackground = Color(0xFF002920)

    val firstName = userProfile?.firstName ?: "Utilizator"
    val lastName = userProfile?.lastName ?: ""
    val balance = userProfile?.balance ?: 0.0
    val cardNumber = userProfile?.cardNumber ?: "0000000000000000"

    val clipboardManager = LocalClipboardManager.current

    var showInsightDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var userChatMessage by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<Pair<String, Boolean>>() }
    var isWaitingForBot by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    val transactions by bankingViewModel.transactions.collectAsState()
    val currentUserId = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: ""
    // Load data on first composition
    LaunchedEffect(Unit) {
        if (currentUserId.isNotEmpty()) {
            aiViewModel.fetchInsight(userId = currentUserId)
        }
        bankingViewModel.loadTransactions()
       // showInsightDialog = true
    }

    LaunchedEffect(aiViewModel.chatResponse.value) {
        if (aiViewModel.chatResponse.value.isNotBlank()) {
            chatMessages.add(Pair(aiViewModel.chatResponse.value, false))
            isWaitingForBot = false
        }
    }

    LaunchedEffect(chatMessages.size, isWaitingForBot) {
        if (chatMessages.isNotEmpty() || isWaitingForBot) {
            val totalItems = chatMessages.size + (if (isWaitingForBot) 1 else 0)
            lazyListState.animateScrollToItem(totalItems - 1)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ================= HEADER: Greeting + Settings =================
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bună, $firstName! 👋",
                            color = roseGold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$firstName $lastName",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Deconectare",
                            tint = roseGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // ================= BALANCE =================
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(text = "Balanță disponibilă", color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatBalance(balance),
                    color = roseGold,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ================= CARD =================
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF003D2E),
                                        Color(0xFF001F15)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "MystFlow Premium",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = "Card",
                                    tint = roseGold
                                )
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                text = formatCardNumber(cardNumber),
                                color = Color.White,
                                fontSize = 20.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$firstName $lastName",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                // Copy card number button
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(cardNumber))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copiază nr. card",
                                        tint = roseGold.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= ACTION BUTTONS: Top Up + Transfer =================
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Up
                    Button(
                        onClick = onNavigateToTopUp,
                        colors = ButtonDefaults.buttonColors(containerColor = roseGold),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Alimentează",
                            tint = emeraldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Alimentează",
                            color = emeraldDeep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    // Transfer
                    Button(
                        onClick = onNavigateToTransfer,
                        colors = ButtonDefaults.buttonColors(containerColor = cardBackground),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Transferă",
                            tint = roseGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Transferă",
                            color = roseGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ================= AI SECTION =================
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Asistență Inteligență Artificială",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showInsightDialog = true
                            if (currentUserId.isNotEmpty()) {
                            aiViewModel.fetchInsight(userId = currentUserId)
                        }},
                        colors = ButtonDefaults.buttonColors(containerColor = cardBackground),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "Security",
                                tint = roseGold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Sfat Securitate",
                                color = Color.White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Button(
                        onClick = { showChatDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = roseGold),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat",
                                tint = emeraldDeep
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "MystBot Chat",
                                color = emeraldDeep,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ================= RECENT TRANSACTIONS =================
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "Activitate recentă",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nicio tranzacție încă. Alimentează contul sau primește un transfer!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(transactions.take(5)) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currentUserId = userProfile?.id,
                        roseGold = roseGold,
                        cardBackground = cardBackground,
                        emeraldDeep = emeraldDeep
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ================= AI INSIGHT DIALOG =================
    if (showInsightDialog) {
        AlertDialog(
            onDismissRequest = { showInsightDialog = false },
            title = {
                Text(
                    text = "Analiză Securitate AI 🛡️",
                    color = roseGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bună, $firstName! ${aiViewModel.insightMessage.value}",
                    color = Color.White
                )
            },
            containerColor = cardBackground,
            confirmButton = {
                TextButton(onClick = { showInsightDialog = false }) {
                    Text("Am înțeles", color = roseGold)
                }
            }
        )
    }

    // ================= CHAT DIALOG =================
    if (showChatDialog) {
        Dialog(onDismissRequest = { showChatDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Discută cu MystBot 🤖",
                            color = roseGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = { chatMessages.clear() }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Șterge",
                                    tint = Color(0xFFEF5350)
                                )
                            }
                            IconButton(onClick = { showChatDialog = false }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Închide",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF00382B),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (chatMessages.isEmpty() && !isWaitingForBot) {
                            Text(
                                text = "Sunt MystBot, asistentul tău. Întreabă-mă orice despre aplicație sau securitate!",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 24.dp)
                            )
                        } else {
                            LazyColumn(
                                state = lazyListState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(chatMessages.size) { index ->
                                    val (messageText, isUser) = chatMessages[index]
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isUser) roseGold else Color(0xFF00382B)
                                            ),
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                            ),
                                            modifier = Modifier.widthIn(max = 240.dp)
                                        ) {
                                            Text(
                                                text = messageText,
                                                color = if (isUser) Color(0xFF00382B) else Color.White,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }

                                if (isWaitingForBot) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFF00382B)
                                                ),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomEnd = 16.dp
                                                ),
                                                modifier = Modifier.widthIn(max = 240.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = roseGold,
                                                        strokeWidth = 2.dp
                                                    )
                                                    Text(
                                                        text = "MystBot tastează...",
                                                        color = Color.LightGray,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF00382B),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userChatMessage,
                            onValueChange = { userChatMessage = it },
                            placeholder = {
                                Text(
                                    "Scrie un mesaj...",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = roseGold,
                                unfocusedBorderColor = Color(0xFF00382B)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (userChatMessage.isNotBlank() && !isWaitingForBot) {
                                    val textTrimis = userChatMessage
                                    chatMessages.add(Pair(textTrimis, true))
                                    userChatMessage = ""
                                    isWaitingForBot = true

                                    // if(ID valid de la Supabase)
                                    if (currentUserId.isNotEmpty()) {
                                        aiViewModel.fetchChatResponse(
                                            message = textTrimis,
                                            userId = currentUserId
                                        )
                                    } else {
                                        // Fallback
                                        chatMessages.add(Pair("Eroare: Sesiune expirată.", false))
                                        isWaitingForBot = false
                                    }
                                }
                            },
                            containerColor = roseGold,
                            contentColor = Color(0xFF00382B),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Trimite",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================= HELPER COMPOSABLES =================

@Composable
private fun TransactionItem(
    transaction: Transaction,
    currentUserId: String?,
    roseGold: Color,
    cardBackground: Color,
    emeraldDeep: Color
) {
    val isReceived = transaction.receiverId == currentUserId
    val isTopUp = transaction.type == "top_up"

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        isTopUp -> Icons.Default.AccountBalanceWallet
                        isReceived -> Icons.Default.CallReceived
                        else -> Icons.Default.CallMade
                    },
                    contentDescription = null,
                    tint = if (isReceived || isTopUp) Color(0xFF4CAF50) else Color(0xFFEF5350),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when {
                            isTopUp -> "Alimentare cont"
                            isReceived -> "Transfer primit"
                            else -> "Transfer trimis"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = transaction.description ?: "",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "${if (isReceived || isTopUp) "+" else "-"}${formatBalance(transaction.amount)}",
                color = if (isReceived || isTopUp) Color(0xFF4CAF50) else Color(0xFFEF5350),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// ================= UTILITY FUNCTIONS =================

/**
 * Formats a balance value as "12,345.00 RON".
 */
fun formatBalance(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ro", "RO")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(amount)} RON"
}

/**
 * Formats a 16-digit card number as "4821 XXXX XXXX XXXX".
 */
fun formatCardNumber(cardNumber: String): String {
    val clean = cardNumber.replace(" ", "")
    return clean.chunked(4).joinToString(" ")
}

/**
 * Returns the masked version of a card number: "**** **** **** 1234".
 */
fun maskCardNumber(cardNumber: String): String {
    val clean = cardNumber.replace(" ", "")
    if (clean.length < 4) return clean
    val lastFour = clean.takeLast(4)
    return "**** **** **** $lastFour"
}