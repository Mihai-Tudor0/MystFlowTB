package com.example.mystflowtb.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mystflowtb.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AiViewModel) {
    val roseGold = Color(0xFFD4A77D)
    val emeraldDeep = Color(0xFF00382B)
    val cardBackground = Color(0xFF002920)

    var showInsightDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }
    var userChatMessage by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<Pair<String, Boolean>>() }
    var isWaitingForBot by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchInsight(userId = 1)
        showInsightDialog = true
    }

    LaunchedEffect(viewModel.chatResponse.value) {
        if (viewModel.chatResponse.value.isNotBlank()) {
            chatMessages.add(Pair(viewModel.chatResponse.value, false))
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
        modifier = Modifier
            .fillMaxSize()
            .background(emeraldDeep)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profil", tint = roseGold, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Contul meu", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                IconButton(onClick = { }) {
                    Icon(Icons.Default.Settings, contentDescription = "Setări", tint = roseGold, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Balanță disponibilă", color = Color.LightGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "14,250.00 RON", color = roseGold, fontSize = 36.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "MystFlow Premium", color = Color.White, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.CreditCard, contentDescription = "Card", tint = roseGold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "**** **** **** 8824", color = Color.White, fontSize = 18.sp, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = roseGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Vezi Card", color = emeraldDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(text = "Asistență Inteligență Artificială", color = Color.LightGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { showInsightDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = cardBackground),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = roseGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sfat Securitate", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }

                Button(
                    onClick = { showChatDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = roseGold),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = emeraldDeep)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("MystBot Chat", color = emeraldDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }

    if (showInsightDialog) {
        AlertDialog(
            onDismissRequest = { showInsightDialog = false },
            title = { Text(text = "Analiză Securitate AI 🛡️", color = roseGold, fontWeight = FontWeight.Bold) },
            text = { Text(text = viewModel.insightMessage.value, color = Color.White) },
            containerColor = cardBackground,
            confirmButton = {
                TextButton(onClick = { showInsightDialog = false }) {
                    Text("Am înțeles", color = roseGold)
                }
            }
        )
    }

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
                        Text(text = "Discută cu MystBot 🤖", color = roseGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row {
                            IconButton(onClick = { chatMessages.clear() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Șterge", tint = Color(0xFFEF5350))
                            }
                            IconButton(onClick = { showChatDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Închide", tint = Color.Gray)
                            }
                        }
                    }

                    Divider(color = Color(0xFF00382B), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (chatMessages.isEmpty() && !isWaitingForBot) {
                            Text(
                                text = "Sunt MystBot, asistentul tău. Întreabă-mă orice despre aplicație sau securitate!",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp)
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
                                                color = if (isUser) emeraldDeep else Color.White,
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
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF00382B)),
                                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp),
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

                    Divider(color = Color(0xFF00382B), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userChatMessage,
                            onValueChange = { userChatMessage = it },
                            placeholder = { Text("Scrie un mesaj...", color = Color.Gray, fontSize = 14.sp) },
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
                                    viewModel.fetchChatResponse(textTrimis)
                                }
                            },
                            containerColor = roseGold,
                            contentColor = emeraldDeep,
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Trimite", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}