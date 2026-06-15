package com.example.mystflowtb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.mystflowtb.screens.*
import com.example.mystflowtb.ui.theme.MystFlowTBTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mystflowtb.screens.SetupScreen
import com.example.mystflowtb.screens.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MystFlowTBTheme {
                val aiViewModel: AiViewModel = viewModel()
                var currentScreen by remember { mutableStateOf("WELCOME") }
                var authMethod by remember { mutableStateOf("PIN") }

                val premiumGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF004D3B),
                        Color(0xFF00382B),
                        Color(0xFF002119)
                    )
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(premiumGradient)
                    ) {
                        when (currentScreen) {
                            "WELCOME" -> {
                                WelcomeScreen(
                                    onNextClicked = {
                                        currentScreen = "LOGIN"
                                    }
                                )
                            }

                            "LOGIN" -> {
                                LoginScreen(
                                    authMethod = authMethod,
                                    onNavigateToSignUp = { currentScreen = "SIGNUP" },
                                    onLoginSuccess = { currentScreen = "HOME" }
                                )
                            }

                            "SIGNUP" -> SignUpScreen(
                                onMethodSelected = { metoda ->
                                    authMethod = metoda
                                    currentScreen = "SETUP"
                                }
                            )

                            "SETUP" -> SetupScreen(
                                method = authMethod,
                                onFinished = { codSalvat ->
                                    currentScreen = "LOGIN"
                                }
                            )

                            "HOME" -> HomeScreen(viewModel = aiViewModel)
                        }
                    }
                }
            }
        }
    }
}
    @Composable
    fun AiSecurityCard(viewModel: AiViewModel = viewModel(), userId: Int) {
        LaunchedEffect(userId) {
            viewModel.fetchInsight(userId)
        }


        Text(text = viewModel.insightMessage.value, color = Color.White)
    }