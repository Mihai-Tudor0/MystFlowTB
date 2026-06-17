package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalClipboard
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
import com.example.mystflowtb.data.model.Card as BankCard
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

    val cards by bankingViewModel.cards.collectAsState()
    val selectedCardIndex by bankingViewModel.selectedCardIndex.collectAsState()
    val transactions by bankingViewModel.transactions.collectAsState()

    val currentBalance = if (cards.isNotEmpty() && selectedCardIndex < cards.size) {
        cards[selectedCardIndex].balance
    } else {
        0.0
    }

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
                            Icons.AutoMirrored.Filled.Logout,
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
                    text = formatBalance(currentBalance),
                    color = roseGold,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ================= CARDS CAROUSEL =================
            item {
                Spacer(modifier = Modifier.height(24.dp))
                if (cards.isEmpty()) {
                    CircularProgressIndicator(color = roseGold, modifier = Modifier.align(Alignment.Center))
                } else {
                    val pagerState = rememberPagerState(
                        initialPage = selectedCardIndex,
                        pageCount = { cards.size + 1 }
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        if (pagerState.currentPage < cards.size) {
                            bankingViewModel.setSelectedCardIndex(pagerState.currentPage)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        if (page < cards.size) {
                            FlipCardView(
                                card = cards[page],
                                firstName = firstName,
                                lastName = lastName,
                                roseGold = roseGold
                            )
                        } else {
                            AddCardView(
                                onAddCard = { bankingViewModel.addNewCard() },
                                cardBackground = cardBackground,
                                roseGold = roseGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(cards.size + 1) { iteration ->
                            val color = if (pagerState.currentPage == iteration) roseGold else Color.Gray.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .background(color, RoundedCornerShape(50))
                            )
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
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        enabled = cards.isNotEmpty() && selectedCardIndex < cards.size
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Alimentează",
                            tint = emeraldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Alimentează",
                            color = emeraldDeep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Transfer
                    Button(
                        onClick = onNavigateToTransfer,
                        colors = ButtonDefaults.buttonColors(containerColor = cardBackground),
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        enabled = cards.isNotEmpty() && selectedCardIndex < cards.size
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Transferă",
                            tint = roseGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Transferă",
                            color = roseGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                Icons.AutoMirrored.Filled.Send,
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
fun FlipCardView(
    card: BankCard,
    firstName: String,
    lastName: String,
    roseGold: Color
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flipAnimation"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { flipped = !flipped }
    ) {
        if (rotation <= 90f) {
            // Front of card
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
                        text = maskCardNumber(card.cardNumber),
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
                        Text(
                            text = "Apasă pentru detalii",
                            color = roseGold.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // Back of card
            val clipboardManager = LocalClipboardManager.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF001F15),
                                Color(0xFF003D2E)
                            )
                        )
                    )
                    .padding(top = 16.dp, bottom = 24.dp)
                    .graphicsLayer {
                        rotationY = 180f // Un-mirror the text
                    }
            ) {
                Column {
                    // Magnetic stripe
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp).background(Color.Black.copy(alpha = 0.8f)))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatCardNumber(card.cardNumber),
                                color = Color.White,
                                fontSize = 15.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(card.cardNumber)) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copiază nr. card",
                                    tint = roseGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("EXP", color = Color.Gray, fontSize = 10.sp)
                                Text(card.expiryDate, color = Color.White, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("CVV", color = Color.Gray, fontSize = 10.sp)
                                Text(card.cvv, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCardView(
    onAddCard: () -> Unit,
    cardBackground: Color,
    roseGold: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackground.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp)
            .clickable(onClick = onAddCard)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.AddCircleOutline,
                    contentDescription = "Adaugă card",
                    tint = roseGold,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cere un card nou", color = roseGold, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

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
    
    val titleText = when {
        isTopUp -> "Alimentare cont"
        isReceived -> transaction.senderName ?: "Transfer primit"
        else -> transaction.receiverName ?: "Transfer trimis"
    }

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
                        isReceived -> Icons.AutoMirrored.Filled.CallReceived
                        else -> Icons.AutoMirrored.Filled.CallMade
                    },
                    contentDescription = null,
                    tint = if (isReceived || isTopUp) Color(0xFF4CAF50) else Color(0xFFEF5350),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = titleText,
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

fun formatBalance(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("ro").setRegion("RO").build()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "${formatter.format(amount)} RON"
}

fun formatCardNumber(cardNumber: String): String {
    val clean = cardNumber.replace(" ", "")
    return clean.chunked(4).joinToString(" ")
}

fun maskCardNumber(cardNumber: String): String {
    val clean = cardNumber.replace(" ", "")
    if (clean.length < 4) return clean
    val lastFour = clean.takeLast(4)
    return "**** **** **** $lastFour"
}